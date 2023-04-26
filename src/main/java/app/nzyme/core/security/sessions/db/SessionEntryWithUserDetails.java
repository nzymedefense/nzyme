package app.nzyme.core.security.sessions.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

@AutoValue
public abstract class SessionEntryWithUserDetails {

    public abstract String sessionId();
    public abstract long userId();
    public abstract String remoteIp();
    public abstract DateTime createdAt();

    @Nullable
    public abstract DateTime lastActivity();

    public static SessionEntryWithUserDetails create(String sessionId, long userId, String remoteIp, DateTime createdAt, DateTime lastActivity) {
        return builder()
                .sessionId(sessionId)
                .userId(userId)
                .remoteIp(remoteIp)
                .createdAt(createdAt)
                .lastActivity(lastActivity)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SessionEntryWithUserDetails.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder sessionId(String sessionId);

        public abstract Builder userId(long userId);

        public abstract Builder remoteIp(String remoteIp);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder lastActivity(DateTime lastActivity);

        public abstract SessionEntryWithUserDetails build();
    }
}
