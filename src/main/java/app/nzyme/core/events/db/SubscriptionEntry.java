package app.nzyme.core.events.db;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.UUID;

@AutoValue
public abstract class SubscriptionEntry {

    public abstract UUID uuid();
    public abstract UUID actionId();
    public abstract String reference();

    @Nullable
    public abstract UUID organizationId();

    public static SubscriptionEntry create(UUID uuid, UUID actionId, String reference, UUID organizationId) {
        return builder()
                .uuid(uuid)
                .actionId(actionId)
                .reference(reference)
                .organizationId(organizationId)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SubscriptionEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder actionId(UUID actionId);

        public abstract Builder reference(String reference);

        public abstract Builder organizationId(UUID organizationId);

        public abstract SubscriptionEntry build();
    }
}
