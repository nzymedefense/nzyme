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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.database.Database;
import horse.wtf.nzyme.dot11.networks.BSSID;
import horse.wtf.nzyme.dot11.networks.Channel;
import horse.wtf.nzyme.dot11.networks.Networks;
import horse.wtf.nzyme.dot11.networks.SSID;
import horse.wtf.nzyme.periodicals.Periodical;
import horse.wtf.nzyme.systemstatus.SystemStatus;
import horse.wtf.nzyme.util.MetricNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Map;

public class SignalIndexWriter extends Periodical {

    private static final Logger LOG = LogManager.getLogger(SignalIndexWriter.class);

    private final Networks networks;
    private final Database database;
    private final SystemStatus systemStatus;

    private final Timer writeTimer;

    public SignalIndexWriter(Nzyme nzyme) {
        this.networks = nzyme.getNetworks();
        this.database = nzyme.getDatabase();
        this.systemStatus = nzyme.getSystemStatus();

        this.writeTimer = nzyme.getMetrics().timer(MetricRegistry.name(MetricNames.SIGNAL_INDEX_WRITER_TIMER));
    }

    @Override
    protected void execute() {
        Timer.Context timer = writeTimer.time();

        try {
            for (Map.Entry<String, BSSID> bssid : networks.getBSSIDs().entrySet()) {
                for (Map.Entry<String, SSID> ssid : bssid.getValue().ssids().entrySet()) {
                    if (!ssid.getValue().isHumanReadable()) {
                        continue;
                    }

                    for (Map.Entry<Integer, Channel> channel : ssid.getValue().channels().entrySet()) {
                        if (this.systemStatus.isInStatus(SystemStatus.TYPE.TRAINING) || !channel.getValue().signalIndexThreshold().hadEnoughData()) {
                            // Don't write status during training phase or during low channel activity. It will fuck up the charts, make them hard to understand.
                            write(
                                    bssid.getValue().bssid(),
                                    ssid.getValue().name(),
                                    channel.getValue().channelNumber(),
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            );
                        } else {
                            write(
                                    bssid.getValue().bssid(),
                                    ssid.getValue().name(),
                                    channel.getValue().channelNumber(),
                                    channel.getValue().signalIndex(),
                                    channel.getValue().signalIndexThreshold().index(),
                                    channel.getValue().signalQualityRecentAverage(),
                                    channel.getValue().signalQualityRecentStddev(),
                                    channel.getValue().expectedDelta().lower(),
                                    channel.getValue().expectedDelta().upper()
                            );
                        }
                    }
                }
            }

        } catch(Exception e) {
            LOG.error("Could not write signal index information.", e);
        } finally {
            timer.stop();
        }
    }

    private void write(String bssid, String ssid, Integer channel, Float signalIndex, Float signalIndexThreshold, Integer signalQuality, Double signalStddev, Integer expectedDeltaUpper, Integer expectedDeltaLower) {
        database.useHandle(handle -> {
            handle.execute("INSERT INTO signal_index_history(bssid, ssid, channel, signal_index, signal_index_threshold, signal_quality, signal_stddev, expected_delta_upper, expected_delta_lower, created_at) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, DATETIME('now'))", bssid.toLowerCase(), ssid, channel, signalIndex, signalIndexThreshold, signalQuality, signalStddev, expectedDeltaUpper, expectedDeltaLower);
        });
    }


    @Override
    public String getName() {
        return "SignalIndexWriter";
    }
}
