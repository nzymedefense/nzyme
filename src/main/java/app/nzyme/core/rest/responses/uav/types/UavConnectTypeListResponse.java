package app.nzyme.core.rest.responses.uav.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class UavConnectTypeListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("types")
    public abstract List<UavConnectTypeDetailsResponse> types();

    public static UavConnectTypeListResponse create(long count, List<UavConnectTypeDetailsResponse> types) {
        return builder()
                .count(count)
                .types(types)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavConnectTypeListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder types(List<UavConnectTypeDetailsResponse> types);

        public abstract UavConnectTypeListResponse build();
    }
}
