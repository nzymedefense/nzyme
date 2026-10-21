package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;

import java.util.List;

@Schema(description = "Body for creating, updating or previewing a beacon exclusion. All criteria are optional, but "
        + "at least one must be set. A beacon or candidate is excluded when every set criterion matches.")
@AutoValue
public abstract class BeaconExclusionRequest {

    @Nullable
    public abstract String name();

    @Nullable
    public abstract String description();

    @Nullable
    public abstract Boolean enabled();

    @Nullable
    public abstract String destinationCidr();

    @Nullable
    public abstract String destinationHost();

    @Nullable
    public abstract String destinationPorts();

    @Nullable
    public abstract List<String> beaconTypes();

    @Nullable
    public abstract String sourceCidr();

    @Nullable
    public abstract String sourceMac();

    @Nullable
    public abstract Boolean applyToExistingBeacons();

    @JsonCreator
    public static BeaconExclusionRequest create(@Schema(description = "Name of the exclusion. Required when creating or updating, ignored for previews.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                                                @JsonProperty("name") String name,
                                                @Schema(description = "Optional description of why the exclusion exists.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                                                @JsonProperty("description") String description,
                                                @Schema(description = "Whether the exclusion is active. Defaults to true.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                                                @JsonProperty("enabled") Boolean enabled,
                                                @Schema(description = "Destination IP address or CIDR network, like 10.0.0.0/8 or 2001:db8::/32. Matches the destination address of a beacon. DNS beacons have no destination address.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                                                @JsonProperty("destination_cidr") String destinationCidr,
                                                @Schema(description = "Destination host name pattern. Use * as a wildcard. *.example.com matches example.com and all of its subdomains. Matches the TLS server name, SOCKS tunneled host or DNS domain of a beacon.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                                                @JsonProperty("destination_host") String destinationHost,
                                                @Schema(description = "Destination ports and port ranges, comma separated, like 443 or 80,443,8000-8100.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                                                @JsonProperty("destination_ports") String destinationPorts,
                                                @Schema(description = "Beacon types to match: TLS, HTTP, SSH, SOCKS, DNS, TCP, UDP. Omit or pass an empty list to match all types.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                                                @JsonProperty("beacon_types") List<String> beaconTypes,
                                                @Schema(description = "IP address or CIDR network of the calling device.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                                                @JsonProperty("source_cidr") String sourceCidr,
                                                @Schema(description = "MAC address of the calling device.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                                                @JsonProperty("source_mac") String sourceMac,
                                                @Schema(description = "Only when creating: also delete the existing beacons that match the exclusion. Defaults to false.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                                                @JsonProperty("apply_to_existing_beacons") Boolean applyToExistingBeacons) {
        return builder()
                .name(name)
                .description(description)
                .enabled(enabled)
                .destinationCidr(destinationCidr)
                .destinationHost(destinationHost)
                .destinationPorts(destinationPorts)
                .beaconTypes(beaconTypes)
                .sourceCidr(sourceCidr)
                .sourceMac(sourceMac)
                .applyToExistingBeacons(applyToExistingBeacons)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconExclusionRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);
        public abstract Builder description(String description);
        public abstract Builder enabled(Boolean enabled);
        public abstract Builder destinationCidr(String destinationCidr);
        public abstract Builder destinationHost(String destinationHost);
        public abstract Builder destinationPorts(String destinationPorts);
        public abstract Builder beaconTypes(List<String> beaconTypes);
        public abstract Builder sourceCidr(String sourceCidr);
        public abstract Builder sourceMac(String sourceMac);
        public abstract Builder applyToExistingBeacons(Boolean applyToExistingBeacons);
        public abstract BeaconExclusionRequest build();
    }
}
