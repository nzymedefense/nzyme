package app.nzyme.core.rest.responses.authentication.mgmt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class QuotaDetailsResponse {

    @JsonProperty("type")
    public abstract String type();

    @JsonProperty("type_human_readable")
    public abstract String typeHumanReadable();

    @Nullable
    @JsonProperty("quota")
    public abstract Integer quota();

    @JsonProperty("use")
    public abstract Integer use();

    public static QuotaDetailsResponse create(String type, String typeHumanReadable, Integer quota, Integer use) {
        return builder()
                .type(type)
                .typeHumanReadable(typeHumanReadable)
                .quota(quota)
                .use(use)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_QuotaDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder type(String type);

        public abstract Builder typeHumanReadable(String typeHumanReadable);

        public abstract Builder quota(Integer quota);

        public abstract Builder use(Integer use);

        public abstract QuotaDetailsResponse build();
    }
}
