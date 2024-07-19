package app.nzyme.core.ouis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class OuiConnectResponse {

    public abstract Map<String, String> ouis();

    @JsonCreator
    public static OuiConnectResponse create(@JsonProperty("ouis") Map<String, String> ouis) {
        return builder()
                .ouis(ouis)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_OuiConnectResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ouis(Map<String, String> ouis);

        public abstract OuiConnectResponse build();
    }
}
