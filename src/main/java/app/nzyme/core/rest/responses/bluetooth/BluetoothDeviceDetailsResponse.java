package app.nzyme.core.rest.responses.bluetooth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class BluetoothDeviceDetailsResponse {

    @JsonProperty("device")
    public abstract BluetoothDeviceSummaryDetailsResponse device();

    @JsonProperty("data_retention_days")
    public abstract int dataRetentionDays();

    public static BluetoothDeviceDetailsResponse create(BluetoothDeviceSummaryDetailsResponse device, int dataRetentionDays) {
        return builder()
                .device(device)
                .dataRetentionDays(dataRetentionDays)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BluetoothDeviceDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder device(BluetoothDeviceSummaryDetailsResponse device);

        public abstract Builder dataRetentionDays(int dataRetentionDays);

        public abstract BluetoothDeviceDetailsResponse build();
    }
}
