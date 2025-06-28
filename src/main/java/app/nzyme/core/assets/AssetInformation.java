package app.nzyme.core.assets;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class AssetInformation {

    public abstract String mac();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();

    public static AssetInformation create(String mac, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .mac(mac)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AssetInformation.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mac(String mac);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract AssetInformation build();
    }
}
