package app.nzyme.core.rest.responses.system.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class DataCategorySizesResponse {

    @Nullable
    @JsonProperty("bytes")
    public abstract Long bytes();

    @JsonProperty("rows")
    public abstract Long rows();

    public static DataCategorySizesResponse create(Long bytes, Long rows) {
        return builder()
                .bytes(bytes)
                .rows(rows)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DataCategorySizesResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bytes(Long bytes);

        public abstract Builder rows(Long rows);

        public abstract DataCategorySizesResponse build();
    }
}
