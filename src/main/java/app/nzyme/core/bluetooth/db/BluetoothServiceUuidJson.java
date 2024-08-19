package app.nzyme.core.bluetooth.db;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class BluetoothServiceUuidJson {

    @JsonProperty("uuid")
    public abstract String uuid();

    @JsonProperty("name")
    @Nullable
    public abstract String name();

    @JsonCreator
    public static BluetoothServiceUuidJson create(@JsonProperty("uuid") String uuid,
                                                  @JsonProperty("name") String name) {
        return builder()
                .uuid(uuid)
                .name(name)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BluetoothServiceUuidJson.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(String uuid);

        public abstract Builder name(String name);

        public abstract BluetoothServiceUuidJson build();
    }
}
