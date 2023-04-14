package app.nzyme.core.rest.responses.authentication.mgmt;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class UsersListResponse {

    public abstract List<UserDetailsResponse> users();

    public static UsersListResponse create(List<UserDetailsResponse> users) {
        return builder()
                .users(users)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UsersListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder users(List<UserDetailsResponse> users);

        public abstract UsersListResponse build();
    }

}
