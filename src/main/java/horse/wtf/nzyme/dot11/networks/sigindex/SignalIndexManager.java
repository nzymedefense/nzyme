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

import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.database.Database;
import horse.wtf.nzyme.systemstatus.SystemStatus;

public class SignalIndexManager {

    private static final int MINIMUM_DELTA_STATE_BASE = 50; // TODO make configurable

    private static final String AVERAGE_QUERY = "SELECT AVG(signal_index)_index FROM signal_index_history " +
            "WHERE bssid = ? AND ssid = ? AND channel = ? AND created_at > DATETIME('now', '-15 minutes')";

    private final Database database;
    private final SystemStatus systemStatus;

    public SignalIndexManager(Nzyme nzyme) {
        this.database = nzyme.getDatabase();
        this.systemStatus = nzyme.getSystemStatus();
    }

    private boolean isTraining() {
        return systemStatus.isInStatus(SystemStatus.TYPE.TRAINING);
    }

    public AverageSignalIndex getRecentAverageSignalIndex(String bssid, String ssid, int channel, int basedOnSize) {
        Float avg = database.withHandle(handle ->
                handle.createQuery(AVERAGE_QUERY)
                        .bind(0, bssid)
                        .bind(1, ssid)
                        .bind(2, channel)
                        .mapTo(Float.class)
                        .first()
        );

        if (avg == null) {
            return AverageSignalIndex.create(0, false, isTraining());
        }

        if(basedOnSize < MINIMUM_DELTA_STATE_BASE) {
            return AverageSignalIndex.create(avg, false, isTraining());
        }

        return AverageSignalIndex.create(avg, true, isTraining());
    }

}
