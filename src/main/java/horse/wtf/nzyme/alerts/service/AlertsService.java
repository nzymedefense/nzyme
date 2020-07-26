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

package horse.wtf.nzyme.alerts.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.alerts.service.callbacks.AlertCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AlertsService {

    public static final int EXPIRY_MINUTES = 10;

    private static final Logger LOG = LogManager.getLogger(AlertsService.class);

    private static final String ALERTS_QUERY = "SELECT * FROM alerts ORDER BY last_seen DESC LIMIT :limit OFFSET :offset";

    private static final String ACTIVE_ALERTS_QUERY = "SELECT * FROM alerts WHERE last_seen >(current_timestamp at time zone 'UTC' - interval '" + EXPIRY_MINUTES + " minutes') " +
            "ORDER BY last_seen DESC";

    private final NzymeLeader nzyme;

    private final List<AlertCallback> callbacks;

    public AlertsService(NzymeLeader nzyme) {
        this.nzyme = nzyme;
        this.callbacks = Lists.newArrayList();
    }

    public void handle(Alert alert) {
        // Check if this is already an active alert.
        for (Map.Entry<UUID, Alert> entry : findActiveAlerts().entrySet()) {
            Alert activeAlert = entry.getValue();
            if(activeAlert.sameAs(alert)) {
                // We've seen this alert before.
                updateLastSeenAndFrameCount(activeAlert, 1);
                return;
            }
        }

        // New alert.
        UUID uuid = UUID.randomUUID();
        alert.setUUID(uuid);

        // Notify uplinks.
        nzyme.notifyUplinksOfAlert(alert);

        // Notify callbacks.
        for (AlertCallback callback : callbacks) {
            LOG.info("Triggering alert callback type [{}]", callback.getClass().getCanonicalName());
            callback.call(alert);
        }

        writeAlert(alert);
    }

    public void registerCallbacks(List<AlertCallback> callbacks) {
        for (AlertCallback callback : callbacks) {
            registerCallback(callback);
        }

    }

    public void registerCallback(AlertCallback callback) {
        LOG.info("Registering alert callback of type [{}].", callback.getClass().getCanonicalName());
        this.callbacks.add(callback);
    }

    public Alert findAlert(UUID id) throws IOException {
        return Alert.serializeFromDatabase(nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM alerts WHERE alert_uuid = :uuid")
                .bind("uuid", id)
                .mapTo(AlertDatabaseEntry.class)
                .one()
        ));
    }

    public Map<UUID, Alert> findAllAlerts(int limit, int offset) {
        return buildAlertsMap(nzyme.getDatabase().withHandle(handle ->
                handle.createQuery(ALERTS_QUERY)
                        .bind("limit", limit)
                        .bind("offset", offset)
                        .mapTo(AlertDatabaseEntry.class)
                        .list()
        ));
    }

    public Map<UUID, Alert> findActiveAlerts() {
        return buildAlertsMap(nzyme.getDatabase().withHandle(handle ->
                handle.createQuery(ACTIVE_ALERTS_QUERY)
                        .mapTo(AlertDatabaseEntry.class)
                        .list()
        ));
    }

    public long countAllAlerts() {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM alerts;")
                        .mapTo(Long.class)
                        .one()
        );
    }

    private Map<UUID, Alert> buildAlertsMap(List<AlertDatabaseEntry> alertEntries) {
        ImmutableMap.Builder<UUID, Alert> alerts = new ImmutableMap.Builder<>();

        for (AlertDatabaseEntry db : alertEntries) {
            try {
                alerts.put(db.uuid(), Alert.serializeFromDatabase(db));
            } catch (Exception e) {
                LOG.error("Could not serialize alert from database.", e);
            }
        }

        return alerts.build();
    }

    private void writeAlert(Alert alert) {
        String fields;
        try {
            fields = nzyme.getObjectMapper().writeValueAsString(alert.getFields());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Couldn't serialize alert fields.", e);
        }

        nzyme.getDatabase().useHandle(handle -> handle.execute("INSERT INTO alerts(alert_uuid, alert_type, subsystem, " +
                        "fields, first_seen, last_seen, frame_count, use_frame_count) " +
                        "VALUES(?, ?, ?, ?, (current_timestamp at time zone 'UTC'), (current_timestamp at time zone 'UTC'), 1, ?)",
                alert.getUUID(),
                alert.getType(),
                alert.getSubsystem(),
                fields,
                alert.isUseFrameCount()
        ));
    }

    private void updateLastSeenAndFrameCount(Alert alert, int frameIncrement) {
        nzyme.getDatabase().useHandle(handle -> handle.execute("UPDATE alerts SET last_seen = (current_timestamp at time zone 'UTC'), frame_count = frame_count+? " +
                "WHERE alert_uuid = ?", frameIncrement, alert.getUUID()));
    }

}
