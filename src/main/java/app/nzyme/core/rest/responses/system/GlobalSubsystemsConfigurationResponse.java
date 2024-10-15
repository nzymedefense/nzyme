package app.nzyme.core.rest.responses.system;

import app.nzyme.plugin.rest.configuration.ConfigurationEntryResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class GlobalSubsystemsConfigurationResponse {

    @JsonProperty("subsystem_ethernet_enabled")
    public abstract ConfigurationEntryResponse ethernetEnabled();

    @JsonProperty("subsystem_dot11_enabled")
    public abstract ConfigurationEntryResponse dot11Enabled();

    @JsonProperty("subsystem_bluetooth_enabled")
    public abstract ConfigurationEntryResponse bluetoothEnabled();

    public static GlobalSubsystemsConfigurationResponse create(ConfigurationEntryResponse ethernetEnabled, ConfigurationEntryResponse dot11Enabled, ConfigurationEntryResponse bluetoothEnabled) {
        return builder()
                .ethernetEnabled(ethernetEnabled)
                .dot11Enabled(dot11Enabled)
                .bluetoothEnabled(bluetoothEnabled)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GlobalSubsystemsConfigurationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ethernetEnabled(ConfigurationEntryResponse ethernetEnabled);

        public abstract Builder dot11Enabled(ConfigurationEntryResponse dot11Enabled);

        public abstract Builder bluetoothEnabled(ConfigurationEntryResponse bluetoothEnabled);

        public abstract GlobalSubsystemsConfigurationResponse build();
    }
}
