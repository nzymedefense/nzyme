package app.nzyme.core.rest.responses.system.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class DataCategorySizesAndConfigurationResponse {

    @Nullable
    @JsonProperty("bytes")
    public abstract Long bytes();

    @JsonProperty("rows")
    public abstract Long rows();

    @JsonProperty("retention_days")
    public abstract Integer retentionDays();

    public static DataCategorySizesAndConfigurationResponse create(Long bytes, Long rows, Integer retentionDays) {
        return builder()
                .bytes(bytes)
                .rows(rows)
                .retentionDays(retentionDays)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DataCategorySizesAndConfigurationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bytes(Long bytes);

        public abstract Builder rows(Long rows);

        public abstract Builder retentionDays(Integer retentionDays);

        public abstract DataCategorySizesAndConfigurationResponse build();
    }
}
