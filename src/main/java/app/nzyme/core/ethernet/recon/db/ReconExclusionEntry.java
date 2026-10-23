package app.nzyme.core.ethernet.recon.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class ReconExclusionEntry {

    public abstract long id();
    public abstract UUID uuid();
    public abstract UUID organizationId();
    public abstract UUID tenantId();
    public abstract String name();
    @Nullable
    public abstract String description();
    public abstract boolean enabled();
    @Nullable
    public abstract List<String> scanTypes();
    @Nullable
    public abstract String sourceCidr();
    @Nullable
    public abstract String sourceMac();
    @Nullable
    public abstract String targetCidr();
    public abstract long matchCount();
    @Nullable
    public abstract DateTime lastMatchedAt();
    @Nullable
    public abstract UUID createdByUserId();
    @Nullable
    public abstract String createdByName();
    @Nullable
    public abstract String createdByEmail();
    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    public static ReconExclusionEntry create(long id, UUID uuid, UUID organizationId, UUID tenantId, String name, String description, boolean enabled, List<String> scanTypes, String sourceCidr, String sourceMac, String targetCidr, long matchCount, DateTime lastMatchedAt, UUID createdByUserId, String createdByName, String createdByEmail, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .id(id)
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .name(name)
                .description(description)
                .enabled(enabled)
                .scanTypes(scanTypes)
                .sourceCidr(sourceCidr)
                .sourceMac(sourceMac)
                .targetCidr(targetCidr)
                .matchCount(matchCount)
                .lastMatchedAt(lastMatchedAt)
                .createdByUserId(createdByUserId)
                .createdByName(createdByName)
                .createdByEmail(createdByEmail)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ReconExclusionEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);
        public abstract Builder uuid(UUID uuid);
        public abstract Builder organizationId(UUID organizationId);
        public abstract Builder tenantId(UUID tenantId);
        public abstract Builder name(String name);
        public abstract Builder description(String description);
        public abstract Builder enabled(boolean enabled);
        public abstract Builder scanTypes(List<String> scanTypes);
        public abstract Builder sourceCidr(String sourceCidr);
        public abstract Builder sourceMac(String sourceMac);
        public abstract Builder targetCidr(String targetCidr);
        public abstract Builder matchCount(long matchCount);
        public abstract Builder lastMatchedAt(DateTime lastMatchedAt);
        public abstract Builder createdByUserId(UUID createdByUserId);
        public abstract Builder createdByName(String createdByName);
        public abstract Builder createdByEmail(String createdByEmail);
        public abstract Builder createdAt(DateTime createdAt);
        public abstract Builder updatedAt(DateTime updatedAt);
        public abstract ReconExclusionEntry build();
    }
}
