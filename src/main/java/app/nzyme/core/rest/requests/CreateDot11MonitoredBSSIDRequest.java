package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.NotEmpty;

@AutoValue
public abstract class CreateDot11MonitoredBSSIDRequest {

    @NotEmpty
    public abstract String bssid();

    @JsonCreator
    public static CreateDot11MonitoredBSSIDRequest create(@JsonProperty("bssid") @NotEmpty String bssid) {
        return builder()
                .bssid(bssid)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateDot11MonitoredBSSIDRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bssid(@NotEmpty String bssid);

        public abstract CreateDot11MonitoredBSSIDRequest build();
    }
}
