package app.nzyme.core.rest.resources.system.connect.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class ConnectApiStatusResponse {

    public abstract List<String> providedData();

    @JsonCreator
    public static ConnectApiStatusResponse create(@JsonProperty("provided_data") List<String> providedData) {
        return builder()
                .providedData(providedData)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectApiStatusResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder providedData(List<String> providedData);

        public abstract ConnectApiStatusResponse build();
    }
}
