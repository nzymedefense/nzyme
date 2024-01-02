package app.nzyme.core.dot11.db.monitoring;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class RestrictedSSIDSubstring {

    public abstract long id();
    public abstract UUID uuid();
    public abstract String substring();
    public abstract DateTime createdAt();

    public static RestrictedSSIDSubstring create(long id, UUID uuid, String substring, DateTime createdAt) {
        return builder()
                .id(id)
                .uuid(uuid)
                .substring(substring)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_RestrictedSSIDSubstring.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder substring(String substring);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract RestrictedSSIDSubstring build();
    }
}
