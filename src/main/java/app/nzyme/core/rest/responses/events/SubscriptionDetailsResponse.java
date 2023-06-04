package app.nzyme.core.rest.responses.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class SubscriptionDetailsResponse {

    @JsonProperty("action_id")
    public abstract UUID actionId();

    @JsonProperty("action_name")
    public abstract String actionName();

    public static SubscriptionDetailsResponse create(UUID actionId, String actionName) {
        return builder()
                .actionId(actionId)
                .actionName(actionName)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SubscriptionDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder actionId(UUID actionId);

        public abstract Builder actionName(String actionName);

        public abstract SubscriptionDetailsResponse build();
    }

}
