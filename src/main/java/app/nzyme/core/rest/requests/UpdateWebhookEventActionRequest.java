package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@AutoValue
public abstract class UpdateWebhookEventActionRequest {

    @NotEmpty
    public abstract String name();

    @NotEmpty
    public abstract String description();

    @NotEmpty
    public abstract String url();

    @NotNull
    public abstract boolean allowInsecure();

    @Nullable
    public abstract String bearerToken();

    @JsonCreator
    public static UpdateWebhookEventActionRequest create(@JsonProperty("name") String name,
                                                         @JsonProperty("description") String description,
                                                         @JsonProperty("url") String url,
                                                         @JsonProperty("allow_insecure") boolean allowInsecure,
                                                         @JsonProperty("bearer_token") String bearerToken) {
        return builder()
                .name(name)
                .description(description)
                .url(url)
                .allowInsecure(allowInsecure)
                .bearerToken(bearerToken)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateWebhookEventActionRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(@NotEmpty String name);

        public abstract Builder description(@NotEmpty String description);

        public abstract Builder url(@NotEmpty String url);

        public abstract Builder allowInsecure(@NotNull boolean allowInsecure);

        public abstract Builder bearerToken(String bearerToken);

        public abstract UpdateWebhookEventActionRequest build();
    }
}
