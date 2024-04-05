package app.nzyme.core.rest.responses.system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class SidebarTitleResponse {

    @JsonProperty("title")
    public abstract String title();

    @Nullable
    @JsonProperty("subtitle")
    public abstract String subtitle();

    public static SidebarTitleResponse create(String title, String subtitle) {
        return builder()
                .title(title)
                .subtitle(subtitle)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SidebarTitleResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder title(String title);

        public abstract Builder subtitle(String subtitle);

        public abstract SidebarTitleResponse build();
    }
}
