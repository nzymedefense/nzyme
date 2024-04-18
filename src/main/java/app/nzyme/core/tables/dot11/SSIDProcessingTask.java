package app.nzyme.core.tables.dot11;

import app.nzyme.core.rest.resources.taps.reports.tables.dot11.Dot11AdvertisedNetworkReport;
import app.nzyme.core.taps.Tap;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class SSIDProcessingTask {

    public abstract String bssid();
    public abstract String ssid();
    public abstract Dot11AdvertisedNetworkReport ssidReport();
    public abstract long bssidDatabaseId();
    public abstract Tap tap();
    public abstract DateTime timestamp();

    public static SSIDProcessingTask create(String bssid, String ssid, Dot11AdvertisedNetworkReport ssidReport, long bssidDatabaseId, Tap tap, DateTime timestamp) {
        return builder()
                .bssid(bssid)
                .ssid(ssid)
                .ssidReport(ssidReport)
                .bssidDatabaseId(bssidDatabaseId)
                .tap(tap)
                .timestamp(timestamp)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSIDProcessingTask.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bssid(String bssid);

        public abstract Builder ssid(String ssid);

        public abstract Builder ssidReport(Dot11AdvertisedNetworkReport ssidReport);

        public abstract Builder bssidDatabaseId(long bssidDatabaseId);

        public abstract Builder tap(Tap tap);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract SSIDProcessingTask build();
    }
}
