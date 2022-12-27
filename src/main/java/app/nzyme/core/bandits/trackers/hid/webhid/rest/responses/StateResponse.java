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

package app.nzyme.core.bandits.trackers.hid.webhid.rest.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import app.nzyme.core.bandits.trackers.TrackerState;
import app.nzyme.core.bandits.trackers.hid.webhid.WebHID;
import app.nzyme.core.bandits.trackers.trackerlogic.ChannelDesignator;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class StateResponse {

    @JsonProperty("clock")
    public abstract DateTime clock();

    @JsonProperty("tracker_state")
    public abstract List<TrackerState> trackerState();

    @JsonProperty("leader_signal_strength")
    public abstract int leaderSignalStrength();

    @JsonProperty("tracker_device_live")
    public abstract boolean trackerDeviceLive();

    @JsonProperty("monitors_live")
    public abstract boolean dot11MonitorsLive();

    @JsonProperty("channels")
    public abstract List<Integer> dot11Channels();

    @JsonProperty("channel_designation_status")
    public abstract ChannelDesignator.DESIGNATION_STATUS channelDesignationStatus();

    @JsonProperty("is_tracking")
    public abstract boolean isTracking();

    @JsonProperty("tracking_target")
    @Nullable
    public abstract String trackingTarget();

    @JsonProperty("track")
    @Nullable
    public abstract String track();

    @JsonProperty("track_frames")
    public abstract long trackFrames();

    @JsonProperty("track_contact")
    @Nullable
    public abstract DateTime trackContact();

    @JsonProperty("bandit_signal")
    @Nullable
    public abstract Long banditSignal();

    @JsonProperty("events")
    public abstract List<WebHID.Event> events();

    public static StateResponse create(DateTime clock, List<TrackerState> trackerState, int leaderSignalStrength, boolean trackerDeviceLive, boolean dot11MonitorsLive, List<Integer> dot11Channels, ChannelDesignator.DESIGNATION_STATUS channelDesignationStatus, boolean isTracking, String trackingTarget, String track, long trackFrames, DateTime trackContact, Long banditSignal, List<WebHID.Event> events) {
        return builder()
                .clock(clock)
                .trackerState(trackerState)
                .leaderSignalStrength(leaderSignalStrength)
                .trackerDeviceLive(trackerDeviceLive)
                .dot11MonitorsLive(dot11MonitorsLive)
                .dot11Channels(dot11Channels)
                .channelDesignationStatus(channelDesignationStatus)
                .isTracking(isTracking)
                .trackingTarget(trackingTarget)
                .track(track)
                .trackFrames(trackFrames)
                .trackContact(trackContact)
                .banditSignal(banditSignal)
                .events(events)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_StateResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder clock(DateTime clock);

        public abstract Builder trackerState(List<TrackerState> trackerState);

        public abstract Builder leaderSignalStrength(int leaderSignalStrength);

        public abstract Builder trackerDeviceLive(boolean trackerDeviceLive);

        public abstract Builder dot11MonitorsLive(boolean dot11MonitorsLive);

        public abstract Builder dot11Channels(List<Integer> dot11Channels);

        public abstract Builder channelDesignationStatus(ChannelDesignator.DESIGNATION_STATUS channelDesignationStatus);

        public abstract Builder isTracking(boolean isTracking);

        public abstract Builder trackingTarget(String trackingTarget);

        public abstract Builder track(String track);

        public abstract Builder trackFrames(long trackFrames);

        public abstract Builder trackContact(DateTime trackContact);

        public abstract Builder banditSignal(Long banditSignal);

        public abstract Builder events(List<WebHID.Event> events);

        public abstract StateResponse build();
    }

}
