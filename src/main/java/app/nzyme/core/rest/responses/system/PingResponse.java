package app.nzyme.core.rest.responses.system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PingResponse {

    @JsonProperty("show_setup_wizard")
    public abstract boolean showSetupWizard();

    public static PingResponse create(boolean showSetupWizard) {
        return builder()
                .showSetupWizard(showSetupWizard)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PingResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder showSetupWizard(boolean showSetupWizard);

        public abstract PingResponse build();
    }

}
