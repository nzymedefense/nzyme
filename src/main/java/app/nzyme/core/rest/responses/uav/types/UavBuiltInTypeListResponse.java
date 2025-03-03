package app.nzyme.core.rest.responses.uav.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class UavBuiltInTypeListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("types")
    public abstract List<UavBuiltInTypeDetailsResponse> types();

    public static UavBuiltInTypeListResponse create(long count, List<UavBuiltInTypeDetailsResponse> types) {
        return builder()
                .count(count)
                .types(types)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavBuiltInTypeListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder types(List<UavBuiltInTypeDetailsResponse> types);

        public abstract UavBuiltInTypeListResponse build();
    }
}
