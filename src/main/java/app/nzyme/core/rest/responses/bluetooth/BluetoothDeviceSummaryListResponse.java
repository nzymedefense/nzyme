package app.nzyme.core.rest.responses.bluetooth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class BluetoothDeviceSummaryListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("devices")
    public abstract List<BluetoothDeviceSummaryDetailsResponse> devices();

    public static BluetoothDeviceSummaryListResponse create(long count, List<BluetoothDeviceSummaryDetailsResponse> devices) {
        return builder()
                .count(count)
                .devices(devices)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BluetoothDeviceSummaryListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder devices(List<BluetoothDeviceSummaryDetailsResponse> devices);

        public abstract BluetoothDeviceSummaryListResponse build();
    }
}
