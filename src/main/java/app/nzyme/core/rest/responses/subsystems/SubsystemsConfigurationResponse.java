package app.nzyme.core.rest.responses.subsystems;

import app.nzyme.plugin.rest.configuration.ConfigurationEntryResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SubsystemsConfigurationResponse {

    @JsonProperty("subsystem_ethernet_available")
    public abstract boolean ethernetAvailable();

    @JsonProperty("subsystem_dot11_available")
    public abstract boolean dot11Available();

    @JsonProperty("subsystem_bluetooth_available")
    public abstract boolean bluetoothAvailable();

    @JsonProperty("subsystem_uav_available")
    public abstract boolean uavAvailable();

    @JsonProperty("subsystem_ethernet_enabled")
    public abstract ConfigurationEntryResponse ethernetEnabled();

    @JsonProperty("subsystem_dot11_enabled")
    public abstract ConfigurationEntryResponse dot11Enabled();

    @JsonProperty("subsystem_bluetooth_enabled")
    public abstract ConfigurationEntryResponse bluetoothEnabled();

    @JsonProperty("subsystem_uav_enabled")
    public abstract ConfigurationEntryResponse uavEnabled();

    public static SubsystemsConfigurationResponse create(boolean ethernetAvailable, boolean dot11Available, boolean bluetoothAvailable, boolean uavAvailable, ConfigurationEntryResponse ethernetEnabled, ConfigurationEntryResponse dot11Enabled, ConfigurationEntryResponse bluetoothEnabled, ConfigurationEntryResponse uavEnabled) {
        return builder()
                .ethernetAvailable(ethernetAvailable)
                .dot11Available(dot11Available)
                .bluetoothAvailable(bluetoothAvailable)
                .uavAvailable(uavAvailable)
                .ethernetEnabled(ethernetEnabled)
                .dot11Enabled(dot11Enabled)
                .bluetoothEnabled(bluetoothEnabled)
                .uavEnabled(uavEnabled)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SubsystemsConfigurationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ethernetAvailable(boolean ethernetAvailable);

        public abstract Builder dot11Available(boolean dot11Available);

        public abstract Builder bluetoothAvailable(boolean bluetoothAvailable);

        public abstract Builder uavAvailable(boolean uavAvailable);

        public abstract Builder ethernetEnabled(ConfigurationEntryResponse ethernetEnabled);

        public abstract Builder dot11Enabled(ConfigurationEntryResponse dot11Enabled);

        public abstract Builder bluetoothEnabled(ConfigurationEntryResponse bluetoothEnabled);

        public abstract Builder uavEnabled(ConfigurationEntryResponse uavEnabled);

        public abstract SubsystemsConfigurationResponse build();
    }
}
