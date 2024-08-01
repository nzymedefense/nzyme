package app.nzyme.core.bluetooth.sig;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.connect.ConnectRegistryKeys;
import app.nzyme.core.util.MetricNames;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.net.HttpHeaders;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class BluetoothSigService {

    private static final Logger LOG = LogManager.getLogger(BluetoothSigService.class);

    private final NzymeNode nzyme;
    private final Timer lookupTimer;

    private final ScheduledExecutorService refresher;

    private final ReentrantLock lock = new ReentrantLock();
    private Map<Integer, String> companyIds;

    // Can be disabled if Connect is not set up or BT SIG data source is not enabled in Connect.
    private boolean isEnabled = false;

    public BluetoothSigService(NzymeNode nzyme) {
        this.nzyme = nzyme;
        this.lookupTimer = nzyme.getMetrics().timer(MetricRegistry.name(MetricNames.BTSIG_CID_LOOKUP_TIMING));

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
                        .setNameFormat("btsig-refresher-%d")
                        .build()
        );

        refresher.scheduleAtFixedRate(this::reload, 1, 1, TimeUnit.HOURS);
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
            Optional<Map<Integer, String>> data = fetchCompanyIdsFromConnect();

            // Check if BT SIG data was disabled in Connect for this cluster.
            if (data.isEmpty()) {
                this.isEnabled = false;
                return;
            }

            this.companyIds = data.get();
            this.isEnabled = true;
        } catch (Exception e) {
            LOG.error("Could not download SIG Company ID data from Connect.", e);
            this.isEnabled = false;
        } finally {
            lock.unlock();
        }
    }

    public Optional<String> lookupCompanyId(int companyId) {
        if (!isEnabled) {
            return Optional.empty();
        }

        try(Timer.Context ignored = lookupTimer.time()) {
            lock.lock();

            try {
                String cid = companyIds.get(companyId);
                return cid == null ? Optional.empty() : Optional.of(cid);
            } finally {
                lock.unlock();
            }
        }
    }


    private Optional<Map<Integer, String>> fetchCompanyIdsFromConnect() {
        LOG.debug("Loading new Bluetooth SIG Company IDs from Connect.");

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
                    .addPathSegment("bluetooth")
                    .addPathSegment("companyids")
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
                        // BG SIG data disabled in Connect for this cluster.
                        return Optional.empty();
                    }

                    throw new RuntimeException("Expected HTTP 200 or 403 but got HTTP " + response.code());
                }


                if (response.body() == null) {
                    throw new RuntimeException("Empty response.");
                }

                LOG.debug("Bluetooth SIG Company ID data download from Connect complete.");

                ObjectMapper om = new ObjectMapper();
                om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                om.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

                ConnectCompanyIdListResponse ids = om.readValue(response.body().bytes(), ConnectCompanyIdListResponse.class);

                Map<Integer, String> table = Maps.newHashMap();
                for (ConnectCompanyIdResponse id : ids.companyIds()) {
                    table.put(id.companyId(), id.name());
                }

                return Optional.of(table);
            }
        } catch (Exception e) {
            LOG.error("Could not download SIG Company ID data from Connect.", e);
            return Optional.empty();
        }
    }

}
