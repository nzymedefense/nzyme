package app.nzyme.core.rest.responses.authentication.mgmt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PermissionDetailsResponse {

    @JsonProperty("id")
    public abstract String id();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("description")
    public abstract String description();

    @JsonProperty("respects_tap_scope")
    public abstract boolean respectsTapScope();

    public static PermissionDetailsResponse create(String id, String name, String description, boolean respectsTapScope) {
        return builder()
                .id(id)
                .name(name)
                .description(description)
                .respectsTapScope(respectsTapScope)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PermissionDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(String id);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder respectsTapScope(boolean respectsTapScope);

        public abstract PermissionDetailsResponse build();
    }
}
