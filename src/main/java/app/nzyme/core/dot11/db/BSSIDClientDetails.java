package app.nzyme.core.dot11.db;

import app.nzyme.core.rest.responses.dot11.Dot11MacAddressResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;


@AutoValue
public abstract class BSSIDClientDetails {

    @JsonProperty("mac")
    public abstract Dot11MacAddressResponse mac();

    public static BSSIDClientDetails create(Dot11MacAddressResponse mac) {
        return builder()
                .mac(mac)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BSSIDClientDetails.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mac(Dot11MacAddressResponse mac);

        public abstract BSSIDClientDetails build();
    }
}
