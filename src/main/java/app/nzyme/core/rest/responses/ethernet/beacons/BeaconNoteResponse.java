package app.nzyme.core.rest.responses.ethernet.beacons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class BeaconNoteResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @Nullable
    @JsonProperty("author_name")
    public abstract String authorName();

    @Nullable
    @JsonProperty("author_email")
    public abstract String authorEmail();

    @JsonProperty("note")
    public abstract String note();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    public static BeaconNoteResponse create(UUID uuid, String authorName, String authorEmail, String note, DateTime createdAt) {
        return builder()
                .uuid(uuid)
                .authorName(authorName)
                .authorEmail(authorEmail)
                .note(note)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconNoteResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);
        public abstract Builder authorName(String authorName);
        public abstract Builder authorEmail(String authorEmail);
        public abstract Builder note(String note);
        public abstract Builder createdAt(DateTime createdAt);
        public abstract BeaconNoteResponse build();
    }

}
