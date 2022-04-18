package horse.wtf.nzyme.configuration.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class BaseConfiguration {

    public abstract String tapSecret();
    public abstract DateTime updatedAt();

    public static BaseConfiguration create(String tapSecret, DateTime updatedAt) {
        return builder()
                .tapSecret(tapSecret)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BaseConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tapSecret(String tapSecret);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract BaseConfiguration build();
    }

}
