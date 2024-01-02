package app.nzyme.core.rest.responses.dot11.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class RestrictedSSIDSubstringDetailsResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("substring")
    public abstract String substring();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    public static RestrictedSSIDSubstringDetailsResponse create(UUID uuid, String substring, DateTime createdAt) {
        return builder()
                .uuid(uuid)
                .substring(substring)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_RestrictedSSIDSubstringDetailsResponse.Builder();
    }
    
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder substring(String substring);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract RestrictedSSIDSubstringDetailsResponse build();
    }
}
