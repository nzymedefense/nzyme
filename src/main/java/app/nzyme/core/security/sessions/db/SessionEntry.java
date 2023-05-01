package app.nzyme.core.security.sessions.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

@AutoValue
public abstract class SessionEntry {

    public abstract String sessionId();
    public abstract long userId();
    public abstract String remoteIp();
    public abstract boolean elevated();
    @Nullable
    public abstract DateTime elevatedSince();
    public abstract boolean mfaValid();
    public abstract DateTime createdAt();
    @Nullable
    public abstract DateTime mfaRequestedAt();

    public static SessionEntry create(String sessionId, long userId, String remoteIp, boolean elevated, DateTime elevatedSince, boolean mfaValid, DateTime createdAt, DateTime mfaRequestedAt) {
        return builder()
                .sessionId(sessionId)
                .userId(userId)
                .remoteIp(remoteIp)
                .elevated(elevated)
                .elevatedSince(elevatedSince)
                .mfaValid(mfaValid)
                .createdAt(createdAt)
                .mfaRequestedAt(mfaRequestedAt)
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

        public abstract Builder elevated(boolean elevated);

        public abstract Builder elevatedSince(DateTime elevatedSince);

        public abstract Builder mfaValid(boolean mfaValid);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder mfaRequestedAt(DateTime mfaRequestedAt);

        public abstract SessionEntry build();
    }
}
