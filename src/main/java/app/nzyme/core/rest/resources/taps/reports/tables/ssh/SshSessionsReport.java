package app.nzyme.core.rest.resources.taps.reports.tables.ssh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class SshSessionsReport {

    public abstract List<SshSessionReport> sessions();

    @JsonCreator
    public static SshSessionsReport create(@JsonProperty("sessions") List<SshSessionReport> sessions) {
        return builder()
                .sessions(sessions)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SshSessionsReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder sessions(List<SshSessionReport> sessions);

        public abstract SshSessionsReport build();
    }
}