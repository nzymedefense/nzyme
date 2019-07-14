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

package horse.wtf.nzyme.dot11.networks.sigindex;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.database.Database;
import horse.wtf.nzyme.systemstatus.SystemStatus;
import horse.wtf.nzyme.util.MetricNames;

public class SignalIndexManager {

    private static final int MINIMUM_DELTA_STATE_BASE = 25;

    private static final String AVERAGE_QUERY = "SELECT AVG(signal_index) FROM signal_index_history " +
            "WHERE bssid = ? AND ssid = ? AND channel = ? AND created_at > DATETIME('now', '-10 minutes')";

    private final Database database;
    private final SystemStatus systemStatus;

    private final Timer timer;

    public SignalIndexManager(Nzyme nzyme) {
        this.database = nzyme.getDatabase();
        this.systemStatus = nzyme.getSystemStatus();

        this.timer = nzyme.getMetrics().timer(MetricRegistry.name(MetricNames.SIGNAL_INDEX_READER_TIMER));
    }

    private boolean isTraining() {
        return systemStatus.isInStatus(SystemStatus.TYPE.TRAINING);
    }

    public AverageSignalIndex getRecentAverageSignalIndex(String bssid, String ssid, int channel, int basedOnSize) {
        Timer.Context ctx = timer.time();

        try {
            Float avg = database.withHandle(handle ->
                    handle.createQuery(AVERAGE_QUERY)
                            .bind(0, bssid)
                            .bind(1, ssid)
                            .bind(2, channel)
                            .mapTo(Float.class)
                            .first()
            );

            if (avg == null) {
                return AverageSignalIndex.create(null, false, isTraining());
            }

            // A minimum buffer.
            if (avg < 0.1) {
                avg = 0.1F;
            }

            if (basedOnSize < MINIMUM_DELTA_STATE_BASE) {
                return AverageSignalIndex.create(null, false, isTraining());
            }

            return AverageSignalIndex.create(avg, true, isTraining());
        } finally {
            ctx.stop();
        }
    }

}
