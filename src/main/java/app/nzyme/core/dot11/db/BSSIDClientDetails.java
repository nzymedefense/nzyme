package app.nzyme.core.dot11.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class BSSIDClientDetails {

    @JsonProperty
    public abstract String mac();

    @JsonProperty
    @Nullable
    public abstract String oui();

    public static BSSIDClientDetails create(String mac, String oui) {
        return builder()
                .mac(mac)
                .oui(oui)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BSSIDClientDetails.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mac(String mac);

        public abstract Builder oui(String oui);

        public abstract BSSIDClientDetails build();
    }
}
