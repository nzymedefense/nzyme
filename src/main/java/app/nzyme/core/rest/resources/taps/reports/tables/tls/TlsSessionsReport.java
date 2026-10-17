package app.nzyme.core.rest.resources.taps.reports.tables.tls;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TlsSessionsReport {

    public abstract List<TlsSessionReport> sessions();

    @JsonCreator
    public static TlsSessionsReport create(@JsonProperty("sessions") List<TlsSessionReport> sessions) {
        return builder()
                .sessions(sessions)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TlsSessionsReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder sessions(List<TlsSessionReport> sessions);

        public abstract TlsSessionsReport build();
    }
}
