package app.nzyme.core.rest.responses.ethernet.assets;

import app.nzyme.core.rest.responses.ethernet.EthernetMacAddressResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class AssetDetailsResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("mac")
    public abstract EthernetMacAddressResponse mac();

    @Nullable
    @JsonProperty("oui")
    public abstract String oui();

    @Nullable
    @JsonProperty("name")
    public abstract String name();

    @Nullable
    @JsonProperty("dhcp_fingerprint_initial")
    public abstract String dhcpFingerprintInitial();

    @Nullable
    @JsonProperty("dhcp_fingerprint_renew")
    public abstract String dhcpFingerprintRenew();

    @Nullable
    @JsonProperty("dhcp_fingerprint_reboot")
    public abstract String dhcpFingerprintReboot();

    @Nullable
    @JsonProperty("dhcp_fingerprint_rebind")
    public abstract String dhcpFingerprintRebind();

    @JsonProperty("hostnames")
    public abstract List<AssetHostnameResponse> hostnames();

    @JsonProperty("ip_addresses")
    public abstract List<AssetIpAddressResponse> ipAddresses();

    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    public static AssetDetailsResponse create(UUID uuid, EthernetMacAddressResponse mac, String oui, String name, String dhcpFingerprintInitial, String dhcpFingerprintRenew, String dhcpFingerprintReboot, String dhcpFingerprintRebind, List<AssetHostnameResponse> hostnames, List<AssetIpAddressResponse> ipAddresses, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .uuid(uuid)
                .mac(mac)
                .oui(oui)
                .name(name)
                .dhcpFingerprintInitial(dhcpFingerprintInitial)
                .dhcpFingerprintRenew(dhcpFingerprintRenew)
                .dhcpFingerprintReboot(dhcpFingerprintReboot)
                .dhcpFingerprintRebind(dhcpFingerprintRebind)
                .hostnames(hostnames)
                .ipAddresses(ipAddresses)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AssetDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder mac(EthernetMacAddressResponse mac);

        public abstract Builder oui(String oui);

        public abstract Builder name(String name);

        public abstract Builder dhcpFingerprintInitial(String dhcpFingerprintInitial);

        public abstract Builder dhcpFingerprintRenew(String dhcpFingerprintRenew);

        public abstract Builder dhcpFingerprintReboot(String dhcpFingerprintReboot);

        public abstract Builder dhcpFingerprintRebind(String dhcpFingerprintRebind);

        public abstract Builder hostnames(List<AssetHostnameResponse> hostnames);

        public abstract Builder ipAddresses(List<AssetIpAddressResponse> ipAddresses);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract AssetDetailsResponse build();
    }
}
