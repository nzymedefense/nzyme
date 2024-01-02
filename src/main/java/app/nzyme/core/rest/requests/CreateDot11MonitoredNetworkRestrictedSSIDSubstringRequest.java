package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.NotEmpty;

@AutoValue
public abstract class CreateDot11MonitoredNetworkRestrictedSSIDSubstringRequest {

    @NotEmpty
    public abstract String substring();

    @JsonCreator
    public static CreateDot11MonitoredNetworkRestrictedSSIDSubstringRequest create(@JsonProperty("substring") String substring) {
        return builder()
                .substring(substring)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateDot11MonitoredNetworkRestrictedSSIDSubstringRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder substring(@NotEmpty String substring);

        public abstract CreateDot11MonitoredNetworkRestrictedSSIDSubstringRequest build();
    }
}
