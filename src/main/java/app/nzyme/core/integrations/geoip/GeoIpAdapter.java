package app.nzyme.core.integrations.geoip;

import jakarta.annotation.Nullable;

import java.net.InetAddress;
import java.util.Optional;

public interface GeoIpAdapter {

    void initialize();
    void shutdown();

    Optional<GeoIpLookupResult> lookup(@Nullable InetAddress address);

    String getName();

}
