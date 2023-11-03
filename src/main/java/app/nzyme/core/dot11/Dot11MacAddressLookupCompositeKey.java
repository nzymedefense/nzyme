package app.nzyme.core.dot11;

import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class Dot11MacAddressLookupCompositeKey {

    public abstract String mac();
    public abstract List<UUID> taps();

    public static Dot11MacAddressLookupCompositeKey create(String mac, List<UUID> taps) {
        return builder()
                .mac(mac)
                .taps(taps)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11MacAddressLookupCompositeKey.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mac(String mac);

        public abstract Builder taps(List<UUID> taps);

        public abstract Dot11MacAddressLookupCompositeKey build();
    }
}
