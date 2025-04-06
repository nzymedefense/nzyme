package app.nzyme.core.rest.responses.bluetooth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class BluetoothDeviceDetailsResponse {

    @JsonProperty("device")
    public abstract BluetoothDeviceSummaryDetailsResponse device();

    public static BluetoothDeviceDetailsResponse create(BluetoothDeviceSummaryDetailsResponse device) {
        return builder()
                .device(device)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BluetoothDeviceDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder device(BluetoothDeviceSummaryDetailsResponse device);

        public abstract BluetoothDeviceDetailsResponse build();
    }
}
