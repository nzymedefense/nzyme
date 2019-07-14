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

package horse.wtf.nzyme.periodicals.alerting.sigindex;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.alerts.AlertsService;
import horse.wtf.nzyme.alerts.SignalStrengthAnomalyAlert;
import horse.wtf.nzyme.configuration.Configuration;
import horse.wtf.nzyme.dot11.networks.BSSID;
import horse.wtf.nzyme.dot11.networks.Channel;
import horse.wtf.nzyme.dot11.networks.Networks;
import horse.wtf.nzyme.dot11.networks.SSID;
import horse.wtf.nzyme.periodicals.Periodical;
import horse.wtf.nzyme.systemstatus.SystemStatus;
import horse.wtf.nzyme.util.MetricNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class SignalIndexAnomalyAlertMonitor extends Periodical {

    private static final Logger LOG = LogManager.getLogger(SignalIndexAnomalyAlertMonitor.class);

    private final Networks networks;
    private final Configuration configuration;
    private final SystemStatus systemStatus;
    private final AlertsService alertsService;

    private final Timer timer;

    private AtomicReference<Map<AnomalySource, List<AnomalyStatus>>> statusTable;

    public SignalIndexAnomalyAlertMonitor(Nzyme nzyme) {
        this.networks = nzyme.getNetworks();
        this.systemStatus = nzyme.getSystemStatus();
        this.configuration = nzyme.getConfiguration();
        this.alertsService = nzyme.getAlertsService();

        this.timer = nzyme.getMetrics().timer(MetricRegistry.name(MetricNames.SIGNAL_INDEX_MONITOR_TIMER));
        this.statusTable = new AtomicReference<>(Maps.newHashMap());

        if (!nzyme.getMetrics().getGauges().containsKey(MetricNames.SIGNAL_INDEX_MONITOR_MEASUREMENTS)) {
            nzyme.getMetrics().register(MetricNames.SIGNAL_INDEX_MONITOR_MEASUREMENTS, (Gauge<Long>) () -> {
                long result = 0;
                for (List<AnomalyStatus> statuses : statusTable.get().values()) {
                    result += statuses.size();
                }

                return result;
            });
        }
    }

    @Override
    protected void execute() {
        Timer.Context ctx = this.timer.time();

        DateTime now = DateTime.now();
        try {
            for (BSSID bssid : networks.getBSSIDs().values()) {
                for (SSID ssid : bssid.ssids().values()) {
                    if (!ssid.isHumanReadable()) {
                        continue;
                    }

                    for (Channel channel : ssid.channels().values()) {
                        AnomalySource source = AnomalySource.create(bssid.bssid(), ssid.name(), channel.channelNumber());

                        if (!statusTable.get().containsKey(source)) {
                            statusTable.get().put(source, Lists.newArrayList());
                        }

                        statusTable.get().get(source).add(AnomalyStatus.create(
                                now,
                                source,
                                channel.signalIndexStatus() == Channel.SignalIndexStatus.ANOMALY)
                        );

                        // TODO: Write status to database.
                    }
                }
            }

            // Retention clean.
            ImmutableMap.Builder<AnomalySource, List<AnomalyStatus>> newStatusTable = new ImmutableMap.Builder<>();
            for (Map.Entry<AnomalySource, List<AnomalyStatus>> source : statusTable.get().entrySet()) {
                ImmutableList.Builder<AnomalyStatus> newStatus = new ImmutableList.Builder<>();
                for (AnomalyStatus anomalyStatus : source.getValue()) {
                    if(anomalyStatus.createdAt().isAfter(now.minusMinutes(1))) {
                        newStatus.add(anomalyStatus);
                    }
                }

                newStatusTable.put(source.getKey(), new ArrayList<>(newStatus.build()));
            }
            this.statusTable.set(new HashMap<>(newStatusTable.build()));

            // Don't run actual alerting during training phase.
            if (systemStatus.isInStatus(SystemStatus.TYPE.TRAINING)) {
                LOG.debug("Not running during training.");
                return;
            }

            // Trigger alert for those over the limit. We have 12 measurements per source per minute. (polling every 5 seconds)
            for (Map.Entry<AnomalySource, List<AnomalyStatus>> x : statusTable.get().entrySet()) {
                AnomalySource source = x.getKey();

                // Only run of our SSIDs.
                if(configuration.ourSSIDs().contains(source.ssid())) {
                    if (x.getValue().size() < configuration.anomalyAlertLookbackMinutes() * 12) {
                        // We have not had enough measurements for this source yet.
                        LOG.debug("Skipping source with not enough measurements (<{}>): [{}]", x.getValue().size(), source);
                        continue;
                    }

                    int anomalies = 0;
                    for (AnomalyStatus status : x.getValue()) {
                        if (status.anomaly()) {
                            anomalies++;
                        }
                    }

                    if (anomalies > (configuration.anomalyAlertLookbackMinutes() * 12) * configuration.anomalyAlertTriggerRatio()) {
                        alertsService.handle(SignalStrengthAnomalyAlert.create(source.ssid(), source.bssid(), source.channel()));
                    }
                }
            }


        } catch(Exception e) {
            LOG.error("Could not check signal index anomalies for alerting.", e);
        } finally {
            ctx.stop();
        }
    }

    @Override
    public String getName() {
        return "SignalIndexAnomalyAlertMonitor";
    }

    @AutoValue
    public static abstract class AnomalySource {

        public abstract String bssid();
        public abstract String ssid();
        public abstract int channel();

        public static AnomalySource create(String bssid, String ssid, int channel) {
            return builder()
                    .bssid(bssid)
                    .ssid(ssid)
                    .channel(channel)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_SignalIndexAnomalyAlertMonitor_AnomalySource.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder bssid(String bssid);

            public abstract Builder ssid(String ssid);

            public abstract Builder channel(int channel);

            public abstract AnomalySource build();
        }

    }

    @AutoValue
    public static abstract class AnomalyStatus {

        public abstract DateTime createdAt();
        public abstract AnomalySource source();
        public abstract boolean anomaly();

        public static AnomalyStatus create(DateTime createdAt, AnomalySource source, boolean anomaly) {
            return builder()
                    .createdAt(createdAt)
                    .source(source)
                    .anomaly(anomaly)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_SignalIndexAnomalyAlertMonitor_AnomalyStatus.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder createdAt(DateTime createdAt);

            public abstract Builder source(AnomalySource source);

            public abstract Builder anomaly(boolean anomaly);

            public abstract AnomalyStatus build();
        }
    }

}
