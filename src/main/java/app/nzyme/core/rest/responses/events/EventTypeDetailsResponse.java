package app.nzyme.core.rest.responses.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class EventTypeDetailsResponse {

    @JsonProperty("id")
    public abstract String id();

    @JsonProperty("category_id")
    public abstract String categoryId();

    @JsonProperty("category_name")
    public abstract String categoryName();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("description")
    public abstract String description();

    @JsonProperty("subscriptions")
    public abstract List<SubscriptionDetailsResponse> subscriptions();

    public static EventTypeDetailsResponse create(String id, String categoryId, String categoryName, String name, String description, List<SubscriptionDetailsResponse> subscriptions) {
        return builder()
                .id(id)
                .categoryId(categoryId)
                .categoryName(categoryName)
                .name(name)
                .description(description)
                .subscriptions(subscriptions)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_EventTypeDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(String id);

        public abstract Builder categoryId(String categoryId);

        public abstract Builder categoryName(String categoryName);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder subscriptions(List<SubscriptionDetailsResponse> subscriptions);

        public abstract EventTypeDetailsResponse build();
    }
}
