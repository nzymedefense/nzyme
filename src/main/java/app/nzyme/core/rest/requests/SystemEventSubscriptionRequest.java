package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.UUID;

@AutoValue
public abstract class SystemEventSubscriptionRequest {

    public abstract UUID actionId();

    @Nullable
    public abstract UUID organizationId();

    @JsonCreator
    public static SystemEventSubscriptionRequest create(@JsonProperty("action_id") UUID actionId, @Nullable @JsonProperty("organization_id") UUID organizationId) {
        return builder()
                .actionId(actionId)
                .organizationId(organizationId)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SystemEventSubscriptionRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder actionId(UUID actionId);

        public abstract Builder organizationId(UUID organizationId);

        public abstract SystemEventSubscriptionRequest build();
    }
}
