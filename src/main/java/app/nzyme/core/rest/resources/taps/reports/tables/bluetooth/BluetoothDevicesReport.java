package app.nzyme.core.rest.resources.taps.reports.tables.bluetooth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class BluetoothDevicesReport {

    public abstract List<BluetoothDeviceReport> devices();

    @JsonCreator
    public static BluetoothDevicesReport create(@JsonProperty("devices") List<BluetoothDeviceReport> devices) {
        return builder()
                .devices(devices)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BluetoothDevicesReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder devices(List<BluetoothDeviceReport> devices);

        public abstract BluetoothDevicesReport build();
    }
}