package app.nzyme.core.monitoring.health;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.monitoring.health.db.IndicatorStatus;
import app.nzyme.core.monitoring.health.indicators.*;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HealthMonitor {

    private static final Logger LOG = LogManager.getLogger(HealthMonitor.class);

    private final NzymeNode nzyme;

    public HealthMonitor(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public void initialize() {
        Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setNameFormat("node-metrics-updater-%d")
                        .setDaemon(true)
                        .build()
        ).scheduleAtFixedRate(this::runChecks, 0, 1, TimeUnit.MINUTES);
    }

    private void runChecks() {
        LOG.debug("Running health monitor checks.");

        // Add all checks/indicators here.
        List<Indicator> indicators = new ImmutableList.Builder<Indicator>()
                .add(new NodeClockIndicator(nzyme.getNodeManager()))
                .add(new CryptoSyncIndicator(nzyme.getCrypto()))
                .add(new DatabaseClockIndicator(nzyme.getDatabase(), nzyme.getConfiguration().ntpServer()))
                .add(new TapClockIndicator(nzyme.getTapManager()))
                .add(new NodeOfflineIndicator(nzyme.getNodeManager()))
                .add(new TapOfflineIndicator(nzyme.getTapManager()))
                .add(new TapThroughputIndicator(nzyme.getTapManager()))
                .build();

        for (Indicator indicator : indicators) {
            try {
                IndicatorStatus status = indicator.run();

                // Write to database.
                nzyme.getDatabase().useHandle(handle ->
                        handle.createUpdate("INSERT INTO health_indicators(indicator_id, indicator_name, level, " +
                                        "last_checked) VALUES(:indicator_id, :indicator_name, :level, :last_checked) " +
                                        "ON CONFLICT(indicator_id) DO UPDATE SET indicator_name = :indicator_name, " +
                                        "level = :level, last_checked = :last_checked")
                                .bind("indicator_id", indicator.getId())
                                .bind("indicator_name", indicator.getName())
                                .bind("level", status.resultLevel().toString().toUpperCase())
                                .bind("last_checked", DateTime.now())
                                .execute()
                );
            } catch(Exception e) {
                LOG.error("Could not run health check indicator. Skipping.", e);
            }
        }

        LOG.debug("Finished running health monitor checks.");
    }

    public Optional<List<IndicatorStatus>> getIndicatorStatus() {
        List<IndicatorStatus> result = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT indicator_id, indicator_name, level, last_checked FROM health_indicators")
                        .mapTo(IndicatorStatus.class)
                        .list()
        );

        if (result == null || result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(result);
    }

}
