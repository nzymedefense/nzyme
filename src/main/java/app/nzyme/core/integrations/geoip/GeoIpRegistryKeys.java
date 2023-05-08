package app.nzyme.core.integrations.geoip;

import app.nzyme.plugin.RegistryKey;

import java.util.Optional;

public class GeoIpRegistryKeys {

    public static final RegistryKey GEOIP_ADAPTER_NAME = RegistryKey.create(
            "geoip_adapter_name",
            Optional.empty(),
            Optional.empty(),
            true
    );

}
