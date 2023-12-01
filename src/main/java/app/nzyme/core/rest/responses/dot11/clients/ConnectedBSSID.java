package app.nzyme.core.rest.responses.dot11.clients;

import app.nzyme.core.rest.responses.dot11.Dot11MacAddressResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class ConnectedBSSID {

    @JsonProperty("mac")
    public abstract Dot11MacAddressResponse mac();

    @JsonProperty("possible_ssids")
    public abstract List<String> ssids();

    public static ConnectedBSSID create(Dot11MacAddressResponse mac, List<String> ssids) {
        return builder()
                .mac(mac)
                .ssids(ssids)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectedBSSID.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mac(Dot11MacAddressResponse mac);

        public abstract Builder ssids(List<String> ssids);

        public abstract ConnectedBSSID build();
    }
}
