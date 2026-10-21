package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "Body for adding a note to a beacon.")
@AutoValue
public abstract class CreateBeaconNoteRequest {

    @NotEmpty
    public abstract String note();

    @JsonCreator
    public static CreateBeaconNoteRequest create(@Schema(description = "Text of the note.", requiredMode = Schema.RequiredMode.REQUIRED)
                                                 @JsonProperty("note") String note) {
        return builder()
                .note(note)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateBeaconNoteRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder note(String note);

        public abstract CreateBeaconNoteRequest build();
    }
}
