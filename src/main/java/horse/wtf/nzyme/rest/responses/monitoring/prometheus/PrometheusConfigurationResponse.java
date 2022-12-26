package horse.wtf.nzyme.rest.responses.monitoring.prometheus;

import app.nzyme.plugin.rest.configuration.ConfigurationEntryResponse;
import app.nzyme.plugin.rest.configuration.EncryptedConfigurationEntryResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PrometheusConfigurationResponse {

    @JsonProperty("prometheus_rest_report_enabled")
    public abstract ConfigurationEntryResponse restReportEnabled();

    @JsonProperty("prometheus_rest_report_username")
    public abstract ConfigurationEntryResponse username();

    @JsonProperty("prometheus_rest_report_password")
    public abstract EncryptedConfigurationEntryResponse password();

    public static PrometheusConfigurationResponse create(ConfigurationEntryResponse restReportEnabled, ConfigurationEntryResponse username, EncryptedConfigurationEntryResponse password) {
        return builder()
                .restReportEnabled(restReportEnabled)
                .username(username)
                .password(password)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PrometheusConfigurationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder restReportEnabled(ConfigurationEntryResponse restReportEnabled);

        public abstract Builder username(ConfigurationEntryResponse username);

        public abstract Builder password(EncryptedConfigurationEntryResponse password);

        public abstract PrometheusConfigurationResponse build();
    }

}
