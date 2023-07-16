package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class ConnectedClientDetails {

    public abstract String clientMac();
    public abstract String bssid();
    public abstract DateTime lastSeen();

    public static ConnectedClientDetails create(String clientMac, String bssid, DateTime lastSeen) {
        return builder()
                .clientMac(clientMac)
                .bssid(bssid)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectedClientDetails.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder clientMac(String clientMac);

        public abstract Builder bssid(String bssid);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract ConnectedClientDetails build();
    }
}
