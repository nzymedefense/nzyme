package horse.wtf.nzyme.rest.responses.monitoring.prometheus;

import app.nzyme.plugin.rest.configuration.ConfigurationEntryResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PrometheusConfigurationResponse {

    @JsonProperty("prometheus_rest_report_enabled")
    public abstract ConfigurationEntryResponse restReportEnabled();

    public static PrometheusConfigurationResponse create(ConfigurationEntryResponse restReportEnabled) {
        return builder()
                .restReportEnabled(restReportEnabled)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PrometheusConfigurationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder restReportEnabled(ConfigurationEntryResponse restReportEnabled);

        public abstract PrometheusConfigurationResponse build();
    }

}
