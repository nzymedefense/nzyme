package app.nzyme.core.bluetooth.sig;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ConnectServiceUuidResponse {

    public abstract String uuid();
    public abstract String name();
    public abstract String id();

    @JsonCreator
    public static ConnectServiceUuidResponse create(@JsonProperty("uuid") String uuid,
                                                    @JsonProperty("name") String name,
                                                    @JsonProperty("id") String id) {
        return builder()
                .uuid(uuid)
                .name(name)
                .id(id)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectServiceUuidResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(String uuid);

        public abstract Builder name(String name);

        public abstract Builder id(String id);

        public abstract ConnectServiceUuidResponse build();
    }
}
