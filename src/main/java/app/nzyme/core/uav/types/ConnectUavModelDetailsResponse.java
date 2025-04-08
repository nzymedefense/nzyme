package app.nzyme.core.uav.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class ConnectUavModelDetailsResponse {

    public abstract String masterId();
    public abstract String make();
    public abstract String model();
    public abstract String fccId();
    @Nullable
    public abstract String classification();
    public abstract ConnectSerialType serialType();
    public abstract String serial();

    @JsonCreator
    public static ConnectUavModelDetailsResponse create(@JsonProperty("master_id") String masterId,
                                                        @JsonProperty("make") String make,
                                                        @JsonProperty("model") String model,
                                                        @JsonProperty("fcc_id") String fccId,
                                                        @JsonProperty("classification") String classification,
                                                        @JsonProperty("serial_type") ConnectSerialType serialType,
                                                        @JsonProperty("serial") String serial) {
        return builder()
                .masterId(masterId)
                .make(make)
                .model(model)
                .fccId(fccId)
                .classification(classification)
                .serialType(serialType)
                .serial(serial)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectUavModelDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder masterId(String masterId);

        public abstract Builder make(String make);

        public abstract Builder model(String model);

        public abstract Builder fccId(String fccId);

        public abstract Builder classification(String classification);

        public abstract Builder serialType(ConnectSerialType serialType);

        public abstract Builder serial(String serial);

        public abstract ConnectUavModelDetailsResponse build();
    }
}
