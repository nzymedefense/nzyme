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

package horse.wtf.nzyme.dot11.networks.beaconrate;

import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.database.Database;
import horse.wtf.nzyme.systemstatus.SystemStatus;
import horse.wtf.nzyme.util.Tools;

public class BeaconRateManager {

    private static final String AVERAGE_QUERY = "SELECT AVG(beacon_rate) FROM beacon_rate_history " +
            "WHERE bssid = ? AND ssid = ? AND created_at > (current_timestamp at time zone 'UTC' - interval '1 minute')";

    private final Database database;
    private final SystemStatus systemStatus;

    public BeaconRateManager(NzymeLeader nzyme) {
        this.database = nzyme.getDatabase();
        this.systemStatus = nzyme.getSystemStatus();
    }

    public BeaconRate getAverageBeaconRate(String bssid, String ssid) {
        if (!Tools.isHumanlyReadable(ssid)) {
            return BeaconRate.create(0.0F, systemStatus.isInStatus(SystemStatus.TYPE.TRAINING));
        }

        Float avg = database.withHandle(handle ->
                handle.createQuery(AVERAGE_QUERY)
                        .bind(0, bssid)
                        .bind(1, ssid)
                        .mapTo(Float.class)
                        .first()
        );

        if (avg == null) {
            avg = 0.0F;
        }

        return BeaconRate.create(avg, systemStatus.isInStatus(SystemStatus.TYPE.TRAINING));
    }

}
