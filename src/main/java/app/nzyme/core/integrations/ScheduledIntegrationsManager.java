package app.nzyme.core.integrations;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.integrations.tenant.cot.CotOutput;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ScheduledIntegrationsManager {

    private static final Logger LOG = LogManager.getLogger(ScheduledIntegrationsManager.class);

    private final NzymeNode nzyme;
    private final ScheduledExecutorService executor;

    private final CotOutput cot;

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

        this.cot = new CotOutput(nzyme);
    }

    public void initialize() {
        LOG.info("Initializing scheduled integrations");

        LOG.info("Initializing and scheduling Cursor on Target tenant integrations.");
        this.cot.initialize();
        executor.scheduleAtFixedRate(
                cot.execute(),
                cot.getConfiguration().initialDelay(),
                cot.getConfiguration().period(),
                cot.getConfiguration().timeUnit()
        );
    }

}
