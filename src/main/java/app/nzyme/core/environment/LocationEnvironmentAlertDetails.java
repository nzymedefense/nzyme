package app.nzyme.core.environment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class LocationEnvironmentAlertDetails {

    @Nullable
    public abstract String event();

    @Nullable
    public abstract String severity();

    @Nullable
    public abstract String certainty();

    @Nullable
    public abstract String urgency();

    public abstract String headline();

    @Nullable
    public abstract String description();

    @Nullable
    public abstract String senderName();

    @Nullable
    public abstract DateTime effective();

    @Nullable
    public abstract DateTime expires();

    @Nullable
    public abstract DateTime ends();

    @JsonCreator
    public static LocationEnvironmentAlertDetails create(@JsonProperty("event") String event,
                                                         @JsonProperty("severity") String severity,
                                                         @JsonProperty("certainty") String certainty,
                                                         @JsonProperty("urgency") String urgency,
                                                         @JsonProperty("headline") String headline,
                                                         @JsonProperty("description") String description,
                                                         @JsonProperty("sender_name") String senderName,
                                                         @JsonProperty("effective") DateTime effective,
                                                         @JsonProperty("expires") DateTime expires,
                                                         @JsonProperty("ends") DateTime ends) {
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
        return new AutoValue_LocationEnvironmentAlertDetails.Builder();
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

        public abstract LocationEnvironmentAlertDetails build();
    }
}
