package app.nzyme.core.dot11.db.monitoring;

import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class MonitoredSecuritySuite {

    public abstract long monitoredNetworkId();
    public abstract UUID uuid();
    public abstract String securitySuite();

    public static MonitoredSecuritySuite create(long monitoredNetworkId, UUID uuid, String securitySuite) {
        return builder()
                .monitoredNetworkId(monitoredNetworkId)
                .uuid(uuid)
                .securitySuite(securitySuite)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitoredSecuritySuite.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder monitoredNetworkId(long monitoredNetworkId);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder securitySuite(String securitySuite);

        public abstract MonitoredSecuritySuite build();
    }

}
