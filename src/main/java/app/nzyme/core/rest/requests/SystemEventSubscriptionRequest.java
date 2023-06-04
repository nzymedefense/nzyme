package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class SystemEventSubscriptionRequest {

    public abstract UUID actionId();

    @JsonCreator
    public static SystemEventSubscriptionRequest create(@JsonProperty("action_id") UUID actionId) {
        return builder()
                .actionId(actionId)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SystemEventSubscriptionRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder actionId(UUID actionId);

        public abstract SystemEventSubscriptionRequest build();
    }
}
