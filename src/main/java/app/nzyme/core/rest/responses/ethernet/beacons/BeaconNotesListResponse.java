package app.nzyme.core.rest.responses.ethernet.beacons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class BeaconNotesListResponse {

    @JsonProperty("notes")
    public abstract List<BeaconNoteResponse> notes();

    public static BeaconNotesListResponse create(List<BeaconNoteResponse> notes) {
        return builder()
                .notes(notes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconNotesListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder notes(List<BeaconNoteResponse> notes);
        public abstract BeaconNotesListResponse build();
    }

}
