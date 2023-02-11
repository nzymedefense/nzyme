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

package app.nzyme.core.bandits.trackers.hid;

import app.nzyme.core.bandits.Bandit;
import app.nzyme.core.bandits.trackers.TrackerState;
import app.nzyme.core.bandits.trackers.protobuf.TrackerMessage;

import java.util.List;

public interface TrackerHID {

    enum TYPE {
        LOG,
        TEXTGUI,
        WEB
    }

    void initialize();

    void onConnectionStateChange(List<TrackerState> connectionState);

    void onPingFromLeaderReceived(TrackerMessage.Ping ping, int rssi);
    void onPingFromTrackerReceived(TrackerMessage.Ping ping, int rssi);

    void onStartTrackingRequestReceived(TrackerMessage.StartTrackRequest request);
    void onCancelTrackingRequestReceived(TrackerMessage.CancelTrackRequest request);

    void onInitialContactWithTrackedBandit(Bandit bandit);
    void onBanditTrace(Bandit bandit, int rssi);

    void onChannelSwitch(int previousChannel, int newChannel);

}
