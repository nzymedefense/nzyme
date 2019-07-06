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

import horse.wtf.nzyme.database.Database;
import horse.wtf.nzyme.dot11.networks.BSSID;
import horse.wtf.nzyme.dot11.networks.Channel;
import horse.wtf.nzyme.dot11.networks.Networks;
import horse.wtf.nzyme.dot11.networks.SSID;
import horse.wtf.nzyme.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class SignalIndexWriter extends Periodical {

    private static final Logger LOG = LogManager.getLogger(SignalIndexWriter.class);

    private final Networks networks;
    private final Database database;

    public SignalIndexWriter(Networks networks, Database database) {
        this.networks = networks;
        this.database = database;
    }

    @Override
    protected void execute() {
        try {
            for (Map.Entry<String, BSSID> bssid : networks.getBSSIDs().entrySet()) {
                for (Map.Entry<String, SSID> ssid : bssid.getValue().ssids().entrySet()) {
                    if (!ssid.getValue().isHumanReadable()) {
                        continue;
                    }

                    for (Map.Entry<Integer, Channel> channel : ssid.getValue().channels().entrySet()) {
                        write(
                                bssid.getValue().bssid(),
                                ssid.getValue().name(),
                                channel.getValue().channelNumber(),
                                channel.getValue().signalIndex()
                        );
                    }
                }
            }

        } catch(Exception e) {
            LOG.error("Could not write signal index information.", e);
        }
    }

    private void write(String bssid, String ssid, int channel, float signalIndex) {
        database.useHandle(handle -> {
            handle.execute("INSERT INTO signal_index_history(bssid, ssid, channel, signal_index, created_at) " +
                    "VALUES(?, ?, ?, ?, DATETIME('now'))", bssid.toLowerCase(), ssid, channel, signalIndex);
        });
    }


    @Override
    public String getName() {
        return "SignalIndexWriter";
    }
}
