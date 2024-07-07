package app.nzyme.core.rest.responses.connect;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class ConnectStatusResponse {

    @JsonProperty("connection_summary")
    public abstract String connectionSummary();

    @JsonProperty("last_successful_report_submission")
    @Nullable
    public abstract DateTime lastSuccessfulReportSubmission();

    public static ConnectStatusResponse create(String connectionSummary, DateTime lastSuccessfulReportSubmission) {
        return builder()
                .connectionSummary(connectionSummary)
                .lastSuccessfulReportSubmission(lastSuccessfulReportSubmission)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectStatusResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder connectionSummary(String connectionSummary);

        public abstract Builder lastSuccessfulReportSubmission(DateTime lastSuccessfulReportSubmission);

        public abstract ConnectStatusResponse build();
    }
}
