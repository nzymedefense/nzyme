package app.nzyme.core.rest.resources.taps.reports.tables.tls;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class TlsSessionsReport {

    public abstract List<TlsSessionReport> sessions();
    public abstract Map<String, String> certificates();

    @JsonCreator
    public static TlsSessionsReport create(@JsonProperty("sessions") List<TlsSessionReport> sessions,
                                           @JsonProperty("certificates") Map<String, String> certificates) {
        return builder()
                .sessions(sessions)
                .certificates(certificates)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TlsSessionsReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder sessions(List<TlsSessionReport> sessions);

        public abstract Builder certificates(Map<String, String> certificates);

        public abstract TlsSessionsReport build();
    }
}
