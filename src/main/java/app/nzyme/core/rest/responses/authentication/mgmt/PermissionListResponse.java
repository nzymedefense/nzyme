package app.nzyme.core.rest.responses.authentication.mgmt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class PermissionListResponse {

    @JsonProperty("permissions")
    public abstract List<PermissionDetailsResponse> permissions();

    public static PermissionListResponse create(List<PermissionDetailsResponse> permissions) {
        return builder()
                .permissions(permissions)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PermissionListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder permissions(List<PermissionDetailsResponse> permissions);

        public abstract PermissionListResponse build();
    }
}
