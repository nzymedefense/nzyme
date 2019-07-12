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

package horse.wtf.nzyme.alerts;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.Nzyme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.ReadableDuration;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class AlertsService {

    private static final Logger LOG = LogManager.getLogger(AlertsService.class);

    private final Map<UUID, Alert> activeAlerts;

    public AlertsService(Nzyme nzyme) {
        this(
                nzyme,
                10,
                TimeUnit.SECONDS,
                nzyme.getConfiguration() == null ? 1 : nzyme.getConfiguration().alertingRetentionPeriodMinutes(),
                TimeUnit.MINUTES
        );
    }

    public AlertsService(Nzyme nzyme, int retentionCheckInterval, TimeUnit retentionCheckTimeUnit, int retentionDuration, TimeUnit retentionDurationTimeUnit) {
        this.activeAlerts = Maps.newHashMap();

        // Regularly delete expired alerts from active alerts.
        Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("alerts-cleaner")
                .build()
        ).scheduleAtFixedRate(() -> {
            try {
                for (Map.Entry<UUID, Alert> entry : Lists.newArrayList(activeAlerts.entrySet())) {
                    Alert alert = entry.getValue();

                    if (alert.getLastSeen().isBefore(DateTime.now().minus(TimeUnit.MILLISECONDS.convert(retentionDuration, retentionDurationTimeUnit)))) {
                        LOG.info("Retention cleaning expired alert [{}/{}]", entry.getValue(), alert.getType());
                        activeAlerts.remove(entry.getKey());
                    }
                }
            } catch(Exception e){
                LOG.error("Error when trying to retention clean expired alerts.", e);
            }
        }, retentionCheckInterval, retentionCheckInterval, retentionCheckTimeUnit);
    }

    public void handle(Alert alert) {
        // Check if this is already an active alert.
        for (Map.Entry<UUID, Alert> entry : activeAlerts.entrySet()) {
            Alert activeAlert = entry.getValue();
            if(activeAlert.sameAs(alert)) {
                // We've seen this alert before. Update the last seen time.
                activeAlert.setLastSeen(DateTime.now());
                activeAlert.incrementFrameCount();
                activeAlerts.put(entry.getKey(), activeAlert);
                return;
            }
        }

        UUID uuid = UUID.randomUUID();
        alert.setUUID(uuid);

        // Notify uplinks
        if (alert.getProbe() != null) { // TODO remove me. HACK until probe -> uplink rel is refactored.
            alert.getProbe().notifyUplinksOfAlert(alert);
        }

        activeAlerts.put(uuid, alert);
    }

    public Map<UUID, Alert> getActiveAlerts() {
        return ImmutableMap.copyOf(this.activeAlerts);
    }

}
