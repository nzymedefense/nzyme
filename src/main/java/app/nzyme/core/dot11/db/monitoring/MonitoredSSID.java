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
    @Nullable
    public abstract UUID organizationId();
    @Nullable
    public abstract UUID tenantId();
    public abstract boolean statusUnexpectedBSSID();
    public abstract boolean statusUnexpectedChannel();
    public abstract boolean statusUnexpectedSecurity();
    public abstract boolean statusUnexpectedFingerprint();
    public abstract boolean statusUnexpectedSignalTracks();
    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    public static MonitoredSSID create(long id, UUID uuid, boolean isEnabled, String ssid, UUID organizationId, UUID tenantId, boolean statusUnexpectedBSSID, boolean statusUnexpectedChannel, boolean statusUnexpectedSecurity, boolean statusUnexpectedFingerprint, boolean statusUnexpectedSignalTracks, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .id(id)
                .uuid(uuid)
                .isEnabled(isEnabled)
                .ssid(ssid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .statusUnexpectedBSSID(statusUnexpectedBSSID)
                .statusUnexpectedChannel(statusUnexpectedChannel)
                .statusUnexpectedSecurity(statusUnexpectedSecurity)
                .statusUnexpectedFingerprint(statusUnexpectedFingerprint)
                .statusUnexpectedSignalTracks(statusUnexpectedSignalTracks)
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

        public abstract Builder statusUnexpectedBSSID(boolean statusUnexpectedBSSID);

        public abstract Builder statusUnexpectedChannel(boolean statusUnexpectedChannel);

        public abstract Builder statusUnexpectedSecurity(boolean statusUnexpectedSecurity);

        public abstract Builder statusUnexpectedFingerprint(boolean statusUnexpectedFingerprint);

        public abstract Builder statusUnexpectedSignalTracks(boolean statusUnexpectedSignalTracks);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract MonitoredSSID build();
    }
}
