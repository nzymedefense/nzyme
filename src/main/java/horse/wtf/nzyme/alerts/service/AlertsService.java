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
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.alerts.Alert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AlertsService {

    private static final Logger LOG = LogManager.getLogger(AlertsService.class);

    private static final String ACTIVE_ALERTS_QUERY = "SELECT * FROM alerts WHERE last_seen >(current_timestamp at time zone 'UTC' - interval '10 minutes') " +
            "ORDER BY alert_uuid";

    private final NzymeLeader nzyme;

    public AlertsService(NzymeLeader nzyme) {
        this(
                nzyme,
                30,
                TimeUnit.MINUTES,
                nzyme.getConfiguration() == null ? 1 : nzyme.getConfiguration().alertingRetentionPeriodMinutes(),
                TimeUnit.MINUTES
        );
    }

    public AlertsService(NzymeLeader nzyme, int retentionCheckInterval, TimeUnit retentionCheckTimeUnit, int retentionDuration, TimeUnit retentionDurationTimeUnit) {
        this.nzyme = nzyme;

        // Retention cleaner.
        Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("alerts-cleaner")
                .build()
        ).scheduleAtFixedRate(() -> {
            try {
                runRetentionClean(retentionDuration, retentionDurationTimeUnit);
            } catch(Exception e){
                LOG.error("Error when trying to retention clean expired alerts.", e);
            }
        }, retentionCheckInterval, retentionCheckInterval, retentionCheckTimeUnit);
    }

    public void handle(Alert alert) {
        // Check if this is already an active alert.
        for (Map.Entry<UUID, Alert> entry : getActiveAlerts().entrySet()) {
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

        // Notify uplinks
        nzyme.notifyUplinksOfAlert(alert);

        writeAlert(alert);
    }

    public Map<UUID, Alert> getActiveAlerts() {
        List<AlertDatabaseEntry> alertEntries = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery(ACTIVE_ALERTS_QUERY)
                        .mapTo(AlertDatabaseEntry.class)
                        .list()
        );

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

    private void runRetentionClean(int retentionDuration, TimeUnit retentionDurationTimeUnit) {
        long seconds = retentionDurationTimeUnit.toSeconds(retentionDuration);

       nzyme.getDatabase().useHandle(handle ->
                handle.execute("DELETE FROM alerts WHERE last_seen < (current_timestamp at time zone 'UTC' - interval '" + seconds + " seconds')"));
    }

}
