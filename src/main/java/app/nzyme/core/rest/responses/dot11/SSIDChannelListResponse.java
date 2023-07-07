package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class SSIDChannelListResponse {

    @JsonProperty("ssids")
    public abstract List<SSIDChannelDetailsResponse> ssids();

    public static SSIDChannelListResponse create(List<SSIDChannelDetailsResponse> ssids) {
        return builder()
                .ssids(ssids)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSIDChannelListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssids(List<SSIDChannelDetailsResponse> ssids);

        public abstract SSIDChannelListResponse build();
    }
}
