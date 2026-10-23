package app.nzyme.core.rest.responses.ethernet.recon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class ReconExclusionResponse {

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
    @JsonProperty("scan_types")
    public abstract List<String> scanTypes();

    @Nullable
    @JsonProperty("source_cidr")
    public abstract String sourceCidr();

    @Nullable
    @JsonProperty("source_mac")
    public abstract String sourceMac();

    @Nullable
    @JsonProperty("target_cidr")
    public abstract String targetCidr();

    @JsonProperty("match_count")
    public abstract long matchCount();

    @Nullable
    @JsonProperty("last_matched_at")
    public abstract DateTime lastMatchedAt();

    @Nullable
    @JsonProperty("impact")
    public abstract ReconExclusionImpactResponse impact();

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

    public static ReconExclusionResponse create(UUID uuid, String name, String description, boolean enabled, List<String> scanTypes, String sourceCidr, String sourceMac, String targetCidr, long matchCount, DateTime lastMatchedAt, ReconExclusionImpactResponse impact, String createdByName, String createdByEmail, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .uuid(uuid)
                .name(name)
                .description(description)
                .enabled(enabled)
                .scanTypes(scanTypes)
                .sourceCidr(sourceCidr)
                .sourceMac(sourceMac)
                .targetCidr(targetCidr)
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
        return new AutoValue_ReconExclusionResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);
        public abstract Builder name(String name);
        public abstract Builder description(String description);
        public abstract Builder enabled(boolean enabled);
        public abstract Builder scanTypes(List<String> scanTypes);
        public abstract Builder sourceCidr(String sourceCidr);
        public abstract Builder sourceMac(String sourceMac);
        public abstract Builder targetCidr(String targetCidr);
        public abstract Builder matchCount(long matchCount);
        public abstract Builder lastMatchedAt(DateTime lastMatchedAt);
        public abstract Builder impact(ReconExclusionImpactResponse impact);
        public abstract Builder createdByName(String createdByName);
        public abstract Builder createdByEmail(String createdByEmail);
        public abstract Builder createdAt(DateTime createdAt);
        public abstract Builder updatedAt(DateTime updatedAt);
        public abstract ReconExclusionResponse build();
    }
}
