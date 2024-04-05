package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class UpdateSidebarTitleRequest {

    @JsonProperty("title")
    public abstract String title();

    @JsonProperty("subtitle")
    @Nullable
    public abstract String subtitle();

    @JsonCreator
    public static UpdateSidebarTitleRequest create(@JsonProperty("title") String title,
                                                   @JsonProperty("subtitle") String subtitle) {
        return builder()
                .title(title)
                .subtitle(subtitle)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateSidebarTitleRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder title(String title);

        public abstract Builder subtitle(String subtitle);

        public abstract UpdateSidebarTitleRequest build();
    }
}
