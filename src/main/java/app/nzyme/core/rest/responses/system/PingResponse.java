package app.nzyme.core.rest.responses.system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class PingResponse {

    @JsonProperty("show_setup_wizard")
    public abstract boolean showSetupWizard();

    @JsonProperty("login_image")
    @Nullable
    public abstract String loginImage();

    public static PingResponse create(boolean showSetupWizard, String loginImage) {
        return builder()
                .showSetupWizard(showSetupWizard)
                .loginImage(loginImage)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PingResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder showSetupWizard(boolean showSetupWizard);

        public abstract Builder loginImage(String loginImage);

        public abstract PingResponse build();
    }
}
