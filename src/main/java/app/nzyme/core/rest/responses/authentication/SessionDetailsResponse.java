package app.nzyme.core.rest.responses.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

@AutoValue
public abstract class SessionDetailsResponse {

    @JsonProperty("user_id")
    public abstract long userId();

    @JsonProperty("remote_ip")
    public abstract String remoteIp();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("last_activity")
    @Nullable
    public abstract DateTime lastActivity();

    public static SessionDetailsResponse create(long userId, String remoteIp, DateTime createdAt, DateTime lastActivity) {
        return builder()
                .userId(userId)
                .remoteIp(remoteIp)
                .createdAt(createdAt)
                .lastActivity(lastActivity)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SessionDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder userId(long userId);

        public abstract Builder remoteIp(String remoteIp);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder lastActivity(DateTime lastActivity);

        public abstract SessionDetailsResponse build();
    }
}
