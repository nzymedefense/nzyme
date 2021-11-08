/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package horse.wtf.nzyme.bandits.engine;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.alerts.BanditContactAlert;
import horse.wtf.nzyme.bandits.*;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifier;
import horse.wtf.nzyme.bandits.trackers.Tracker;
import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import horse.wtf.nzyme.dot11.frames.Dot11Frame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.result.ResultBearing;
import org.joda.time.DateTime;

import javax.validation.constraints.Null;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ContactManager implements ContactIdentifierProcess {

    private static final Logger LOG = LogManager.getLogger(ContactManager.class);

    private final NzymeLeader nzyme;

    private ImmutableMap<UUID, Contact> contacts;
    private ImmutableMap<UUID, Bandit> bandits;

    private final ContactIdentifierEngine identifierEngine;
    private final ContactRecorder contactRecorder;

    public ContactManager(NzymeLeader nzyme) {
        this.nzyme = nzyme;

        this.identifierEngine = new ContactIdentifierEngine(nzyme.getMetrics());
        this.contactRecorder = new ContactRecorder(10, nzyme);

        // Register default bandits.
        DefaultBandits.seed(this);
    }

    public ContactRecorder getContactRecorder() {
        return contactRecorder;
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

    public boolean banditExists(UUID uuid) {
        // Use the cached bandits if they have not been invalidated by a writing function.
        if (bandits != null) {
            for (Bandit bandit : bandits.values()) {
                if (bandit.uuid().equals(uuid)) {
                    return true;
                }
            }
        }

        long count = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM bandits WHERE bandit_uuid = :uuid")
                        .bind("uuid", uuid)
                        .mapTo(Long.class)
                        .first());

        return count > 0;
    }

    public Map<UUID, Bandit> getBandits() {
        // Return the cached bandits if they have not been invalidated by a writing function.
        if (bandits != null) {
            return bandits;
        }

        List<Bandit> bandits = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM bandits ORDER BY updated_at DESC;")
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
        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("INSERT INTO contacts(contact_uuid, source_role, source_name, bandit_id, frame_count, first_seen, last_seen) " +
                "VALUES(:contact_uuid, :source_role, :source_name, :bandit_id, 0, (current_timestamp at time zone 'UTC'), (current_timestamp at time zone 'UTC'))")
                .bind("contact_uuid", contact.uuid())
                .bind("bandit_id", contact.bandit().databaseId())
                .bind("source_role", contact.sourceRole())
                .bind("source_name", contact.sourceName())
                .execute()
        );
        this.contacts = null;
    }

    public void registerTrackerContactStatus(TrackerMessage.ContactStatus status) {
        Optional<Bandit> opt = findBanditByUUID(UUID.fromString(status.getUuid()));
        if (opt.isEmpty()) {
            LOG.info("Ignoring contact status for non-existent bandit [{}].", status.getUuid());
            return;
        }

        Bandit bandit = opt.get();
        if (banditHasActiveContactOnSource(bandit, status.getSource())) {
            // Update existing contact.
            updateContactFrames(bandit, status.getSource(), status.getFrames(), status.getRssi());
        } else {
            // First contact.
            registerContact(Contact.create(
                    UUID.randomUUID(),
                    DateTime.now(),
                    DateTime.now(),
                    status.getFrames(),
                    Role.TRACKER,
                    status.getSource(),
                    status.getRssi(),
                    bandit.databaseId(),
                    bandit
            ));
        }
    }

    public boolean banditHasActiveContactOnSource(Bandit bandit, String sourceName) {
        long count = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM contacts " +
                        "WHERE bandit_id = :bandit_id AND source_name = :source_name " +
                        "AND last_seen > (current_timestamp at time zone 'UTC' - interval '" + TrackTimeout.MINUTES + " minutes')")
                        .bind("bandit_id", bandit.databaseId())
                        .bind("source_name", sourceName)
                        .mapTo(Long.class)
                        .first());
        return count > 0;
    }

    public void registerContactFrame(Bandit bandit, String sourceName, int rssi, String bssid, Optional<String> ssid) {
        UUID contactUUID = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT contact_uuid FROM contacts WHERE bandit_id = :bandit_id AND source_name = :source_name AND last_seen > (current_timestamp at time zone 'UTC' - interval '" + TrackTimeout.MINUTES + " minutes')")
                        .bind("source_name", sourceName)
                        .bind("last_signal", rssi)
                        .bind("bandit_id", bandit.databaseId())
                        .mapTo(UUID.class)
                        .first()
        );

        if (contactUUID != null) {
            nzyme.getDatabase().useHandle(handle -> handle.createUpdate("UPDATE contacts SET frame_count = frame_count+1, " +
                            "last_seen = (current_timestamp at time zone 'UTC'), last_signal = :last_signal " +
                            "WHERE contact_uuid = :contact_uuid")
                    .bind("last_signal", rssi)
                    .bind("contact_uuid", contactUUID)
                    .execute()
            );

            // Register frame in contact recorder for tracking.
            this.contactRecorder.recordFrame(contactUUID, rssi, bssid, ssid);

            // TODO: this will cause way too many queries. find a better way.
            this.contacts = null;
        }
    }

    public Optional<List<ContactRecordAggregation>> findRecordValuesOfContact(UUID contactUUID, ContactRecorder.RECORD_TYPE recordType) {
        List<ContactRecordAggregation> values = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT DISTINCT(record_value) AS record_value, SUM(frame_count) AS frame_count FROM contact_records " +
                                "WHERE contact_uuid = :contact_uuid AND record_type = :record_type " +
                                "GROUP BY record_value ORDER BY frame_count DESC")
                        .bind("contact_uuid", contactUUID)
                        .bind("record_type", recordType)
                        .mapTo(ContactRecordAggregation.class)
                        .list()
        );

        if (values == null || values.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(values);
        }
    }

    public Optional<Map<String, Map<String, Long>>> findFrameCountHistogramsOfContact(UUID contactUUID, List<String> values, ContactRecorder.RECORD_TYPE type) {
        if (values.isEmpty()) {
            return Optional.empty();
        }

        Map<String, Map<String, Long>> result = Maps.newHashMap();

        for (String value : values) {
            Map<String, Long> histogram = Maps.newHashMap();

            List<ContactRecorderFrameCountHistogramEntry> entries = nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT frame_count, created_at FROM contact_records " +
                                    "WHERE contact_uuid = :contact_uuid AND record_type = :record_type AND record_value = :value " +
                                    "AND created_at > (current_timestamp at time zone 'UTC' - interval '1 day') " +
                                    "ORDER BY created_at DESC")
                            .bind("contact_uuid", contactUUID)
                            .bind("record_type", type)
                            .bind("value", value)
                            .mapTo(ContactRecorderFrameCountHistogramEntry.class)
                            .list()
            );

            for (ContactRecorderFrameCountHistogramEntry entry : entries) {
                histogram.put(entry.createdAt().toString(), entry.frameCount());
            }

            result.put(value, histogram);
        }

        return Optional.of(result);
    }

    public void updateContactFrames(Bandit bandit, String sourceName, long frameCount, int rssi) {
        LOG.info("new contact frame: signal {}", rssi);

        nzyme.getDatabase().useHandle(handle -> handle.createUpdate("UPDATE contacts SET frame_count = :frame_count, " +
                "last_seen = (current_timestamp at time zone 'UTC'), last_signal = :last_signal " +
                "WHERE bandit_id = :bandit_id AND source_name = :source_name " +
                "AND last_seen > (current_timestamp at time zone 'UTC' - interval '" + TrackTimeout.MINUTES + " minutes')")
                .bind("frame_count", frameCount)
                .bind("last_signal", rssi)
                .bind("source_name", sourceName)
                .bind("bandit_id", bandit.databaseId())
                .execute()
        );

        // TODO: this will cause way too many queries. find a better way.
        this.contacts = null;
    }

    public Map<UUID, Contact> findContacts() {
        return findContacts(Integer.MAX_VALUE, 0);
    }

    public Map<UUID, Contact> findContacts(int limit, int offset) {
        List<Contact> contacts = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM contacts ORDER BY last_seen DESC LIMIT :limit OFFSET :offset")
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(Contact.class)
                        .list()
        );

        ImmutableMap.Builder<UUID, Contact> result = new ImmutableMap.Builder<>();
        for (Contact x : contacts) {
            result.put(x.uuid(), Contact.create(
                    x.uuid(),
                    x.firstSeen(),
                    x.lastSeen(),
                    x.frameCount(),
                    x.sourceRole(),
                    x.sourceName(),
                    x.lastSignal(),
                    x.banditId(),
                    findBanditByDatabaseId(x.banditId()).orElse(null)
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

    public Optional<Contact> findContactOfBandit(Bandit bandit, UUID contactUUID) {
        Contact contact = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM contacts WHERE bandit_id = :bandit_id AND contact_uuid = :contact_uuid")
                        .bind("bandit_id", bandit.databaseId())
                        .bind("contact_uuid", contactUUID)
                        .mapTo(Contact.class)
                        .first()
        );

        if (contact == null) {
            return Optional.empty();
        } else {
            return Optional.of(contact);
        }
    }

    public List<Contact> findContactsOfTracker(Tracker tracker) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM contacts WHERE source_name = :source_name ORDER BY last_seen DESC LIMIT 50")
                        .bind("source_name", tracker.getName())
                        .mapTo(Contact.class)
                        .list()
        );
    }
    
    @Override
    public void identify(Dot11Frame frame) {
        for (Map.Entry<UUID, Bandit> x : getBandits().entrySet()) {
            Bandit bandit = x.getValue();

            // Run all identifiers.
            if(bandit.identifiers() != null && !bandit.identifiers().isEmpty()) {
                // If no identifier missed, this is a bandit frame.

                Optional<ContactIdentifierEngine.ContactIdentification> result = identifierEngine.identify(frame, bandit);
                if (result.isPresent()) {
                    // Create new contact if this is the first frame.
                    if (!banditHasActiveContactOnSource(bandit, nzyme.getNodeID())) {
                        LOG.debug("New contact for bandit [{}].", bandit);
                        DateTime now = DateTime.now();
                        registerContact(Contact.create(
                                UUID.randomUUID(),
                                now,
                                now,
                                1L,
                                Role.LEADER,
                                nzyme.getNodeID(),
                                frame.meta().getAntennaSignal(),
                                null,
                                bandit
                        ));
                    }

                    LOG.debug("Registering frame for existing bandit [{}]", bandit);
                    registerContactFrame(bandit, nzyme.getNodeID(), frame.meta().getAntennaSignal(), result.get().bssid(), result.get().ssid());

                    // Register/refresh alert.
                    if (nzyme.getConfiguration().dot11Alerts().contains(Alert.TYPE_WIDE.BANDIT_CONTACT)) {
                        nzyme.getAlertsService().handle(BanditContactAlert.create(DateTime.now(), bandit.name(), bandit.uuid().toString(), result.get().ssid(), 1L));
                    }
                }
            }
        }
    }

}