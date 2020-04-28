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

package horse.wtf.nzyme.bandits.trackers.hid;

import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class RPIWiringPiHID implements TrackerHID {

    private static final Logger LOG = LogManager.getLogger(RPIWiringPiHID.class);

    @Override
    public void initialize() {
        try {
            Runtime.getRuntime().exec("/usr/bin/gpio mode 0 out");
            Runtime.getRuntime().exec("/usr/bin/gpio write 0 1");
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    @Override
    public void onConnectionStateChange() {

    }

    @Override
    public void onContactStateChange() {

    }

    @Override
    public void onContactRSSIChange() {

    }

    @Override
    public void onLeaderRSSIChange() {

    }

    @Override
    public void onPingFromLeaderReceived(TrackerMessage.Ping ping, int rssi) {
        try {
            Runtime.getRuntime().exec("/usr/bin/gpio write 0 0");
            Runtime.getRuntime().exec("/usr/bin/gpio write 0 1");
            Thread.sleep(250);
            Runtime.getRuntime().exec("/usr/bin/gpio write 0 0");
        } catch (IOException | InterruptedException e) {
            LOG.error(e);
        }
    }

    @Override
    public void onPingFromTrackerReceived(TrackerMessage.Ping ping, int rssi) {

    }

    @Override
    public void onTrackRequestReceived(TrackerMessage.TrackRequest request) {

    }

    @Override
    public void onBanditReceived(TrackerMessage.BanditBroadcast bandit) {

    }

}
