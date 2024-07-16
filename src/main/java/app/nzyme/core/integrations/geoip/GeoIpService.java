package app.nzyme.core.integrations.geoip;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.connect.ConnectRegistryKeys;
import app.nzyme.core.integrations.geoip.ipinfo.IpInfoFreeGeoIpAdapter;
import app.nzyme.core.integrations.geoip.noop.NoOpGeoIpAdapter;
import app.nzyme.core.util.MetricNames;
import app.nzyme.plugin.RegistryCryptoException;
import com.codahale.metrics.Gauge;
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

    private final LoadingCache<InetAddress, Optional<GeoIpLookupResult>> cache;
    private boolean useCaching = true;

    public GeoIpService(NzymeNode nzyme) {
        this.nzyme = nzyme;

        this.cache = CacheBuilder.newBuilder() // TODO Configurable
                .maximumSize(10000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public Optional<GeoIpLookupResult> load(@NotNull InetAddress address) {
                        return lookup(address);
                    }
                });

        nzyme.getMetrics().register(MetricNames.GEOIP_CACHE_SIZE, new Gauge<Long>() {
            @Override
            public Long getValue() {
                return cache.size();
            }
        });

        // Reload on configuration change.
        nzyme.getRegistryChangeMonitor()
                .onChange("core", ConnectRegistryKeys.CONNECT_API_KEY.key(), this::reload);
    }

    private void reload() {
        GeoIpAdapter currentAdapter = adapter;

        // Reload with new registry settings.
        initialize();

        // Clear cache.
        cache.invalidateAll();

        // Shut down previous adapter.
        if (currentAdapter != null) {
            currentAdapter.shutdown();
        }
    }

    public void initialize() {
        // IMPORTANT: This method will also be called on configuration changes.

        // Find out if there is a GeoIp adapter configured. Use NoOp adapter if not.
        GeoIpAdapter adapter;

        Optional<String> adapterName = nzyme.getDatabaseCoreRegistry().getValue(GeoIpRegistryKeys.GEOIP_PROVIDER_NAME.key());
        if (adapterName.isEmpty()) {
            useCaching = false;
            adapter = new NoOpGeoIpAdapter();
        } else {
            switch (adapterName.get()) {
                case "ipinfo_free":
                    Optional<String> apiToken;
                    try {
                        apiToken = nzyme.getDatabaseCoreRegistry()
                                .getEncryptedValue(IpInfoFreeGeoIpAdapter.REGISTRY_KEY_TOKEN.key());
                    } catch (RegistryCryptoException e) {
                        throw new RuntimeException("Could not decrypt API token.");
                    }

                    if (apiToken.isPresent()) {
                        adapter = new IpInfoFreeGeoIpAdapter(apiToken.get(), nzyme.getBaseConfiguration());
                    } else {
                        LOG.error("Missing IPinfo token. Cannot initialize GeoIP adapter.");
                        useCaching = false;
                        adapter = new NoOpGeoIpAdapter();
                    }
                    break;
                default:
                    useCaching = false;
                    adapter = new NoOpGeoIpAdapter();
            }
        }

        // Initialize adapter.
        LOG.info("Loading Geo IP adapter of type [{}].", adapter.getClass().getCanonicalName());
        this.adapter = adapter;
        this.adapter.initialize();
    }

    public Optional<GeoIpLookupResult> lookup(InetAddress address) {
        if (useCaching) {
            return cache.getUnchecked(address);
        } else {
            return adapter.lookup(address);
        }
    }

}
