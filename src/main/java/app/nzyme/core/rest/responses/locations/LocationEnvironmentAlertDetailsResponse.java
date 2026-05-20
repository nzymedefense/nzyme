package app.nzyme.core.rest.responses.locations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class LocationEnvironmentAlertDetailsResponse {

    @Nullable
    @JsonProperty("event")
    public abstract String event();

    @Nullable
    @JsonProperty("severity")
    public abstract String severity();

    @Nullable
    @JsonProperty("certainty")
    public abstract String certainty();

    @Nullable
    @JsonProperty("urgency")
    public abstract String urgency();

    @JsonProperty("headline")
    public abstract String headline();

    @Nullable
    @JsonProperty("description")
    public abstract String description();

    @Nullable
    @JsonProperty("sender_name")
    public abstract String senderName();

    @Nullable
    @JsonProperty("effective")
    public abstract DateTime effective();

    @Nullable
    @JsonProperty("expires")
    public abstract DateTime expires();

    @Nullable
    @JsonProperty("ends")
    public abstract DateTime ends();

    public static LocationEnvironmentAlertDetailsResponse create(String event, String severity, String certainty, String urgency, String headline, String description, String senderName, DateTime effective, DateTime expires, DateTime ends) {
        return builder()
                .event(event)
                .severity(severity)
                .certainty(certainty)
                .urgency(urgency)
                .headline(headline)
                .description(description)
                .senderName(senderName)
                .effective(effective)
                .expires(expires)
                .ends(ends)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_LocationEnvironmentAlertDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder event(String event);

        public abstract Builder severity(String severity);

        public abstract Builder certainty(String certainty);

        public abstract Builder urgency(String urgency);

        public abstract Builder headline(String headline);

        public abstract Builder description(String description);

        public abstract Builder senderName(String senderName);

        public abstract Builder effective(DateTime effective);

        public abstract Builder expires(DateTime expires);

        public abstract Builder ends(DateTime ends);

        public abstract LocationEnvironmentAlertDetailsResponse build();
    }
}
