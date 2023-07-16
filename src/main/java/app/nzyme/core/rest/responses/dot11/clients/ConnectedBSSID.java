package app.nzyme.core.rest.responses.dot11.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class ConnectedBSSID {

    @JsonProperty("bssid")
    public abstract String bssid();

    @JsonProperty("oui")
    @Nullable
    public abstract String oui();

    @JsonProperty("possible_ssids")
    public abstract List<String> ssids();

    public static ConnectedBSSID create(String bssid, String oui, List<String> ssids) {
        return builder()
                .bssid(bssid)
                .oui(oui)
                .ssids(ssids)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectedBSSID.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bssid(String bssid);

        public abstract Builder oui(String oui);

        public abstract Builder ssids(List<String> ssids);

        public abstract ConnectedBSSID build();
    }
}
