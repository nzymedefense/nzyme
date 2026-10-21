package app.nzyme.core.rest.responses.ethernet.beacons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import app.nzyme.core.rest.responses.ethernet.L4AddressResponse;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class BeaconDetailsResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("organization_id")
    public abstract UUID organizationId();

    @JsonProperty("tenant_id")
    public abstract UUID tenantId();

    @JsonProperty("type")
    public abstract String type();

    @JsonProperty("type_human_readable")
    public abstract String typeHumanReadable();

    @JsonProperty("destination_key")
    public abstract String destinationKey();

    @Nullable
    @JsonProperty("destination_host")
    public abstract String destinationHost();

    @Nullable
    @JsonProperty("destination_address")
    public abstract String destinationAddress();

    @Nullable
    @JsonProperty("destination_port")
    public abstract Integer destinationPort();

    @JsonProperty("destination")
    public abstract String destination();

    @Nullable
    @JsonProperty("destination_address_data")
    public abstract L4AddressResponse destinationAddressData();

    @JsonProperty("destination_is_site_local")
    public abstract boolean destinationIsSiteLocal();

    @JsonProperty("name")
    public abstract String name();

    @Nullable
    @JsonProperty("description")
    public abstract String description();

    @JsonProperty("classification")
    public abstract String classification();

    @Nullable
    @JsonProperty("classification_changed_at")
    public abstract DateTime classificationChangedAt();

    @JsonProperty("source_count")
    public abstract int sourceCount();

    @JsonProperty("checkin_count")
    public abstract long checkinCount();

    @JsonProperty("interval_seconds")
    public abstract double intervalSeconds();

    @JsonProperty("interval_jitter")
    public abstract double intervalJitter();

    @Nullable
    @JsonProperty("enrichment")
    public abstract Map<String, Object> enrichment();

    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    @JsonProperty("matching_exclusions")
    public abstract List<BeaconExclusionReferenceResponse> matchingExclusions();

    public static BeaconDetailsResponse create(UUID uuid, UUID organizationId, UUID tenantId, String type, String typeHumanReadable, String destinationKey, String destinationHost, String destinationAddress, Integer destinationPort, String destination, L4AddressResponse destinationAddressData, boolean destinationIsSiteLocal, String name, String description, String classification, DateTime classificationChangedAt, int sourceCount, long checkinCount, double intervalSeconds, double intervalJitter, Map<String, Object> enrichment, DateTime firstSeen, DateTime lastSeen, DateTime createdAt, DateTime updatedAt, List<BeaconExclusionReferenceResponse> matchingExclusions) {
        return builder()
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .type(type)
                .typeHumanReadable(typeHumanReadable)
                .destinationKey(destinationKey)
                .destinationHost(destinationHost)
                .destinationAddress(destinationAddress)
                .destinationPort(destinationPort)
                .destination(destination)
                .destinationAddressData(destinationAddressData)
                .destinationIsSiteLocal(destinationIsSiteLocal)
                .name(name)
                .description(description)
                .classification(classification)
                .classificationChangedAt(classificationChangedAt)
                .sourceCount(sourceCount)
                .checkinCount(checkinCount)
                .intervalSeconds(intervalSeconds)
                .intervalJitter(intervalJitter)
                .enrichment(enrichment)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .matchingExclusions(matchingExclusions)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder type(String type);

        public abstract Builder typeHumanReadable(String typeHumanReadable);

        public abstract Builder destinationKey(String destinationKey);

        public abstract Builder destinationHost(String destinationHost);

        public abstract Builder destinationAddress(String destinationAddress);

        public abstract Builder destinationPort(Integer destinationPort);

        public abstract Builder destination(String destination);

        public abstract Builder destinationAddressData(L4AddressResponse destinationAddressData);

        public abstract Builder destinationIsSiteLocal(boolean destinationIsSiteLocal);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder classification(String classification);

        public abstract Builder classificationChangedAt(DateTime classificationChangedAt);

        public abstract Builder sourceCount(int sourceCount);

        public abstract Builder checkinCount(long checkinCount);

        public abstract Builder intervalSeconds(double intervalSeconds);

        public abstract Builder intervalJitter(double intervalJitter);

        public abstract Builder enrichment(Map<String, Object> enrichment);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder matchingExclusions(List<BeaconExclusionReferenceResponse> matchingExclusions);

        public abstract BeaconDetailsResponse build();
    }
}
