package app.nzyme.core.rest.responses.ethernet.beacons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import app.nzyme.core.rest.responses.ethernet.L4AddressResponse;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class BeaconSourceResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("source_address")
    public abstract String sourceAddress();

    @Nullable
    @JsonProperty("address")
    public abstract L4AddressResponse address();

    @Nullable
    @JsonProperty("source_mac")
    public abstract String sourceMac();

    @Nullable
    @JsonProperty("source_mac_oui")
    public abstract String sourceMacOui();

    @Nullable
    @JsonProperty("asset_uuid")
    public abstract UUID assetUuid();

    @Nullable
    @JsonProperty("asset_name")
    public abstract String assetName();

    @JsonProperty("checkin_count")
    public abstract long checkinCount();

    @JsonProperty("interval_seconds")
    public abstract double intervalSeconds();

    @JsonProperty("interval_jitter")
    public abstract double intervalJitter();

    @JsonProperty("regularity")
    public abstract double regularity();

    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    public static BeaconSourceResponse create(UUID uuid, String sourceAddress, L4AddressResponse address, String sourceMac, String sourceMacOui, UUID assetUuid, String assetName, long checkinCount, double intervalSeconds, double intervalJitter, double regularity, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .uuid(uuid)
                .sourceAddress(sourceAddress)
                .address(address)
                .sourceMac(sourceMac)
                .sourceMacOui(sourceMacOui)
                .assetUuid(assetUuid)
                .assetName(assetName)
                .checkinCount(checkinCount)
                .intervalSeconds(intervalSeconds)
                .intervalJitter(intervalJitter)
                .regularity(regularity)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconSourceResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder sourceAddress(String sourceAddress);

        public abstract Builder address(L4AddressResponse address);

        public abstract Builder sourceMac(String sourceMac);

        public abstract Builder sourceMacOui(String sourceMacOui);

        public abstract Builder assetUuid(UUID assetUuid);

        public abstract Builder assetName(String assetName);

        public abstract Builder checkinCount(long checkinCount);

        public abstract Builder intervalSeconds(double intervalSeconds);

        public abstract Builder intervalJitter(double intervalJitter);

        public abstract Builder regularity(double regularity);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract BeaconSourceResponse build();
    }
}
