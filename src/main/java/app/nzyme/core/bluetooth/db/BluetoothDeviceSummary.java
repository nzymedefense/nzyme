package app.nzyme.core.bluetooth.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class BluetoothDeviceSummary {

    public abstract String mac();
    public abstract List<String> aliases();
    public abstract List<String> devices();
    public abstract List<String> transports();
    public abstract List<String> names();
    public abstract double averageRssi();
    public abstract List<Integer> companyIds();
    public abstract List<Integer> classNumbers();
    public abstract List<String> discoveredServices();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();

    public static BluetoothDeviceSummary create(String mac, List<String> aliases, List<String> devices, List<String> transports, List<String> names, double averageRssi, List<Integer> companyIds, List<Integer> classNumbers, List<String> discoveredServices, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .mac(mac)
                .aliases(aliases)
                .devices(devices)
                .transports(transports)
                .names(names)
                .averageRssi(averageRssi)
                .companyIds(companyIds)
                .classNumbers(classNumbers)
                .discoveredServices(discoveredServices)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BluetoothDeviceSummary.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mac(String mac);

        public abstract Builder aliases(List<String> aliases);

        public abstract Builder devices(List<String> devices);

        public abstract Builder transports(List<String> transports);

        public abstract Builder names(List<String> names);

        public abstract Builder averageRssi(double averageRssi);

        public abstract Builder companyIds(List<Integer> companyIds);

        public abstract Builder classNumbers(List<Integer> classNumbers);

        public abstract Builder discoveredServices(List<String> discoveredServices);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract BluetoothDeviceSummary build();
    }
}
