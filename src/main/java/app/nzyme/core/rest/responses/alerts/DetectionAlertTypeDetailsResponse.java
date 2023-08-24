package app.nzyme.core.rest.responses.alerts;

import app.nzyme.core.rest.responses.events.SubscriptionDetailsResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class DetectionAlertTypeDetailsResponse {

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("title")
    public abstract String title();

    @JsonProperty("subsystem")
    public abstract String subsystem();

    @JsonProperty("subscriptions")
    public abstract List<SubscriptionDetailsResponse> subscriptions();

    public static DetectionAlertTypeDetailsResponse create(String name, String title, String subsystem, List<SubscriptionDetailsResponse> subscriptions) {
        return builder()
                .name(name)
                .title(title)
                .subsystem(subsystem)
                .subscriptions(subscriptions)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DetectionAlertTypeDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder title(String title);

        public abstract Builder subsystem(String subsystem);

        public abstract Builder subscriptions(List<SubscriptionDetailsResponse> subscriptions);

        public abstract DetectionAlertTypeDetailsResponse build();
    }
}
