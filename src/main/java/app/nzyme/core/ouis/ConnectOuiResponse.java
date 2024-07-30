package app.nzyme.core.ouis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class ConnectOuiResponse {

    public abstract Map<String, String> ouis();

    @JsonCreator
    public static ConnectOuiResponse create(@JsonProperty("ouis") Map<String, String> ouis) {
        return builder()
                .ouis(ouis)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectOuiResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ouis(Map<String, String> ouis);

        public abstract ConnectOuiResponse build();
    }
}
