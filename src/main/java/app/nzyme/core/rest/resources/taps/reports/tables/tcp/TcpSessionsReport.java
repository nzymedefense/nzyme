package app.nzyme.core.rest.resources.taps.reports.tables.tcp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TcpSessionsReport {

    public abstract List<TcpSessionReport> sessions();

    @JsonCreator
    public static TcpSessionsReport create(@JsonProperty("sessions") List<TcpSessionReport> sessions) {
        return builder()
                .sessions(sessions)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TcpSessionsReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder sessions(List<TcpSessionReport> sessions);

        public abstract TcpSessionsReport build();
    }
}
