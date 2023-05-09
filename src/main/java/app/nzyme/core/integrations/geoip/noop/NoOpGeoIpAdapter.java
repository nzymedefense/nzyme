package app.nzyme.core.integrations.geoip.noop;

import app.nzyme.core.integrations.geoip.GeoIpAdapter;
import app.nzyme.core.integrations.geoip.GeoIpLookupResult;

import java.net.InetAddress;
import java.util.Optional;

public class NoOpGeoIpAdapter implements GeoIpAdapter {

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public Optional<GeoIpLookupResult> lookup(InetAddress address) {
        return Optional.empty();
    }

    @Override
    public String getName() {
        return "noop";
    }


}
