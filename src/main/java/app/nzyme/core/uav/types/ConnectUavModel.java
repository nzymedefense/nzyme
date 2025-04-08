package app.nzyme.core.uav.types;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class ConnectUavModel {

    public abstract String masterId();
    public abstract String make();
    public abstract String model();
    public abstract String fccId();
    @Nullable
    public abstract String classification();
    public abstract ConnectSerialType serialType();
    public abstract String serial();

    public static ConnectUavModel create(String masterId, String make, String model, String fccId, String classification, ConnectSerialType serialType, String serial) {
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
        return new AutoValue_ConnectUavModel.Builder();
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

        public abstract ConnectUavModel build();
    }
}
