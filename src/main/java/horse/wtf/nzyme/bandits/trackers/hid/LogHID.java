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

import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.bandits.trackers.TrackerState;
import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class LogHID implements TrackerHID {

    private static final Logger LOG = LogManager.getLogger(LogHID.class);

    @Override
    public void initialize() {
        LOG.info("LOG HID initialized.");
    }

    @Override
    public void onConnectionStateChange(List<TrackerState> connectionState) {
        LOG.info("Connection state changed: {}.", connectionState);
    }

    @Override
    public void onStartTrackingRequestReceived(TrackerMessage.StartTrackRequest request) {
        LOG.info("Received request to start tracking bandit [{}].", request.getUuid());
    }

    @Override
    public void onCancelTrackingRequestReceived(TrackerMessage.CancelTrackRequest request) {
        LOG.info("Received request to abort tracking bandit.");
    }

    @Override
    public void onInitialContactWithTrackedBandit(Bandit bandit) {
        LOG.info("Made initial contact with tracked bandit [{}].", bandit.uuid());
    }

    @Override
    public void onBanditTrace(Bandit bandit, int rssi) {
        LOG.info("Bandit trace at RSSI <{}>.", rssi);
    }

    @Override
    public void onChannelSwitch(int previousChannel, int newChannel) {
        LOG.info("Switching channel from [{}] to [{}].", previousChannel, newChannel);
    }

    @Override
    public void onPingFromLeaderReceived(TrackerMessage.Ping ping, int rssi) {
        LOG.info("Received ping from leader [{}] at RSSI <{}>.", ping.getSource(), rssi);
    }

    @Override
    public void onPingFromTrackerReceived(TrackerMessage.Ping ping, int rssi) {
        LOG.info("Received ping from tracker [{}] at RSSI <{}>.", ping.getSource(), rssi);
    }

    @Override
    public void onBanditReceived(TrackerMessage.BanditBroadcast bandit) {
        LOG.info("Received bandit definition from leader.");
    }

}
