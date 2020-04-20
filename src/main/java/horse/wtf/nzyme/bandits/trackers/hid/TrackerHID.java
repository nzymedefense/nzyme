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

public interface TrackerHID {

    // BUILD TO WHERE YOU CAN RUN IT ON A PI THAT SHOWS RECEIVED PACKETS, SIGNAL STRENGTH AND PARSED PINGS USING LEDS
    // start with local STDOUT HID

    void onConnectionStateChange();
    void onContactStateChange();

    void onContactRSSIChange();
    void onLeaderRSSIChange();

    void onPingFromLeaderReceived(TrackerMessage.Ping ping, int rssi);
    void onPingFromTrackerReceived(TrackerMessage.Ping ping, int rssi);
    void onTrackRequestReceived(TrackerMessage.TrackRequest request);
    void onBanditReceived(TrackerMessage.BanditBroadcast bandit);

}
