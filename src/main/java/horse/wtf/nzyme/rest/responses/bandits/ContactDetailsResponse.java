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

package horse.wtf.nzyme.rest.responses.bandits;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.bandits.engine.ContactRecordAggregation;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class ContactDetailsResponse {

    @JsonProperty
    public abstract UUID uuid();

    @JsonProperty("frame_count")
    public abstract Long frameCount();

    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty("is_active")
    public abstract Boolean isActive();

    @JsonProperty("last_signal")
    public abstract int lastSignal();

    @JsonProperty("bandit_uuid")
    public abstract String banditUUID();

    @JsonProperty("bandit_name")
    public abstract String banditName();

    @JsonProperty("source_role")
    public abstract String sourceRole();

    @JsonProperty("source_name")
    public abstract String sourceName();

    @JsonProperty("ssids")
    public abstract List<ContactRecordAggregation> ssids();

    @JsonProperty("bssids")
    public abstract List<ContactRecordAggregation> bssids();

    @JsonProperty("ssid_frame_count_histograms")
    public abstract Map<String, Map<String, Long>> ssidFrameCountHistograms();

    @JsonProperty("bssid_frame_count_histograms")
    public abstract Map<String, Map<String, Long>> bssidFrameCountHistograms();

    public static ContactDetailsResponse create(UUID uuid, Long frameCount, DateTime firstSeen, DateTime lastSeen, Boolean isActive, int lastSignal, String banditUUID, String banditName, String sourceRole, String sourceName, List<ContactRecordAggregation> ssids, List<ContactRecordAggregation> bssids, Map<String, Map<String, Long>> ssidFrameCountHistograms, Map<String, Map<String, Long>> bssidFrameCountHistograms) {
        return builder()
                .uuid(uuid)
                .frameCount(frameCount)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .isActive(isActive)
                .lastSignal(lastSignal)
                .banditUUID(banditUUID)
                .banditName(banditName)
                .sourceRole(sourceRole)
                .sourceName(sourceName)
                .ssids(ssids)
                .bssids(bssids)
                .ssidFrameCountHistograms(ssidFrameCountHistograms)
                .bssidFrameCountHistograms(bssidFrameCountHistograms)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ContactDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder frameCount(Long frameCount);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder isActive(Boolean isActive);

        public abstract Builder lastSignal(int lastSignal);

        public abstract Builder banditUUID(String banditUUID);

        public abstract Builder banditName(String banditName);

        public abstract Builder sourceRole(String sourceRole);

        public abstract Builder sourceName(String sourceName);

        public abstract Builder ssids(List<ContactRecordAggregation> ssids);

        public abstract Builder bssids(List<ContactRecordAggregation> bssids);

        public abstract Builder ssidFrameCountHistograms(Map<String, Map<String, Long>> ssidFrameCountHistograms);

        public abstract Builder bssidFrameCountHistograms(Map<String, Map<String, Long>> bssidFrameCountHistograms);

        public abstract ContactDetailsResponse build();
    }

}
