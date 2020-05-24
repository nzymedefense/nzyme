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

import java.util.List;

public interface TrackerHID {

    enum ChannelWidthChangeDirection {
        WIDE, NARROW
    }

    void initialize();

    void onConnectionStateChange(List<TrackerState> connectionState);

    void onPingFromLeaderReceived(TrackerMessage.Ping ping, int rssi);
    void onPingFromTrackerReceived(TrackerMessage.Ping ping, int rssi);
    void onBanditReceived(TrackerMessage.BanditBroadcast bandit);

    void onStartTrackingRequestReceived(TrackerMessage.StartTrackRequest request);
    void onCancelTrackingRequestReceived(TrackerMessage.CancelTrackRequest request);

    // TODO implement
    void onInitialContactWithTrackedBandit(Bandit bandit);
    void onBanditTrace(int rssi);
    void onChannelSwitch(int newChannel);
    void onChannelWidthChange(ChannelWidthChangeDirection direction, List<Integer> activeChannels);

    void onContactRequestReceived();

}
