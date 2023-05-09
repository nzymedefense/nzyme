package app.nzyme.core.integrations.geoip;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.integrations.geoip.ipinfo.IpInfoFreeGeoIpAdapter;
import app.nzyme.core.integrations.geoip.noop.NoOpGeoIpAdapter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class GeoIpService {

    private static final Logger LOG = LogManager.getLogger(IpInfoFreeGeoIpAdapter.class);

    private final NzymeNode nzyme;

    private GeoIpAdapter adapter;

    private final LoadingCache<InetAddress, Optional<GeoIpLookupResult>> cache;

    public GeoIpService(NzymeNode nzyme) {
        this.nzyme = nzyme;

        this.cache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public Optional<GeoIpLookupResult> load(@NotNull InetAddress address) {
                        return adapter.lookup(address);
                    }
                });
    }

    public void initialize() {
        // Find out if there is a GeoIp adapter configured. Use NoOp adapter if not.
        GeoIpAdapter adapter;

        Optional<String> adapterName = nzyme.getDatabaseCoreRegistry().getValue(GeoIpRegistryKeys.GEOIP_ADAPTER_NAME.key());
        if (adapterName.isEmpty()) {
            adapter = new NoOpGeoIpAdapter();
        } else {
            switch (adapterName.get()) {
                case "ipinfo_free":
                    Optional<String> apiToken = nzyme.getDatabaseCoreRegistry()
                            .getValue(IpInfoFreeGeoIpAdapter.REGISTRY_KEY_TOKEN.key());

                    if (apiToken.isPresent()) {
                        adapter = new IpInfoFreeGeoIpAdapter(apiToken.get(), nzyme.getBaseConfiguration());
                    } else {
                        LOG.error("Missing IPinfo token. Cannot initialize GeoIP adapter.");
                        adapter = new NoOpGeoIpAdapter();
                    }
                    break;
                default:
                    adapter = new NoOpGeoIpAdapter();
            }
        }

        // Initialize adapter.
        LOG.info("Loading Geo IP adapter of type [{}].", adapter.getClass().getCanonicalName());
        this.adapter = adapter;
        this.adapter.initialize();
    }

    public Optional<GeoIpLookupResult> lookup(InetAddress address) {
        return cache.getUnchecked(address);
    }

    public long getCacheSize() {
        return this.cache.size();
    }

    public String getAdapterName() {
        return this.adapter.getName();
    }

}
