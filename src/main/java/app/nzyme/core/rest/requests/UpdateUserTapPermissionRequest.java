package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class UpdateUserTapPermissionRequest {

    public abstract boolean allowAccessAllTenantTaps();
    public abstract List<String> taps();

    @JsonCreator
    public static UpdateUserTapPermissionRequest create(@JsonProperty("allow_access_all_tenant_taps") boolean allowAccessAllTenantTaps,
                                                        @JsonProperty("taps") List<String> taps) {
        return builder()
                .allowAccessAllTenantTaps(allowAccessAllTenantTaps)
                .taps(taps)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateUserTapPermissionRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder allowAccessAllTenantTaps(boolean allowAccessAllTenantTaps);

        public abstract Builder taps(List<String> taps);

        public abstract UpdateUserTapPermissionRequest build();
    }

}
