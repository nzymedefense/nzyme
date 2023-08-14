package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class BSSIDWithTap {

    public abstract String bssid();
    public abstract UUID tapUUID();
    public abstract Float signalStrength();

    public static BSSIDWithTap create(String bssid, UUID tapUUID, Float signalStrength) {
        return builder()
                .bssid(bssid)
                .tapUUID(tapUUID)
                .signalStrength(signalStrength)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BSSIDWithTap.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bssid(String bssid);

        public abstract Builder tapUUID(UUID tapUUID);

        public abstract Builder signalStrength(Float signalStrength);

        public abstract BSSIDWithTap build();
    }
}
