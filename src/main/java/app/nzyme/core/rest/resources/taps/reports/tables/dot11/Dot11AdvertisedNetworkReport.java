package app.nzyme.core.rest.resources.taps.reports.tables.dot11;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class Dot11AdvertisedNetworkReport {

    public abstract List<Dot11SecurityInformationReport> security();
    public abstract List<String> fingerprints();
    public abstract boolean wps();
    public abstract Dot11SignalStrengthReport signalStrength();
    public abstract List<String> infrastructureTypes();
    public abstract Map<Long, Map<String, Dot11ChannelStatisticsReport>> channelStatistics();

    @JsonCreator
    public static Dot11AdvertisedNetworkReport create(@JsonProperty("security") List<Dot11SecurityInformationReport> security,
                                                      @JsonProperty("fingerprints") List<String> fingerprints,
                                                      @JsonProperty("wps") boolean wps,
                                                      @JsonProperty("signal_strength") Dot11SignalStrengthReport signalStrength,
                                                      @JsonProperty("infrastructure_types") List<String> infrastructureTypes,
                                                      @JsonProperty("channel_statistics") Map<Long, Map<String, Dot11ChannelStatisticsReport>> channelStatistics) {
        return builder()
                .security(security)
                .fingerprints(fingerprints)
                .wps(wps)
                .signalStrength(signalStrength)
                .infrastructureTypes(infrastructureTypes)
                .channelStatistics(channelStatistics)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11AdvertisedNetworkReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder security(List<Dot11SecurityInformationReport> security);

        public abstract Builder fingerprints(List<String> fingerprints);

        public abstract Builder wps(boolean wps);

        public abstract Builder signalStrength(Dot11SignalStrengthReport signalStrength);

        public abstract Builder infrastructureTypes(List<String> infrastructureTypes);

        public abstract Builder channelStatistics(Map<Long, Map<String, Dot11ChannelStatisticsReport>> channelStatistics);

        public abstract Dot11AdvertisedNetworkReport build();
    }
}
