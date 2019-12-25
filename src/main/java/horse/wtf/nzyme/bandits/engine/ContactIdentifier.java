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
import org.joda.time.DateTime;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ContactIdentifier {

    private static final Logger LOG = LogManager.getLogger(ContactIdentifier.class);

    private final Nzyme nzyme;

    private ImmutableMap<UUID, Bandit> bandits;
    private ImmutableMap<UUID, Contact> contacts;

    public ContactIdentifier(Nzyme nzyme) {
        this.nzyme = nzyme;
    }

    public void registerBandit(Bandit bandit) {
        if (bandits == null) {
            throw new IllegalStateException("ContactIdentifier has not been initialized.");
        }

        ImmutableMap.Builder<UUID, Bandit> newBandits = copyOfBandits();
        newBandits.put(bandit.uuid(), bandit);

        this.bandits = newBandits.build();
    }

    public void removeBandit(UUID uuid) {
        if (bandits == null) {
            throw new IllegalStateException("ContactIdentifier has not been initialized.");
        }

        ImmutableMap.Builder<UUID, Bandit> copy = copyOfBandits();

        ImmutableMap.Builder<UUID, Bandit> newBandits = new ImmutableMap.Builder<>();
        for (Map.Entry<UUID, Bandit> x : copy.build().entrySet()) {
            if (!x.getKey().equals(uuid)) {
                newBandits.put(x.getKey(), x.getValue());
            }
        }

        this.bandits = newBandits.build();
    }

    public Map<UUID, Bandit> getBandits() {
        return copyOfBandits().build();
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

    public void initialize() {
        // TODO seed from database (last_seen > 30 minutes)
        bandits = new ImmutableMap.Builder<UUID, Bandit>().build();
        contacts = new ImmutableMap.Builder<UUID, Contact>().build();
    }

    // TODO timer
    public void identify(Dot11Frame frame) {
        for (Map.Entry<UUID, Bandit> x : copyOfBandits().build().entrySet()) {
            Bandit bandit = x.getValue();

            // Run all identifiers.
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

    private ImmutableMap.Builder<UUID, Bandit> copyOfBandits() {
        ImmutableMap.Builder<UUID, Bandit> c = new ImmutableMap.Builder<>();
        c.putAll(bandits);

        return c;
    }

    private ImmutableMap.Builder<UUID, Contact> copyOfContacts() {
        ImmutableMap.Builder<UUID, Contact> c = new ImmutableMap.Builder<>();
        c.putAll(contacts);

        return c;
    }

}