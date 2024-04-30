package app.nzyme.core.rest.resources.taps.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.HashMap;
import java.util.List;

@AutoValue
public abstract class HelloReport {

    public abstract HashMap<String, List<WiFiSupportedFrequencyReport>> wifiDeviceAssignments();

    @JsonCreator
    public static HelloReport create(@JsonProperty("wifi_device_assignments") HashMap<String, List<WiFiSupportedFrequencyReport>> wifiDeviceAssignments) {
        return builder()
                .wifiDeviceAssignments(wifiDeviceAssignments)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_HelloReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder wifiDeviceAssignments(HashMap<String, List<WiFiSupportedFrequencyReport>> wifiDeviceAssignments);

        public abstract HelloReport build();
    }
}