package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class ClientWithOrganizationAndTenant {

    public abstract String macAddress();
    public abstract UUID organizationId();
    public abstract UUID tenantId();
    public abstract DateTime lastSeen();

    public static ClientWithOrganizationAndTenant create(String macAddress, UUID organizationId, UUID tenantId, DateTime lastSeen) {
        return builder()
                .macAddress(macAddress)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ClientWithOrganizationAndTenant.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder macAddress(String macAddress);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract ClientWithOrganizationAndTenant build();
    }
}
