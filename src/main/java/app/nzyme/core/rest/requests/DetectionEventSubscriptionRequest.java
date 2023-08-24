package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class DetectionEventSubscriptionRequest {

    public abstract UUID actionId();
    public abstract UUID organizationId();

    @JsonCreator
    public static DetectionEventSubscriptionRequest create(@JsonProperty("action_id") UUID actionId, @JsonProperty("organization_id") UUID organizationId) {
        return builder()
                .actionId(actionId)
                .organizationId(organizationId)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DetectionEventSubscriptionRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder actionId(UUID actionId);

        public abstract Builder organizationId(UUID organizationId);

        public abstract DetectionEventSubscriptionRequest build();
    }
}
