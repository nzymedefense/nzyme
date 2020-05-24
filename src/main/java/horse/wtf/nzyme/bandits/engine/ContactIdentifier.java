/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.bandits.engine;

import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableMap;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.alerts.BanditContactAlert;
import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.bandits.BanditListProvider;
import horse.wtf.nzyme.bandits.Contact;
import horse.wtf.nzyme.bandits.DefaultBandits;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifier;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.frames.Dot11DeauthenticationFrame;
import horse.wtf.nzyme.dot11.frames.Dot11Frame;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import horse.wtf.nzyme.util.MetricNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.result.ResultBearing;
import org.joda.time.DateTime;

import javax.validation.constraints.Null;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ContactIdentifier implements BanditListProvider {

    private static final Logger LOG = LogManager.getLogger(ContactIdentifier.class);

    public static final int ACTIVE_MINUTES = 10;

    private final NzymeLeader nzyme;

    private ImmutableMap<UUID, Contact> contacts;
    private ImmutableMap<UUID, Bandit> bandits;

    private final Timer timing;

    public ContactIdentifier(NzymeLeader nzyme) {
        this.nzyme = nzyme;
        this.timing = nzyme.getMetrics().timer(MetricNames.CONTACT_IDENTIFIER_TIMING);

        // Register default bandits.
        DefaultBandits.seed(this);
    }

    public long registerBandit(Bandit bandit) {
        AtomicReference<Long> banditId = new AtomicReference<>();
        nzyme.getDatabase().useHandle(x -> x.inTransaction(handle -> {
            ResultBearing result = handle.createUpdate("INSERT INTO bandits(bandit_uuid, name, description, read_only, created_at, updated_at) " +
                    "VALUES(:bandit_uuid, :name, :description, :read_only, (current_timestamp at time zone 'UTC'), (current_timestamp at time zone 'UTC'))")
                    .bind("bandit_uuid", bandit.uuid())
                    .bind("name", bandit.name())
                    .bind("description", bandit.description())
                    .bind("read_only", bandit.readOnly())
                    .executeAndReturnGeneratedKeys("id");

            banditId.set(result.mapTo(Long.class).first());

            if (bandit.identifiers() != null) {
                for (BanditIdentifier identifier : bandit.identifiers()) {
                    String configuration;

                    try {
                        configuration = nzyme.getObjectMapper().writeValueAsString(identifier.configuration());
                    } catch(Exception e) {
                        throw new RuntimeException(e);
                    }

                    handle.createUpdate("INSERT INTO bandit_identifiers(bandit_id, identifier_uuid, identifier_type, configuration, created_at, updated_at) " +
                            "VALUES(:bandit_id, :identifier_uuid, :identifier_type, :configuration, (current_timestamp at time zone 'UTC'), (current_timestamp at time zone 'UTC'))")
                            .bind("bandit_id", banditId.get())
                            .bind("identifier_uuid", identifier.getUuid())
                            .bind("identifier_type", identifier.descriptor().type())
                            .bind("configuration", configuration)
                            .execute();
                }
            }

            return null;
        }));

        this.bandits = null;
        return banditId.get();
    }

    public void updateBandit(UUID uuid, String description, String name) {
        nzyme.getDatabase().useHandle(handle -> handle.execute(
                "UPDATE bandits SET name = ?, description = ?, updated_at = (current_timestamp at time zone 'UTC') WHERE bandit_uuid = ?",
                name, description, uuid
        ));
        this.bandits = null;
    }

    public void removeBandit(UUID uuid) {
        nzyme.getDatabase().useHandle(handle -> handle.execute("DELETE FROM bandits WHERE bandit_uuid = ?", uuid));
        this.bandits = null;
    }

    public void registerIdentifier(Bandit bandit, BanditIdentifier identifier) {
        nzyme.getDatabase().useHandle(handle -> {
            String configuration;
            try {
                configuration = nzyme.getObjectMapper().writeValueAsString(identifier.configuration());
            } catch(Exception e) {
                throw new RuntimeException(e);
            }

            handle.createUpdate("INSERT INTO bandit_identifiers(bandit_id, identifier_uuid, identifier_type, configuration, created_at, updated_at) " +
                    "VALUES(:bandit_id, :identifier_uuid, :identifier_type, :configuration, (current_timestamp at time zone 'UTC'), (current_timestamp at time zone 'UTC'))")
                    .bind("bandit_id", bandit.databaseId())
                    .bind("identifier_uuid", UUID.randomUUID())
                    .bind("identifier_type", identifier.descriptor().type())
                    .bind("configuration", configuration)
                    .execute();

            handle.createUpdate("UPDATE bandits SET updated_at = (current_timestamp at time zone 'UTC') WHERE bandit_uuid = :bandit_uuid")
                    .bind("bandit_uuid",  bandit.uuid())
                    .execute();
        });
        this.bandits = null;
    }

    public void removeIdentifier(UUID uuid) {
        nzyme.getDatabase().useHandle(handle -> handle.execute("DELETE FROM bandit_identifiers WHERE identifier_uuid = ?", uuid));
        this.bandits = null;

    }

    public Map<UUID, Bandit> getBandits() {
        // Return the cached bandits if they have not been invalidated by a writing function.
        if (bandits != null) {
            return bandits;
        }

        List<Bandit> bandits = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM bandits")
                        .mapTo(Bandit.class)
                        .list()
        );

        ImmutableMap.Builder<UUID, Bandit> result = new ImmutableMap.Builder<>();

        for (Bandit x : bandits) {
            List<BanditIdentifier> identifiers = nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT * FROM bandit_identifiers WHERE bandit_id = :bandit_id")
                            .bind("bandit_id", x.databaseId())
                            .mapTo(BanditIdentifier.class)
                            .list()
            );

            Bandit bandit = Bandit.create(
                    x.databaseId(),
                    x.uuid(),
                    x.name(),
                    x.description(),
                    x.readOnly(),
                    x.createdAt(),
                    x.updatedAt(),
                    identifiers
            );
            result.put(bandit.uuid(), bandit);
        }

        this.bandits = result.build();
        return this.bandits;
    }

    @Override
    public List<Bandit> getBanditList() {
        return new ArrayList<>(getBandits().values());
    }

    @Override
    @Null
    public Bandit getCurrentlyTrackedBandit() {
        // Only relevant for tracker implementations.
        return null;
    }

    public Optional<Bandit> findBanditByDatabaseId(Long id) {
        for (Bandit bandit : getBandits().values()) {
            if (bandit.databaseId() != null && bandit.databaseId().equals(id)) {
                return Optional.of(bandit);
            }
        }

        return Optional.empty();
    }

    public Optional<Bandit> findBanditByUUID(UUID uuid) {
        for (Bandit bandit : getBandits().values()) {
            if (bandit.uuid() != null && bandit.uuid().equals(uuid)) {
                return Optional.of(bandit);
            }
        }

        return Optional.empty();
    }

    public void registerContact(Contact contact) {
        //noinspection ConstantConditions
        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("INSERT INTO contacts(contact_uuid, bandit_id, frame_count, first_seen, last_seen) " +
                "VALUES(:contact_uuid, :bandit_id, 0, (current_timestamp at time zone 'UTC'), (current_timestamp at time zone 'UTC'))")
                .bind("contact_uuid", contact.uuid())
                .bind("bandit_id", contact.bandit().databaseId())
                .execute()
        );
        this.contacts = null;
    }

    public boolean banditHasActiveContact(Bandit bandit) {
        long count = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM contacts " +
                        "WHERE bandit_id = :bandit_id " +
                        "AND last_seen > (current_timestamp at time zone 'UTC' - interval '" + ACTIVE_MINUTES + " minutes')")
                .bind("bandit_id", bandit.databaseId())
                .mapTo(Long.class)
                .first());
        return count > 0;
    }

    public void registerContactFrame(Bandit bandit, Dot11Frame frame) {
        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("UPDATE contacts SET frame_count = frame_count+1, " +
                "last_seen = (current_timestamp at time zone 'UTC') " +
                "WHERE bandit_id = :bandit_id " +
                "AND last_seen > (current_timestamp at time zone 'UTC' - interval '" + ACTIVE_MINUTES + " minutes')")
                .bind("bandit_id", bandit.databaseId())
                .execute()
        );

        // TODO: this will cause way too many queries. find a better way.
        this.contacts = null;
    }

    public Map<UUID, Contact> findContacts() {
        if (contacts != null) {
            return contacts;
        }

        List<Contact> contacts = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM contacts")
                        .mapTo(Contact.class)
                        .list()
        );

        ImmutableMap.Builder<UUID, Contact> result = new ImmutableMap.Builder<>();
        for (Contact x : contacts) {
            result.put(x.uuid(), Contact.create(
                    x.uuid(),
                    x.banditId(),
                    findBanditByDatabaseId(x.banditId()).orElse(null),
                    x.firstSeen(),
                    x.lastSeen(),
                    x.frameCount()
            ));
        }

        this.contacts = result.build();
        return this.contacts;
    }

    public List<Contact> findContactsOfBandit(Bandit bandit) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM contacts WHERE bandit_id = :bandit_id ORDER BY last_seen DESC LIMIT 50")
                        .bind("bandit_id", bandit.databaseId())
                        .mapTo(Contact.class)
                        .list()
        );
    }

    public void identify(Dot11Frame frame) {
        Timer.Context timer = this.timing.time();
        for (Map.Entry<UUID, Bandit> x : getBandits().entrySet()) {
            Bandit bandit = x.getValue();

            // Run all identifiers.
            if(bandit.identifiers() != null && !bandit.identifiers().isEmpty()) {
                boolean match = false;
                for (BanditIdentifier identifier : bandit.identifiers()) {
                    if (frame instanceof Dot11BeaconFrame) {
                        Optional<Boolean> matches = identifier.matches((Dot11BeaconFrame) frame);
                        if (matches.isPresent() && matches.get()) {
                            match = true;
                            break;
                        }
                    }

                    if (frame instanceof Dot11ProbeResponseFrame) {
                        Optional<Boolean> matches = identifier.matches((Dot11ProbeResponseFrame) frame);
                        if (matches.isPresent() && matches.get()) {
                            match = true;
                            break;
                        }
                    }

                    if (frame instanceof Dot11DeauthenticationFrame) {
                        Optional<Boolean> matches = identifier.matches((Dot11DeauthenticationFrame) frame);
                        if (matches.isPresent() && matches.get()) {
                            match = true;
                            break;
                        }
                    }
                }

                // If no identifier missed, this is a bandit frame.
                if (match) {
                    // Create new contact if this is the first frame.
                    if (!banditHasActiveContact(bandit)) {
                        LOG.debug("New contact for bandit [{}].", bandit);
                        DateTime now = DateTime.now();
                        registerContact(Contact.create(
                                UUID.randomUUID(),
                                null,
                                bandit,
                                now,
                                now,
                                1L
                        ));
                    }

                    LOG.debug("Registering frame for existing bandit [{}]", bandit);
                    registerContactFrame(bandit, frame);

                    // Register/refresh alert.
                    if (nzyme.getConfiguration().dot11Alerts().contains(Alert.TYPE_WIDE.BANDIT_CONTACT)) {
                        nzyme.getAlertsService().handle(BanditContactAlert.create(DateTime.now(), bandit.name(), bandit.uuid().toString(), 1L));
                    }
                }
            }
        }

        timer.stop();
    }

}