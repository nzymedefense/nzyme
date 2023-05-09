package app.nzyme.core.integrations.geoip.ipinfo;

import app.nzyme.core.configuration.base.BaseConfiguration;
import app.nzyme.core.integrations.geoip.GeoIpAdapter;
import app.nzyme.core.integrations.geoip.GeoIpAsnInformation;
import app.nzyme.core.integrations.geoip.GeoIpGeoInformation;
import app.nzyme.core.integrations.geoip.GeoIpLookupResult;
import app.nzyme.core.integrations.geoip.ipinfo.mmdb.FreeCountryAsnLookupResult;
import app.nzyme.plugin.RegistryKey;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.maxmind.db.Reader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public class IpInfoFreeGeoIpAdapter implements GeoIpAdapter {

    private static final Logger LOG = LogManager.getLogger(IpInfoFreeGeoIpAdapter.class);

    public static final RegistryKey REGISTRY_KEY_TOKEN = RegistryKey.create(
            "geoipprov_ipinfo_api_key",
            Optional.empty(),
            Optional.empty(),
            true
    );

    private final URL mmdbUri;
    private final Path mmdbPath;
    private final File mmdb;

    private final ScheduledExecutorService updater = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("geo-ipinfofree-updater-%d")
                    .build()
    );
    private Reader mmdbReader;

    private boolean paused = false;

    public IpInfoFreeGeoIpAdapter(String token, BaseConfiguration baseConfiguration) {
        mmdbPath = Path.of(baseConfiguration.dataDirectory(), "geo_ipinfo.mmdb");
        mmdb = mmdbPath.toFile();

        LOG.info("IPinfo MMDB file is at [{}].", mmdbPath);

        try {
            this.mmdbUri = new URIBuilder("https://ipinfo.io/data/free/country_asn.mmdb")
                    .addParameter("token", token)
                    .build()
                    .toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            LOG.error("Could not initialize IPinfo Geo IP adapter.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize() {
        LOG.info("Initializing IPinfo Geo IP adapter.");

        this.updater.scheduleAtFixedRate(() -> {
            try {
                // This will only actually download if file is outdated.
                checkMMDB();
            } catch(Exception e) {
                LOG.error("Could not refresh geo IP data file.", e);
            }
        }, 1, 1, TimeUnit.MINUTES);

        try {
            checkMMDB();

            try {
                this.mmdbReader = new Reader(mmdb);
            } catch (IOException e) {
                LOG.error("Could not create MMDB database reader.", e);
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            LOG.error("Could not initialize IPinfo Geo IP adapter.", e);
        }
    }

    @Override
    public void shutdown() {
        if (this.mmdbReader != null) {
            try {
                this.mmdbReader.close();
            } catch (IOException e) {
                LOG.error("Could not close MMDB reader.", e);
            }
        }

        this.updater.shutdownNow();
    }

    @Override
    public Optional<GeoIpLookupResult> lookup(InetAddress address) {
        while(paused) {
            /*
             * Spin if lookups are paused. This can happen while MMDB file is replaced. A full mutex on this method
             * would be way too expensive so going the stupid manual route.
             */
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) { /* noop */ }
        }

        try {
            FreeCountryAsnLookupResult lookup = mmdbReader.get(address, FreeCountryAsnLookupResult.class);

            if (lookup == null) {
                return Optional.empty();
            }

            GeoIpGeoInformation geo = GeoIpGeoInformation.create(
                    null,
                    lookup.getCountryCode(),
                    lookup.getCountryName(),
                    null,
                    null
            );

            Long asNumber;
            if (lookup.getAsNumber() != null) {
                asNumber = Long.parseLong(lookup.getAsNumber().split("^AS")[1]);
            } else {
                asNumber = null;
            }

            GeoIpAsnInformation asn = GeoIpAsnInformation.create(
                    asNumber,
                    lookup.getAsName(),
                    lookup.getAsDomain()
            );

            return Optional.of(GeoIpLookupResult.create(asn, geo));
        } catch (Exception e) {
            LOG.info("Could not look up IP address [{}].", address, e);
            return Optional.empty();
        }
    }

    @Override
    public String getName() {
        return "ipinfo_free";
    }

    private void checkMMDB() throws IOException {
        if (mmdb.exists()) {
            // MMDB exists. Is it up to date?
            BasicFileAttributes fa = Files.readAttributes(mmdbPath, BasicFileAttributes.class);
            if(new DateTime(fa.creationTime().toMillis()).isBefore(DateTime.now().minusHours(12))) {
                // MMDB is outdated. Download.
                LOG.info("MMDB exists but is out of date. Downloading new copy.");
                downloadAndStoreMmdb();
            } else {
                LOG.debug("MMDB exists and is up to date.");
            }
        } else {
            LOG.info("MMDB does not exist. Downloading new copy.");
            downloadAndStoreMmdb();
        }
    }

    private void downloadAndStoreMmdb() throws IOException {
        LOG.info("Downloading IPinfo MMDB.");

        OkHttpClient c = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.MINUTES)
                .followRedirects(true)
                .build();

        Response response = c.newCall(new Request.Builder()
                        .addHeader("User-Agent", "nzyme")
                        .get()
                        .url(this.mmdbUri)
                        .build())
                .execute();

        try (response) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Expected HTTP 200 but got HTTP " + response.code());
            }
            if (response.body() == null) {
                throw new RuntimeException("Empty response.");
            }
            LOG.info("Download complete. Writing to file [{}].", mmdbPath);

            paused = true;
            Files.write(mmdbPath, response.body().bytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } finally {
            paused = false;
        }

        LOG.info("Finished.");
    }

}
