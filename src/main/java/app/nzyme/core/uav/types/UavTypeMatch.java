package app.nzyme.core.uav.types;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class UavTypeMatch {

    public abstract String type();
    @Nullable
    public abstract String name();
    @Nullable
    public abstract String model();
    @Nullable
    public abstract String defaultClassification();

    public static UavTypeMatch create(String type, String name, String model, String defaultClassification) {
        return builder()
                .type(type)
                .name(name)
                .model(model)
                .defaultClassification(defaultClassification)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavTypeMatch.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder type(String type);

        public abstract Builder name(String name);

        public abstract Builder model(String model);

        public abstract Builder defaultClassification(String defaultClassification);

        public abstract UavTypeMatch build();
    }
}
