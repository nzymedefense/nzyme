package app.nzyme.core.events.actions.webhook;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;

@AutoValue
public abstract class WebhookActionConfiguration {

    @JsonProperty("url")
    public abstract String url();

    @JsonProperty("allow_insecure")
    public abstract boolean allowInsecure();

    @Nullable
    @JsonProperty("bearer_token")
    public abstract String bearerToken();

    @JsonCreator
    public static WebhookActionConfiguration create(@JsonProperty("url") String url,
                                                    @JsonProperty("allow_insecure") boolean allowInsecure,
                                                    @JsonProperty("bearer_token") String bearerToken) {
        return builder()
                .url(url)
                .allowInsecure(allowInsecure)
                .bearerToken(bearerToken)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_WebhookActionConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder url(String url);

        public abstract Builder allowInsecure(boolean allowInsecure);

        public abstract Builder bearerToken(@Null String bearerToken);

        public abstract WebhookActionConfiguration build();
    }
}
