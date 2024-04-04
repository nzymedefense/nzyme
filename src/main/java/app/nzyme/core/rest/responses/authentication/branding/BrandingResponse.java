package app.nzyme.core.rest.responses.authentication.branding;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class BrandingResponse {

    @JsonProperty("sidebar_title_text")
    public abstract String sidebarTitleText();

    @Nullable
    @JsonProperty("sidebar_subtitle_text")
    public abstract String sidebarSubtitleText();

    public static BrandingResponse create(String sidebarTitleText, String sidebarSubtitleText) {
        return builder()
                .sidebarTitleText(sidebarTitleText)
                .sidebarSubtitleText(sidebarSubtitleText)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BrandingResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder sidebarTitleText(String sidebarTitleText);

        public abstract Builder sidebarSubtitleText(String sidebarSubtitleText);

        public abstract BrandingResponse build();
    }
}