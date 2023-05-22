package app.nzyme.core.rest.responses.taps;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class TapHighLevelInformationDetailsResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("name")
    public abstract String name();

    public static TapHighLevelInformationDetailsResponse create(UUID uuid, String name) {
        return builder()
                .uuid(uuid)
                .name(name)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapHighLevelInformationDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder name(String name);

        public abstract TapHighLevelInformationDetailsResponse build();
    }
}
