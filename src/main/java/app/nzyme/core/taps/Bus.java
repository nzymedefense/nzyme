package app.nzyme.core.taps;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class Bus {

    public abstract Long id();
    public abstract String name();
    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    public static Bus create(Long id, String name, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .id(id)
                .name(name)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Bus.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(Long id);

        public abstract Builder name(String name);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Bus build();
    }

}
