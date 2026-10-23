package app.nzyme.core.rest.responses.ethernet.recon;

import app.nzyme.core.rest.responses.ethernet.EthernetMacAddressResponse;
import app.nzyme.core.rest.responses.ethernet.L4AddressResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class ReconScanDetailsResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("type")
    public abstract String type();

    @JsonProperty("type_human_readable")
    public abstract String typeHumanReadable();

    @JsonProperty("is_port_scan")
    public abstract boolean isPortScan();

    @JsonProperty("recent")
    public abstract boolean recent();

    @JsonProperty("detection_window_minutes")
    public abstract int detectionWindowMinutes();

    @Nullable
    @JsonProperty("source_address")
    public abstract String sourceAddress();

    @Nullable
    @JsonProperty("source_address_data")
    public abstract L4AddressResponse sourceAddressData();

    @Nullable
    @JsonProperty("source_mac")
    public abstract String sourceMac();

    @Nullable
    @JsonProperty("source_mac_data")
    public abstract EthernetMacAddressResponse sourceMacData();

    @JsonProperty("source")
    public abstract String source();

    @Nullable
    @JsonProperty("target_address")
    public abstract String targetAddress();

    @Nullable
    @JsonProperty("target_address_data")
    public abstract L4AddressResponse targetAddressData();

    @JsonProperty("target")
    public abstract String target();

    @JsonProperty("host_count")
    public abstract int hostCount();

    @JsonProperty("port_count")
    public abstract int portCount();

    @JsonProperty("probe_count")
    public abstract long probeCount();

    @JsonProperty("open_count")
    public abstract long openCount();

    @JsonProperty("closed_count")
    public abstract long closedCount();

    @JsonProperty("unanswered_count")
    public abstract long unansweredCount();

    @JsonProperty("targets_truncated")
    public abstract boolean targetsTruncated();

    @Nullable
    @JsonProperty("enrichment")
    public abstract Map<String, Object> enrichment();

    @JsonProperty("taps")
    public abstract List<ReconScanTapResponse> taps();

    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    @JsonProperty("matching_exclusions")
    public abstract List<ReconExclusionReferenceResponse> matchingExclusions();

    public static ReconScanDetailsResponse create(UUID uuid, String type, String typeHumanReadable, boolean isPortScan, boolean recent, int detectionWindowMinutes, String sourceAddress, L4AddressResponse sourceAddressData, String sourceMac, EthernetMacAddressResponse sourceMacData, String source, String targetAddress, L4AddressResponse targetAddressData, String target, int hostCount, int portCount, long probeCount, long openCount, long closedCount, long unansweredCount, boolean targetsTruncated, Map<String, Object> enrichment, List<ReconScanTapResponse> taps, DateTime firstSeen, DateTime lastSeen, DateTime createdAt, DateTime updatedAt, List<ReconExclusionReferenceResponse> matchingExclusions) {
        return builder()
                .uuid(uuid)
                .type(type)
                .typeHumanReadable(typeHumanReadable)
                .isPortScan(isPortScan)
                .recent(recent)
                .detectionWindowMinutes(detectionWindowMinutes)
                .sourceAddress(sourceAddress)
                .sourceAddressData(sourceAddressData)
                .sourceMac(sourceMac)
                .sourceMacData(sourceMacData)
                .source(source)
                .targetAddress(targetAddress)
                .targetAddressData(targetAddressData)
                .target(target)
                .hostCount(hostCount)
                .portCount(portCount)
                .probeCount(probeCount)
                .openCount(openCount)
                .closedCount(closedCount)
                .unansweredCount(unansweredCount)
                .targetsTruncated(targetsTruncated)
                .enrichment(enrichment)
                .taps(taps)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .matchingExclusions(matchingExclusions)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ReconScanDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);
        public abstract Builder type(String type);
        public abstract Builder typeHumanReadable(String typeHumanReadable);
        public abstract Builder isPortScan(boolean isPortScan);
        public abstract Builder recent(boolean recent);
        public abstract Builder detectionWindowMinutes(int detectionWindowMinutes);
        public abstract Builder sourceAddress(String sourceAddress);
        public abstract Builder sourceAddressData(L4AddressResponse sourceAddressData);
        public abstract Builder sourceMac(String sourceMac);
        public abstract Builder sourceMacData(EthernetMacAddressResponse sourceMacData);
        public abstract Builder source(String source);
        public abstract Builder targetAddress(String targetAddress);
        public abstract Builder targetAddressData(L4AddressResponse targetAddressData);
        public abstract Builder target(String target);
        public abstract Builder hostCount(int hostCount);
        public abstract Builder portCount(int portCount);
        public abstract Builder probeCount(long probeCount);
        public abstract Builder openCount(long openCount);
        public abstract Builder closedCount(long closedCount);
        public abstract Builder unansweredCount(long unansweredCount);
        public abstract Builder targetsTruncated(boolean targetsTruncated);
        public abstract Builder enrichment(Map<String, Object> enrichment);
        public abstract Builder taps(List<ReconScanTapResponse> taps);
        public abstract Builder firstSeen(DateTime firstSeen);
        public abstract Builder lastSeen(DateTime lastSeen);
        public abstract Builder createdAt(DateTime createdAt);
        public abstract Builder updatedAt(DateTime updatedAt);
        public abstract Builder matchingExclusions(List<ReconExclusionReferenceResponse> matchingExclusions);
        public abstract ReconScanDetailsResponse build();
    }
}
