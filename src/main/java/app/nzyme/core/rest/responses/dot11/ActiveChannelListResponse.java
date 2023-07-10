package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class ActiveChannelListResponse {

    @JsonProperty("channels")
    public abstract List<ActiveChannelDetailsResponse> channels();

    public static ActiveChannelListResponse create(List<ActiveChannelDetailsResponse> channels) {
        return builder()
                .channels(channels)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ActiveChannelListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder channels(List<ActiveChannelDetailsResponse> channels);

        public abstract ActiveChannelListResponse build();
    }
}
