package app.nzyme.core.integrations.geoip;

import java.net.InetAddress;
import java.util.Optional;

public interface GeoIpAdapter {

    void initialize();
    void shutdown();

    Optional<GeoIpLookupResult> lookup(InetAddress address);

    String getName();

}
