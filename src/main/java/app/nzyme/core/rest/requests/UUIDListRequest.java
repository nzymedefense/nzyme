package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class UUIDListRequest {

    public abstract List<UUID> uuids();

    @JsonCreator
    public static UUIDListRequest create(@JsonProperty("uuids") List<UUID> uuids) {
        return builder()
                .uuids(uuids)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UUIDListRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuids(List<UUID> uuids);

        public abstract UUIDListRequest build();
    }
}
