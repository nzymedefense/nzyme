package app.nzyme.core.assets.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class AssetHostnameEntry {

    public abstract long id();
    public abstract long assetId();
    public abstract UUID uuid();
    public abstract String hostname();
    public abstract String source();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();

    public static AssetHostnameEntry create(long id, long assetId, UUID uuid, String hostname, String source, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .id(id)
                .assetId(assetId)
                .uuid(uuid)
                .hostname(hostname)
                .source(source)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AssetHostnameEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder assetId(long assetId);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder hostname(String hostname);

        public abstract Builder source(String source);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract AssetHostnameEntry build();
    }
}
