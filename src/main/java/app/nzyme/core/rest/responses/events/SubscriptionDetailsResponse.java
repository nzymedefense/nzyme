package app.nzyme.core.rest.responses.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class SubscriptionDetailsResponse {

    @JsonProperty("subscription_id")
    public abstract UUID subscriptionId();

    @JsonProperty("action_id")
    public abstract UUID actionId();

    @JsonProperty("action_type")
    public abstract String actionType();

    @JsonProperty("action_type_human_readable")
    public abstract String actionTypeHumanReadable();

    @JsonProperty("action_name")
    public abstract String actionName();

    public static SubscriptionDetailsResponse create(UUID subscriptionId, UUID actionId, String actionType, String actionTypeHumanReadable, String actionName) {
        return builder()
                .subscriptionId(subscriptionId)
                .actionId(actionId)
                .actionType(actionType)
                .actionTypeHumanReadable(actionTypeHumanReadable)
                .actionName(actionName)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SubscriptionDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder subscriptionId(UUID subscriptionId);

        public abstract Builder actionId(UUID actionId);

        public abstract Builder actionType(String actionType);

        public abstract Builder actionTypeHumanReadable(String actionTypeHumanReadable);

        public abstract Builder actionName(String actionName);

        public abstract SubscriptionDetailsResponse build();
    }
}
