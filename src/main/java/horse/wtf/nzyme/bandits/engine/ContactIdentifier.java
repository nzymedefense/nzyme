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

import com.google.common.collect.ImmutableMap;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.bandits.Contact;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifier;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.frames.Dot11DeauthenticationFrame;
import horse.wtf.nzyme.dot11.frames.Dot11Frame;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.result.ResultBearing;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ContactIdentifier {

    private static final Logger LOG = LogManager.getLogger(ContactIdentifier.class);

    private final Nzyme nzyme;

    private ImmutableMap<UUID, Contact> contacts;
    private ImmutableMap<UUID, Bandit> bandits;

    private static final int ACTIVE_MINUTES = 10;

    public ContactIdentifier(Nzyme nzyme) {
        this.nzyme = nzyme;
    }

    public long registerBandit(Bandit bandit) {
        AtomicReference<Long> banditId = new AtomicReference<>();
        nzyme.getDatabase().useHandle(x -> x.inTransaction(handle -> {
            ResultBearing result = handle.createUpdate("INSERT INTO bandits(bandit_uuid, name, description, created_at, updated_at) " +
                    "VALUES(:bandit_uuid, :name, :description, (current_timestamp at time zone 'UTC'), (current_timestamp at time zone 'UTC'))")
                    .bind("bandit_uuid", bandit.uuid())
                    .bind("name", bandit.name())
                    .bind("description", bandit.description())
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

                    handle.createUpdate("INSERT INTO bandit_identifiers(bandit_id, identifier_type, configuration, created_at, updated_at) " +
                            "VALUES(:bandit_id, :identifier_type, :configuration, (current_timestamp at time zone 'UTC'), (current_timestamp at time zone 'UTC'))")
                            .bind("bandit_id", banditId.get())
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

    public void removeBandit(UUID uuid) {
        nzyme.getDatabase().useHandle(handle -> handle.execute("DELETE FROM bandits WHERE bandit_uuid = ?", uuid));
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
                    x.createdAt(),
                    x.updatedAt(),
                    identifiers
            );
            result.put(bandit.uuid(), bandit);
        }

        this.bandits = result.build();
        return this.bandits;
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

        this.contacts = null;
        return count > 0;
    }

    // TODO: this might lead to heavy cache invalidation and we might have to rethink caching in general.
    //       same applies to fast UPDATES here
    public void registerContactFrame(Bandit bandit, Dot11Frame frame) {
        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("UPDATE contacts SET frame_count = frame_count+1, " +
                "last_seen = (current_timestamp at time zone 'UTC') " +
                "WHERE bandit_id = :bandit_id " +
                "AND last_seen > (current_timestamp at time zone 'UTC' - interval '" + ACTIVE_MINUTES + " minutes')")
                .bind("bandit_id", bandit.databaseId())
                .execute()
        );
        this.contacts = null;
    }


    // TODO needs retention cleaning
    public Map<UUID, Contact> getContacts() {
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

    // TODO timer
    public void identify(Dot11Frame frame) {
        for (Map.Entry<UUID, Bandit> x : getBandits().entrySet()) {
            Bandit bandit = x.getValue();

            // Run all identifiers.
            if(bandit.identifiers() != null && !bandit.identifiers().isEmpty()) {
                boolean anyMissed = false;
                for (BanditIdentifier identifier : bandit.identifiers()) {
                    Optional<Boolean> identified = Optional.empty();

                    if (frame instanceof Dot11BeaconFrame) {
                        identified = identifier.matches((Dot11BeaconFrame) frame);
                    }

                    if (frame instanceof Dot11ProbeResponseFrame) {
                        identified = identifier.matches((Dot11ProbeResponseFrame) frame);
                    }

                    if (frame instanceof Dot11DeauthenticationFrame) {
                        identified = identifier.matches((Dot11DeauthenticationFrame) frame);
                    }

                    if (identified.isPresent()) {
                        if (!identified.get()) {
                            anyMissed = true;
                        }
                    }
                }

                // If no identifier missed, this is a bandit frame.
                if (!anyMissed) {
                    // Create new contact if this is the first frame.s
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
                }
            }
        }
    }

}