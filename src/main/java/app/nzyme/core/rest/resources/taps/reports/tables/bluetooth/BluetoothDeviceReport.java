package app.nzyme.core.rest.resources.taps.reports.tables.bluetooth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class BluetoothDeviceReport {

    public abstract String mac();
    public abstract String alias();
    public abstract String device();
    public abstract String transport();
    public abstract DateTime lastSeen();

    @Nullable
    public abstract String name();
    @Nullable
    public abstract Integer rssi();
    @Nullable
    public abstract Integer companyId();
    @Nullable
    public abstract Integer classNumber();
    @Nullable
    public abstract Integer appearance();
    @Nullable
    public abstract String modalias();
    @Nullable
    public abstract Integer txPower();
    @Nullable
    public abstract String manufacturerData();
    @Nullable
    public abstract List<String> uuids();
    @Nullable
    public abstract List<String> serviceData();
    @Nullable
    public abstract Map<String, Map<String, Object>> tags();

    @JsonCreator
    public static BluetoothDeviceReport create(@JsonProperty("mac") String mac,
                                               @JsonProperty("alias") String alias,
                                               @JsonProperty("device") String device,
                                               @JsonProperty("transport") String transport,
                                               @JsonProperty("last_seen") DateTime lastSeen,
                                               @JsonProperty("name") String name,
                                               @JsonProperty("rssi") Integer rssi,
                                               @JsonProperty("company_id") Integer companyId,
                                               @JsonProperty("class") Integer classNumber,
                                               @JsonProperty("appearance") Integer appearance,
                                               @JsonProperty("modalias") String modalias,
                                               @JsonProperty("tx_power") Integer txPower,
                                               @JsonProperty("manufacturer_data") String manufacturerData,
                                               @JsonProperty("uuids") List<String> uuids,
                                               @JsonProperty("service_data") List<String> serviceData,
                                               @JsonProperty("tags") Map<String, Map<String, Object>> tags) {
        return builder()
                .mac(mac)
                .alias(alias)
                .device(device)
                .transport(transport)
                .lastSeen(lastSeen)
                .name(name)
                .rssi(rssi)
                .companyId(companyId)
                .classNumber(classNumber)
                .appearance(appearance)
                .modalias(modalias)
                .txPower(txPower)
                .manufacturerData(manufacturerData)
                .uuids(uuids)
                .serviceData(serviceData)
                .tags(tags)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BluetoothDeviceReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mac(String mac);

        public abstract Builder alias(String alias);

        public abstract Builder device(String device);

        public abstract Builder transport(String transport);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder name(String name);

        public abstract Builder rssi(Integer rssi);

        public abstract Builder companyId(Integer companyId);

        public abstract Builder classNumber(Integer classNumber);

        public abstract Builder appearance(Integer appearance);

        public abstract Builder modalias(String modalias);

        public abstract Builder txPower(Integer txPower);

        public abstract Builder manufacturerData(String manufacturerData);

        public abstract Builder uuids(List<String> uuids);

        public abstract Builder serviceData(List<String> serviceData);

        public abstract Builder tags(Map<String, Map<String, Object>> tags);

        public abstract BluetoothDeviceReport build();
    }
}