package app.nzyme.core.rest.responses.ethernet.beacons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class BeaconExclusionResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("name")
    public abstract String name();

    @Nullable
    @JsonProperty("description")
    public abstract String description();

    @JsonProperty("enabled")
    public abstract boolean enabled();

    @Nullable
    @JsonProperty("destination_cidr")
    public abstract String destinationCidr();

    @Nullable
    @JsonProperty("destination_host")
    public abstract String destinationHost();

    @Nullable
    @JsonProperty("destination_ports")
    public abstract String destinationPorts();

    @Nullable
    @JsonProperty("beacon_types")
    public abstract List<String> beaconTypes();

    @Nullable
    @JsonProperty("source_cidr")
    public abstract String sourceCidr();

    @Nullable
    @JsonProperty("source_mac")
    public abstract String sourceMac();

    @JsonProperty("match_count")
    public abstract long matchCount();

    @Nullable
    @JsonProperty("last_matched_at")
    public abstract DateTime lastMatchedAt();

    @Nullable
    @JsonProperty("impact")
    public abstract BeaconExclusionImpactResponse impact();

    @Nullable
    @JsonProperty("created_by_name")
    public abstract String createdByName();

    @Nullable
    @JsonProperty("created_by_email")
    public abstract String createdByEmail();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    public static BeaconExclusionResponse create(UUID uuid, String name, String description, boolean enabled, String destinationCidr, String destinationHost, String destinationPorts, List<String> beaconTypes, String sourceCidr, String sourceMac, long matchCount, DateTime lastMatchedAt, BeaconExclusionImpactResponse impact, String createdByName, String createdByEmail, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .uuid(uuid)
                .name(name)
                .description(description)
                .enabled(enabled)
                .destinationCidr(destinationCidr)
                .destinationHost(destinationHost)
                .destinationPorts(destinationPorts)
                .beaconTypes(beaconTypes)
                .sourceCidr(sourceCidr)
                .sourceMac(sourceMac)
                .matchCount(matchCount)
                .lastMatchedAt(lastMatchedAt)
                .impact(impact)
                .createdByName(createdByName)
                .createdByEmail(createdByEmail)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconExclusionResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);
        public abstract Builder name(String name);
        public abstract Builder description(String description);
        public abstract Builder enabled(boolean enabled);
        public abstract Builder destinationCidr(String destinationCidr);
        public abstract Builder destinationHost(String destinationHost);
        public abstract Builder destinationPorts(String destinationPorts);
        public abstract Builder beaconTypes(List<String> beaconTypes);
        public abstract Builder sourceCidr(String sourceCidr);
        public abstract Builder sourceMac(String sourceMac);
        public abstract Builder matchCount(long matchCount);
        public abstract Builder lastMatchedAt(DateTime lastMatchedAt);
        public abstract Builder impact(BeaconExclusionImpactResponse impact);
        public abstract Builder createdByName(String createdByName);
        public abstract Builder createdByEmail(String createdByEmail);
        public abstract Builder createdAt(DateTime createdAt);
        public abstract Builder updatedAt(DateTime updatedAt);
        public abstract BeaconExclusionResponse build();
    }

}
