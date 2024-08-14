package app.nzyme.core.configuration.node;

import com.google.auto.value.AutoValue;

import java.util.Optional;

@AutoValue
public abstract class ProtocolsConfiguration {

    public abstract Optional<TcpConfiguration> tcp();

    public static ProtocolsConfiguration create(Optional<TcpConfiguration> tcp) {
        return builder()
                .tcp(tcp)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ProtocolsConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tcp(Optional<TcpConfiguration> tcp);

        public abstract ProtocolsConfiguration build();
    }
}
