package app.nzyme.core.rest.responses.bluetooth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class BluetoothDeviceSummaryDetailsResponse {

    @JsonProperty("mac")
    public abstract String mac();

    @JsonProperty("aliases")
    public abstract List<String> aliases();

    @JsonProperty("devices")
    public abstract List<String> devices();

    @JsonProperty("transports")
    public abstract List<String> transports();

    @JsonProperty("names")
    public abstract List<String> names();

    @JsonProperty("average_rssi")
    public abstract double averageRssi();

    @JsonProperty("companies")
    public abstract List<String> companies();

    @JsonProperty("device_classes")
    public abstract List<String> classes();

    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    public static BluetoothDeviceSummaryDetailsResponse create(String mac, List<String> aliases, List<String> devices, List<String> transports, List<String> names, double averageRssi, List<String> companies, List<String> classes, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .mac(mac)
                .aliases(aliases)
                .devices(devices)
                .transports(transports)
                .names(names)
                .averageRssi(averageRssi)
                .companies(companies)
                .classes(classes)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BluetoothDeviceSummaryDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mac(String mac);

        public abstract Builder aliases(List<String> aliases);

        public abstract Builder devices(List<String> devices);

        public abstract Builder transports(List<String> transports);

        public abstract Builder names(List<String> names);

        public abstract Builder averageRssi(double averageRssi);

        public abstract Builder companies(List<String> companies);

        public abstract Builder classes(List<String> classes);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract BluetoothDeviceSummaryDetailsResponse build();
    }
}
