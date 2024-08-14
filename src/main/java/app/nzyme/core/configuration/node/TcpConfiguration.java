package app.nzyme.core.configuration.node;

import com.google.auto.value.AutoValue;

import java.util.Optional;

@AutoValue
public abstract class TcpConfiguration {

    public abstract Optional<Integer> sessionTimeoutSeconds();

    public static TcpConfiguration create(Optional<Integer> sessionTimeoutSeconds) {
        return builder()
                .sessionTimeoutSeconds(sessionTimeoutSeconds)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TcpConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder sessionTimeoutSeconds(Optional<Integer> sessionTimeoutSeconds);

        public abstract TcpConfiguration build();
    }
}
