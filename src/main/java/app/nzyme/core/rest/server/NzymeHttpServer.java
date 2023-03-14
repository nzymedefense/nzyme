package app.nzyme.core.rest.server;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.NzymeNodeImpl;
import app.nzyme.core.crypto.tls.KeyStoreBootstrapResult;
import app.nzyme.core.distributed.messaging.Message;
import app.nzyme.core.distributed.messaging.MessageHandler;
import app.nzyme.core.distributed.messaging.MessageProcessingResult;
import app.nzyme.core.distributed.messaging.MessageType;
import app.nzyme.core.rest.CORSFilter;
import app.nzyme.core.rest.NzymeExceptionMapper;
import app.nzyme.core.rest.NzymeLeaderInjectionBinder;
import app.nzyme.core.rest.ObjectMapperProvider;
import app.nzyme.core.rest.authentication.PrometheusBasicAuthFilter;
import app.nzyme.core.rest.authentication.RESTAuthenticationFilter;
import app.nzyme.core.rest.authentication.TapAuthenticationFilter;
import app.nzyme.core.rest.interceptors.TapTableSizeInterceptor;
import app.nzyme.core.rest.resources.*;
import app.nzyme.core.rest.resources.assets.WebInterfaceAssetsResource;
import app.nzyme.core.rest.resources.authentication.AuthenticationResource;
import app.nzyme.core.rest.resources.ethernet.DNSResource;
import app.nzyme.core.rest.resources.monitoring.MonitoringResource;
import app.nzyme.core.rest.resources.monitoring.PrometheusResource;
import app.nzyme.core.rest.resources.system.*;
import app.nzyme.core.rest.resources.system.cluster.NodesResource;
import app.nzyme.core.rest.resources.taps.StatusResource;
import app.nzyme.core.rest.resources.taps.TablesResource;
import app.nzyme.core.rest.resources.taps.TapsResource;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.CompressionConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NzymeHttpServer {

    private static final Logger LOG = LogManager.getLogger(NzymeNodeImpl.class);

    private final NzymeNode nzyme;
    private final List<Object> pluginRestResources;

    private HttpServer server;

    public NzymeHttpServer(NzymeNode nzyme, List<Object> pluginRestResources) {
        this.nzyme = nzyme;
        this.pluginRestResources = pluginRestResources;
    }

    public void initialize() {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(new RESTAuthenticationFilter(nzyme));
        resourceConfig.register(new TapAuthenticationFilter(nzyme));
        resourceConfig.register(new PrometheusBasicAuthFilter(nzyme));
        resourceConfig.register(new CORSFilter());
        resourceConfig.register(new NzymeLeaderInjectionBinder(nzyme));
        resourceConfig.register(new ObjectMapperProvider());
        resourceConfig.register(new JacksonJaxbJsonProvider());
        resourceConfig.register(new NzymeExceptionMapper());
        resourceConfig.register(new TapTableSizeInterceptor(nzyme));
        resourceConfig.register(MultiPartFeature.class);

        // Register REST API resources.
        resourceConfig.register(AuthenticationResource.class);
        resourceConfig.register(PingResource.class);
        resourceConfig.register(AlertsResource.class);
        resourceConfig.register(BanditsResource.class);
        resourceConfig.register(ProbesResource.class);
        resourceConfig.register(TrackersResource.class);
        resourceConfig.register(NetworksResource.class);
        resourceConfig.register(SystemResource.class);
        resourceConfig.register(DashboardResource.class);
        resourceConfig.register(AssetInventoryResource.class);
        resourceConfig.register(ReportsResource.class);
        resourceConfig.register(StatusResource.class);
        resourceConfig.register(TablesResource.class);
        resourceConfig.register(TapsResource.class);
        resourceConfig.register(DNSResource.class);
        resourceConfig.register(PluginResource.class);
        resourceConfig.register(PrometheusResource.class);
        resourceConfig.register(CryptoResource.class);
        resourceConfig.register(MonitoringResource.class);
        resourceConfig.register(NodesResource.class);
        resourceConfig.register(HealthResource.class);
        resourceConfig.register(RegistryResource.class);

        // Plugin-supplied REST resources.
        for (Object resource : pluginRestResources) {
            try {
                resourceConfig.register(resource);
                LOG.info("Loaded plugin REST resource [{}].", resource.getClass().getCanonicalName());
            } catch(Exception e) {
                LOG.error("Could not register plugin REST resource [{}].", resource.getClass().getCanonicalName(), e);
            }
        }

        // Enable GZIP.
        resourceConfig.registerClasses(EncodingFilter.class, GZipEncoder.class, DeflateEncoder.class);

        // Register web interface asset resources.
        resourceConfig.register(WebInterfaceAssetsResource.class);

        try {
            KeyStoreBootstrapResult keyStore = nzyme.getCrypto().bootstrapTLSKeyStore();
            final SSLContextConfigurator sslContextConfigurator = new SSLContextConfigurator();
            sslContextConfigurator.setKeyStorePass("".toCharArray());
            sslContextConfigurator.setKeyStoreBytes(keyStore.keystoreBytes());
            final SSLContext sslContext = sslContextConfigurator.createSSLContext(true);
            SSLEngineConfigurator sslEngineConfigurator = new SSLEngineConfigurator(sslContext, false, false, false);

            server = GrizzlyHttpServerFactory.createHttpServer(
                    nzyme.getConfiguration().restListenUri(),
                    resourceConfig,
                    true,
                    sslEngineConfigurator
            );

            LOG.info("Final TLS type: [{}]", keyStore.loadSource());
        } catch(Exception e) {
            throw new RuntimeException("Could not start web server.", e);
        }

        CompressionConfig compressionConfig = server.getListener("grizzly").getCompressionConfig();
        compressionConfig.setCompressionMode(CompressionConfig.CompressionMode.ON);
        compressionConfig.setCompressionMinSize(1);
        compressionConfig.setCompressibleMimeTypes();

        // Start server.
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException("Could not start REST API.", e);
        }

        // Register message handler for requested server restarts.
        nzyme.getMessageBus().onMessageReceived(MessageType.CHECK_RESTART_HTTP_SERVER, new MessageHandler() {
            @Override
            public MessageProcessingResult handle(Message message) {
                // TODO move this to a separate class file. Will have logic to determine if restart is required or not.
                // has to work with all types. wildcard, etc. - do the whole load order bootstrap.
                // move http server classes into own files? this is too much here.
                LOG.info("HANDLING EVENT.");
                return MessageProcessingResult.SUCCESS;
            }

            @Override
            public String getName() {
                return "Check for required HTTP server restart after TLS configuration change.";
            }
        });

        LOG.info("Started web interface and REST API at [{}]. Access it at: [{}]",
                nzyme.getConfiguration().restListenUri(),
                nzyme.getConfiguration().httpExternalUri());
    }

    public void reloadHttpServer(int gracePeriod, TimeUnit tu) {
        Executors.newSingleThreadExecutor().submit(() -> {
            LOG.info("Restarting HTTP server.");
            server.shutdown(gracePeriod, tu);
            initialize();
        });
    }

    public void shutdownNow() {
        server.shutdownNow();
    }

}
