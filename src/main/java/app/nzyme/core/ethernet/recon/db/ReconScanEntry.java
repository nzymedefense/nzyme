package app.nzyme.core.ethernet.recon.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class ReconScanEntry {

    public abstract long id();
    public abstract UUID uuid();
    public abstract UUID organizationId();
    public abstract UUID tenantId();
    public abstract String type();
    @Nullable
    public abstract String sourceAddress();
    @Nullable
    public abstract String sourceMac();
    @Nullable
    public abstract String targetAddress();
    public abstract int hostCount();
    public abstract int portCount();
    public abstract long probeCount();
    public abstract long openCount();
    public abstract long closedCount();
    public abstract long unansweredCount();
    public abstract boolean targetsTruncated();
    @Nullable
    public abstract String enrichment();
    public abstract List<UUID> tapUuids();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();
    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    public static ReconScanEntry create(long id, UUID uuid, UUID organizationId, UUID tenantId, String type, String sourceAddress, String sourceMac, String targetAddress, int hostCount, int portCount, long probeCount, long openCount, long closedCount, long unansweredCount, boolean targetsTruncated, String enrichment, List<UUID> tapUuids, DateTime firstSeen, DateTime lastSeen, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .id(id)
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .type(type)
                .sourceAddress(sourceAddress)
                .sourceMac(sourceMac)
                .targetAddress(targetAddress)
                .hostCount(hostCount)
                .portCount(portCount)
                .probeCount(probeCount)
                .openCount(openCount)
                .closedCount(closedCount)
                .unansweredCount(unansweredCount)
                .targetsTruncated(targetsTruncated)
                .enrichment(enrichment)
                .tapUuids(tapUuids)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ReconScanEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);
        public abstract Builder uuid(UUID uuid);
        public abstract Builder organizationId(UUID organizationId);
        public abstract Builder tenantId(UUID tenantId);
        public abstract Builder type(String type);
        public abstract Builder sourceAddress(String sourceAddress);
        public abstract Builder sourceMac(String sourceMac);
        public abstract Builder targetAddress(String targetAddress);
        public abstract Builder hostCount(int hostCount);
        public abstract Builder portCount(int portCount);
        public abstract Builder probeCount(long probeCount);
        public abstract Builder openCount(long openCount);
        public abstract Builder closedCount(long closedCount);
        public abstract Builder unansweredCount(long unansweredCount);
        public abstract Builder targetsTruncated(boolean targetsTruncated);
        public abstract Builder enrichment(String enrichment);
        public abstract Builder tapUuids(List<UUID> tapUuids);
        public abstract Builder firstSeen(DateTime firstSeen);
        public abstract Builder lastSeen(DateTime lastSeen);
        public abstract Builder createdAt(DateTime createdAt);
        public abstract Builder updatedAt(DateTime updatedAt);
        public abstract ReconScanEntry build();
    }
}
