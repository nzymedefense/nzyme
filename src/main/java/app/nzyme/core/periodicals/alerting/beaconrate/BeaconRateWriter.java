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

package app.nzyme.core.periodicals.alerting.beaconrate;

import app.nzyme.plugin.Database;
import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.networks.BSSID;
import app.nzyme.core.dot11.networks.Networks;
import app.nzyme.core.dot11.networks.SSID;
import app.nzyme.core.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BeaconRateWriter extends Periodical {

    private static final Logger LOG = LogManager.getLogger(BeaconRateWriter.class);

    private final Networks networks;
    private final Database database;

    public BeaconRateWriter(NzymeNode nzyme) {
        this.networks = nzyme.getNetworks();
        this.database = nzyme.getDatabase();
    }

    @Override
    protected void execute() {
        try {
            for (BSSID bssid : networks.getBSSIDs().values()) {
                for (SSID ssid : bssid.ssids().values()) {
                    if (!ssid.isHumanReadable()) {
                        continue;
                    }

                    database.useHandle(handle -> handle.execute("INSERT INTO beacon_rate_history(bssid, ssid, beacon_rate, created_at) " +
                            "VALUES(?, ?, ?, current_timestamp at time zone 'UTC')",
                            bssid.bssid().toLowerCase(),
                            ssid.name(),
                            ssid.beaconCount.get()
                    ));

                    // Reset internal counter.
                    ssid.beaconCount.set(0);
                }
            }
        } catch(Exception e) {
            LOG.error("Could not write beacon rate information.", e);
        }
    }

    @Override
    public String getName() {
        return "BeaconRateWriter";
    }

}
