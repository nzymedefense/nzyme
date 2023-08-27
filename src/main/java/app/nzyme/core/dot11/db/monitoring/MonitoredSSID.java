package app.nzyme.core.dot11.db.monitoring;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.UUID;

@AutoValue
public abstract class MonitoredSSID {

    public abstract long id();
    public abstract UUID uuid();
    public abstract boolean isEnabled();
    public abstract String ssid();
    public abstract UUID organizationId();
    public abstract UUID tenantId();
    public abstract boolean enabledUnexpectedBSSID();
    public abstract boolean enabledUnexpectedChannel();
    public abstract boolean enabledUnexpectedSecuritySuites();
    public abstract boolean enabledUnexpectedFingerprint();
    public abstract boolean enabledUnexpectedSignalTracks();
    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    public static MonitoredSSID create(long id, UUID uuid, boolean isEnabled, String ssid, UUID organizationId, UUID tenantId, boolean enabledUnexpectedBSSID, boolean enabledUnexpectedChannel, boolean enabledUnexpectedSecuritySuites, boolean enabledUnexpectedFingerprint, boolean enabledUnexpectedSignalTracks, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .id(id)
                .uuid(uuid)
                .isEnabled(isEnabled)
                .ssid(ssid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .enabledUnexpectedBSSID(enabledUnexpectedBSSID)
                .enabledUnexpectedChannel(enabledUnexpectedChannel)
                .enabledUnexpectedSecuritySuites(enabledUnexpectedSecuritySuites)
                .enabledUnexpectedFingerprint(enabledUnexpectedFingerprint)
                .enabledUnexpectedSignalTracks(enabledUnexpectedSignalTracks)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitoredSSID.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder isEnabled(boolean isEnabled);

        public abstract Builder ssid(String ssid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder enabledUnexpectedBSSID(boolean enabledUnexpectedBSSID);

        public abstract Builder enabledUnexpectedChannel(boolean enabledUnexpectedChannel);

        public abstract Builder enabledUnexpectedSecuritySuites(boolean enabledUnexpectedSecuritySuites);

        public abstract Builder enabledUnexpectedFingerprint(boolean enabledUnexpectedFingerprint);

        public abstract Builder enabledUnexpectedSignalTracks(boolean enabledUnexpectedSignalTracks);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract MonitoredSSID build();
    }
}
