package app.nzyme.core.rest.responses.authentication.mgmt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TapPermissionsListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("taps")
    public abstract List<TapPermissionDetailsResponse> taps();

    public static TapPermissionsListResponse create(long count, List<TapPermissionDetailsResponse> taps) {
        return builder()
                .count(count)
                .taps(taps)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapPermissionsListResponse.Builder();
    }
    
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder taps(List<TapPermissionDetailsResponse> taps);

        public abstract TapPermissionsListResponse build();
    }
}
