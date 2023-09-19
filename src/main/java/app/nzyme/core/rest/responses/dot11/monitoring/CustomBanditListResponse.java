package app.nzyme.core.rest.responses.dot11.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class CustomBanditListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("bandits")
    public abstract List<CustomBanditDetailsResponse> bandits();

    public static CustomBanditListResponse create(long total, List<CustomBanditDetailsResponse> bandits) {
        return builder()
                .total(total)
                .bandits(bandits)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CustomBanditListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder bandits(List<CustomBanditDetailsResponse> bandits);

        public abstract CustomBanditListResponse build();
    }
}
