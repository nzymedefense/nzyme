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

package app.nzyme.core.dot11.networks.beaconrate;

import app.nzyme.plugin.Database;
import app.nzyme.core.NzymeLeader;
import app.nzyme.core.systemstatus.SystemStatus;
import app.nzyme.core.util.Tools;

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
