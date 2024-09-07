package app.nzyme.core.rest.misc;

import app.nzyme.core.rest.responses.context.MacAddressTransparentHostnameResponse;
import app.nzyme.core.rest.responses.context.MacAddressTransparentIpAddressResponse;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class CategorizedTransparentContextData {

    public abstract List<MacAddressTransparentIpAddressResponse> ipAddresses();
    public abstract List<MacAddressTransparentHostnameResponse> hostnames();

    public static CategorizedTransparentContextData create(List<MacAddressTransparentIpAddressResponse> ipAddresses, List<MacAddressTransparentHostnameResponse> hostnames) {
        return builder()
                .ipAddresses(ipAddresses)
                .hostnames(hostnames)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CategorizedTransparentContextData.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ipAddresses(List<MacAddressTransparentIpAddressResponse> ipAddresses);

        public abstract Builder hostnames(List<MacAddressTransparentHostnameResponse> hostnames);

        public abstract CategorizedTransparentContextData build();
    }
}
