package app.nzyme.core.configuration.node;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class MiscConfiguration {

    @Nullable
    public abstract String customTitle();

    @Nullable
    public abstract String customFaviconUrl();

    public static MiscConfiguration create(String customTitle, String customFaviconUrl) {
        return builder()
                .customTitle(customTitle)
                .customFaviconUrl(customFaviconUrl)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MiscConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder customTitle(String customTitle);

        public abstract Builder customFaviconUrl(String customFaviconUrl);

        public abstract MiscConfiguration build();
    }

}
