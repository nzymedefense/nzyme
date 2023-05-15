package app.nzyme.core.security.authentication.roles;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Permission {

    public abstract String id();
    public abstract String name();
    public abstract String description();
    public abstract boolean respectsTapScope();

    public static Permission create(String id, String name, String description, boolean respectsTapScope) {
        return builder()
                .id(id)
                .name(name)
                .description(description)
                .respectsTapScope(respectsTapScope)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Permission.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(String id);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder respectsTapScope(boolean respectsTapScope);

        public abstract Permission build();
    }
}
