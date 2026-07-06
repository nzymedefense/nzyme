package app.nzyme.core.rest.responses.gnss;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class GNSSTapDetailsResponse {

    @JsonProperty("id")
    public abstract UUID uuid();

    @JsonProperty("name")
    public abstract String name();

    // ADD constellation status, jam values etc
    // add online/offline (based on capture updated_at)

    public static GNSSTapDetailsResponse create(UUID uuid, String name) {
        return builder()
                .uuid(uuid)
                .name(name)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSTapDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder name(String name);

        public abstract GNSSTapDetailsResponse build();
    }
}
