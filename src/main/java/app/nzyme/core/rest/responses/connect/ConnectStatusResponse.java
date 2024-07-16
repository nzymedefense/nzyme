package app.nzyme.core.rest.responses.connect;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class ConnectStatusResponse {

    @JsonProperty("connection_summary")
    public abstract String connectionSummary();

    @JsonProperty("last_successful_report_submission")
    @Nullable
    public abstract DateTime lastSuccessfulReportSubmission();

    @JsonProperty("provided_services")
    public abstract List<String> providedServices();

    public static ConnectStatusResponse create(String connectionSummary, DateTime lastSuccessfulReportSubmission, List<String> providedServices) {
        return builder()
                .connectionSummary(connectionSummary)
                .lastSuccessfulReportSubmission(lastSuccessfulReportSubmission)
                .providedServices(providedServices)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectStatusResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder connectionSummary(String connectionSummary);

        public abstract Builder lastSuccessfulReportSubmission(DateTime lastSuccessfulReportSubmission);

        public abstract Builder providedServices(List<String> providedServices);

        public abstract ConnectStatusResponse build();
    }
}
