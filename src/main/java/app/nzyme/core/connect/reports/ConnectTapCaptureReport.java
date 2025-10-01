package app.nzyme.core.connect.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class ConnectTapCaptureReport {

    @JsonProperty("interface_name")
    public abstract String interfaceName();

    @JsonProperty("capture_type")
    public abstract String captureType();

    @JsonProperty("is_running")
    public abstract boolean isRunning();

    @JsonProperty("cycle_time")
    public abstract int cycleTime();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    public static ConnectTapCaptureReport create(String interfaceName, String captureType, boolean isRunning, int cycleTime, DateTime updatedAt) {
        return builder()
                .interfaceName(interfaceName)
                .captureType(captureType)
                .isRunning(isRunning)
                .cycleTime(cycleTime)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectTapCaptureReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder interfaceName(String interfaceName);

        public abstract Builder captureType(String captureType);

        public abstract Builder isRunning(boolean isRunning);

        public abstract Builder cycleTime(int cycleTime);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract ConnectTapCaptureReport build();
    }
}
