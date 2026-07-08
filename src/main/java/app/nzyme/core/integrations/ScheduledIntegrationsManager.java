package app.nzyme.core.integrations;

import app.nzyme.core.NzymeNode;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ScheduledIntegrationsManager {

    private static final Logger LOG = LogManager.getLogger(ScheduledIntegrationsManager.class);

    private final NzymeNode nzyme;
    private final ScheduledExecutorService executor;

    public ScheduledIntegrationsManager(NzymeNode nzyme) {
        this.nzyme = nzyme;

        this.executor = Executors.newScheduledThreadPool(5,
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("sched-integrations-%d")
                        .setUncaughtExceptionHandler((thread, throwable) ->
                                LOG.error("Uncaught exception in a scheduled integration", throwable))
                        .build()
        );
    }

    public void initialize() {
        LOG.info("Initializing scheduled integrations");

        // TODO for future integrations
    }

}
