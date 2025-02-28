package app.nzyme.core.rest.responses.uav.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class UavCustomTypeListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("types")
    public abstract List<UavCustomTypeDetailsResponse> types();

    public static UavCustomTypeListResponse create(long count, List<UavCustomTypeDetailsResponse> types) {
        return builder()
                .count(count)
                .types(types)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavCustomTypeListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder types(List<UavCustomTypeDetailsResponse> types);

        public abstract UavCustomTypeListResponse build();
    }
}
