package app.nzyme.core.rest.responses.uav.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UavConnectTypeDetailsResponse {

    @JsonProperty("type")
    public abstract String type();

    @JsonProperty("model")
    public abstract String model();

    @JsonProperty("serial_type")
    public abstract String serialType();

    @JsonProperty("serial")
    public abstract String serial();

    public static UavConnectTypeDetailsResponse create(String type, String model, String serialType, String serial) {
        return builder()
                .type(type)
                .model(model)
                .serialType(serialType)
                .serial(serial)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavConnectTypeDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder type(String type);

        public abstract Builder model(String model);

        public abstract Builder serialType(String serialType);

        public abstract Builder serial(String serial);

        public abstract UavConnectTypeDetailsResponse build();
    }
}
