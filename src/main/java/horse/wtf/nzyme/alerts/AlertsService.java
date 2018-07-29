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
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.Nzyme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class AlertsService {

    private static final Logger LOG = LogManager.getLogger(AlertsService.class);

    private final Nzyme nzyme;

    private final Map<UUID, Alert> activeAlerts;

    public AlertsService(Nzyme nzyme) {
        this.nzyme = nzyme;
        this.activeAlerts = Maps.newHashMap();

        // Regularly delete expired alerts from active alerts.
        Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("alerts-cleaner")
                .build()
        ).scheduleAtFixedRate(() -> {
            for (Map.Entry<UUID, Alert> entry : activeAlerts.entrySet()) {
                Alert alert = entry.getValue();

                if(alert.getLastSeen().isBefore(DateTime.now().minusMinutes(nzyme.getConfiguration().getAlertingRetentionPeriodMinutes()))) {
                    LOG.info("Retention cleaning expired alert [{}/{}]", entry.getValue(), alert.getType());
                    activeAlerts.remove(entry.getKey());
                }
            }
        }, 10, 10, TimeUnit.SECONDS);
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
        LOG.warn("ALERT: [{}] {}", uuid, alert.getMessage());

        activeAlerts.put(uuid, alert);
    }

    public Map<UUID, Alert> getActiveAlerts() {
        return ImmutableMap.copyOf(this.activeAlerts);
    }

}
