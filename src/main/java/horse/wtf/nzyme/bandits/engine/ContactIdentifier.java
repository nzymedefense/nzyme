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
import java.util.concurrent.atomic.AtomicLong;

public class ContactIdentifier {

    private static final Logger LOG = LogManager.getLogger(ContactIdentifier.class);

    private final Nzyme nzyme;

    private ImmutableMap<UUID, Contact> contacts;
    private ImmutableMap<UUID, Bandit> bandits;

    public ContactIdentifier(Nzyme nzyme) {
        this.nzyme = nzyme;
    }

    public void registerBandit(Bandit bandit) {
        nzyme.getDatabase().useHandle(x -> x.inTransaction(handle -> {
            ResultBearing result = handle.createUpdate("INSERT INTO bandits(bandit_uuid, name, description, created_at, updated_at) " +
                    "VALUES(:bandit_uuid, :name, :description, (current_timestamp at time zone 'UTC'), (current_timestamp at time zone 'UTC'))")
                    .bind("bandit_uuid", bandit.uuid())
                    .bind("name", bandit.name())
                    .bind("description", bandit.description())
                    .executeAndReturnGeneratedKeys("id");

            Long banditId = result.mapTo(Long.class).first();

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
                            .bind("bandit_id", banditId)
                            .bind("identifier_type", identifier.descriptor().type())
                            .bind("configuration", configuration)
                            .execute();
                }
            }

            return null;
        }));

        this.bandits = null;
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

    public void registerContact(Contact contact) {
        if (contacts == null) {
            throw new IllegalStateException("ContactIdentifier has not been initialized.");
        }

        ImmutableMap.Builder<UUID, Contact> copy = copyOfContacts();
        copy.put(contact.uuid(), contact);

        this.contacts = copy.build();
    }

    public boolean banditHasActiveContact(Bandit bandit) {
        if (contacts == null) {
            throw new IllegalStateException("ContactIdentifier has not been initialized.");
        }

        for (Contact contact : copyOfContacts().build().values()) {
            if (contact.bandit().uuid().equals(bandit.uuid())) {
                return true;
            }
        }

        return false;
    }

    public void registerContactFrame(Bandit bandit, Dot11Frame frame) {
        if (contacts == null) {
            throw new IllegalStateException("ContactIdentifier has not been initialized.");
        }

        for (Contact contact : contacts.values()) {
            if (contact.bandit().uuid().equals(bandit.uuid())) {
                contact.recordFrame(frame);
                return;
            }
        }
    }

    public Map<UUID, Contact> getContacts() {
        return copyOfContacts().build();
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
                                bandit,
                                now,
                                now,
                                new AtomicLong(0)
                        ));
                    }

                    LOG.debug("Registering frame for existing bandit [{}]", bandit);
                    registerContactFrame(bandit, frame);
                }
            }
        }

    }

}