package app.nzyme.core.rest.responses.dot11.monitoring.configimport;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ChannelImportDataResponse {

    @JsonProperty("channel")
    public abstract long channel();

    @JsonProperty("exists")
    public abstract boolean exists();

    public static ChannelImportDataResponse create(long channel, boolean exists) {
        return builder()
                .channel(channel)
                .exists(exists)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ChannelImportDataResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder channel(long channel);

        public abstract Builder exists(boolean exists);

        public abstract ChannelImportDataResponse build();
    }
}
