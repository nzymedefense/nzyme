package app.nzyme.core.rest.resources.taps.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

import java.util.HashMap;
import java.util.List;

@AutoValue
public abstract class HelloReport {

    @Nullable
    public abstract HashMap<String, List<WiFiSupportedFrequencyReport>> wifiDeviceAssignments();

    @Nullable
    public abstract HashMap<String, Integer> wifiDeviceCycleTimes();

    @JsonCreator
    public static HelloReport create(@JsonProperty("wifi_device_assignments") HashMap<String, List<WiFiSupportedFrequencyReport>> wifiDeviceAssignments,
                                     @JsonProperty("wifi_device_cycle_times") HashMap<String, Integer> wifiDeviceCycleTimes) {
        return builder()
                .wifiDeviceAssignments(wifiDeviceAssignments)
                .wifiDeviceCycleTimes(wifiDeviceCycleTimes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_HelloReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder wifiDeviceAssignments(HashMap<String, List<WiFiSupportedFrequencyReport>> wifiDeviceAssignments);

        public abstract Builder wifiDeviceCycleTimes(HashMap<String, Integer> wifiDeviceCycleTimes);

        public abstract HelloReport build();
    }
}