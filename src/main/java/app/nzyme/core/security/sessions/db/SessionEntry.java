package app.nzyme.core.security.sessions.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class SessionEntry {

    public abstract String sessionId();
    public abstract long userId();
    public abstract String remoteIp();
    public abstract DateTime createdAt();

    public static SessionEntry create(String sessionId, long userId, String remoteIp, DateTime createdAt) {
        return builder()
                .sessionId(sessionId)
                .userId(userId)
                .remoteIp(remoteIp)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SessionEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder sessionId(String sessionId);

        public abstract Builder userId(long userId);

        public abstract Builder remoteIp(String remoteIp);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract SessionEntry build();
    }
}
