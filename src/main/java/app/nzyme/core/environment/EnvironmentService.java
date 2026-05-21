package app.nzyme.core.environment;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.connect.ConnectRegistryKeys;
import app.nzyme.core.floorplans.db.TenantLocationEntry;
import app.nzyme.core.security.authentication.db.TenantEntry;
import com.google.common.collect.Maps;
import com.google.common.net.HttpHeaders;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.joda.JodaModule;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class EnvironmentService {

    private static final Logger LOG = LogManager.getLogger(EnvironmentService.class);

    private final NzymeNode nzyme;

    private final ScheduledExecutorService refresher;
    private final ReentrantLock lock = new ReentrantLock();

    // Can be disabled if Connect is not set up or environment data source is not enabled in Connect.
    private boolean isEnabled = false;

    private Map<UUID, EnvironmentData> environmentData;

    public EnvironmentService(NzymeNode nzyme) {
        this.nzyme = nzyme;

        // Reload on configuration change.
        nzyme.getRegistryChangeMonitor()
                .onChange("core", ConnectRegistryKeys.CONNECT_API_KEY.key(), this::reload);
        nzyme.getRegistryChangeMonitor()
                .onChange("core", ConnectRegistryKeys.CONNECT_ENABLED.key(), this::reload);

        // Reload if provided services by Connect change.
        nzyme.getRegistryChangeMonitor()
                .onChange("core", ConnectRegistryKeys.PROVIDED_SERVICES.key(), this::reload);

        refresher = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("environment-refresher-%d")
                        .build()
        );

        refresher.scheduleAtFixedRate(this::reload, 30, 30, TimeUnit.MINUTES);
    }

    private void reload() {
        // Reload with new registry settings.
        initialize();
    }

    public void initialize() {
        // IMPORTANT: This method will also be called on configuration changes.

        this.isEnabled = nzyme.getConnect().isEnabled();
        if (!this.isEnabled) {
            return;
        }

        lock.lock();

        try {
            Optional<Map<UUID, EnvironmentData>> data = fetchEnvironmentFromConnect();

            // Check if environment data was disabled in Connect for this cluster.
            if (data.isEmpty()) {
                this.isEnabled = false;
                return;
            }

            this.isEnabled = true;
            this.environmentData = data.get();
        } catch (Exception e) {
            LOG.error("Could not download environment data from Connect.", e);
            this.isEnabled = false;
        } finally {
            lock.unlock();
        }
    }

    private Optional<Map<UUID, EnvironmentData>> fetchEnvironmentFromConnect() {
        Map<UUID, EnvironmentData> data = Maps.newHashMap();

        for (TenantEntry tenant : nzyme.getAuthenticationService().findAllTenantsOfAllOrganizations()) {
            for (TenantLocationEntry location : nzyme.getAuthenticationService()
                    .findAllTenantLocations(tenant.organizationUuid(), tenant.uuid(), Integer.MAX_VALUE, 0)) {

                if (location.latitude() == null || location.longitude() == null
                        || location.latitude() == 0.0 || location.longitude() == 0.0) {
                    continue;
                }

                LOG.debug("Loading new environment data from Connect for location [{}].", location.uuid());

                try {
                    OkHttpClient c = new OkHttpClient.Builder()
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(5, TimeUnit.MINUTES)
                            .followRedirects(true)
                            .build();

                    HttpUrl url = HttpUrl.get(nzyme.getConnect().getApiUri())
                            .newBuilder()
                            .addPathSegment("data")
                            .addPathSegment("environment")
                            .addPathSegment("location")
                            .addQueryParameter("latitude", String.valueOf(location.latitude()))
                            .addQueryParameter("longitude", String.valueOf(location.longitude()))
                            .build();

                    Response response = c.newCall(new Request.Builder()
                            .addHeader("User-Agent", "nzyme")
                            .get()
                            .url(url)
                            .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + nzyme.getConnect().getApiKey())
                            .addHeader("Content-Type", "application/json")
                            .addHeader(HttpHeaders.USER_AGENT, "nzyme-node")
                            .build()
                    ).execute();

                    try (response) {
                        if (!response.isSuccessful()) {
                            if (response.code() == 403) {
                                // Environment data disabled in Connect for this cluster.
                                return Optional.empty();
                            }

                            if (response.code() == 400) {
                                // No environmental data available for location.
                                continue;
                            }

                            throw new RuntimeException("Expected HTTP 200 or 403 but got HTTP " + response.code());
                        }

                        LOG.debug("Environment data download from Connect complete.");

                        ObjectMapper om = JsonMapper.builder()
                                .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
                                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                                .addModule(new JodaModule())
                                .build();

                        data.put(location.uuid(), om.readValue(response.body().bytes(), EnvironmentData.class));
                    }
                } catch (Exception e) {
                    LOG.error("Could not download environment data from Connect for location [{}].", location.uuid(), e);
                    continue;
                }
            }
        }

        return Optional.of(data);
    }

    public Optional<EnvironmentData> getEnvironmentData(UUID locationId) {
        if (!isEnabled) {
            return Optional.empty();
        }

        EnvironmentData data = environmentData.get(locationId);

        if (data == null) {
            return Optional.empty();
        } else {
            return Optional.of(data);
        }
    }

}
