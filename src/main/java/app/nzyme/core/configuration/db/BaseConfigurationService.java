package app.nzyme.core.configuration.db;

import app.nzyme.core.NzymeNode;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.concurrent.atomic.AtomicReference;

public class BaseConfigurationService {

    private static final Logger LOG = LogManager.getLogger(BaseConfigurationService.class);

    private final NzymeNode nzyme;
    private final AtomicReference<BaseConfiguration> cache;

    private boolean initialized = false;

    public BaseConfigurationService(NzymeNode nzyme) {
        this.nzyme = nzyme;

        this.cache = new AtomicReference<>(null);
    }

    public void initialize() {
        // There should always only be one row.
        long count = nzyme.getDatabase().withHandle(handle ->
            handle.createQuery("SELECT COUNT(*) FROM base_configuration")
                    .mapTo(Long.class)
                    .first()
        );

        if (count == 0) {
            // Create initial row.
            LOG.info("Creating initial default base configuration.");

            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("INSERT INTO base_configuration(tap_secret, updated_at) " +
                            "VALUES(:tap_secret, :timestamp)")
                            .bind("tap_secret", generateTapSecret())
                            .bind("timestamp", DateTime.now())
                            .execute()
            );
        }

        if (count > 1) {
            LOG.warn("More than one base configuration detected. This should never happen and can cause issues.");
        }

        invalidateAndUpdateCache();

        initialized = true;
    }

    public BaseConfiguration getConfiguration() {
        return cache.get();
    }

    public void setTapSecret(String secret) {
        nzyme.getDatabase().useHandle(handle -> {
            handle.createUpdate("UPDATE base_configuration SET " +
                            "tap_secret = :tap_secret, " +
                            "updated_at = (current_timestamp at time zone 'UTC')")
                    .bind("tap_secret", secret)
                    .execute();
        });

        invalidateAndUpdateCache();
    }

    public boolean isInitialized() {
        return initialized;
    }

    public String generateTapSecret() {
        return RandomStringUtils.random(64, true, true);
    }

    private void invalidateAndUpdateCache() {
        BaseConfiguration config = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT tap_secret, updated_at FROM base_configuration LIMIT 1")
                        .mapTo(BaseConfiguration.class)
                        .one()
        );

        if (config == null) {
            LOG.fatal("!!!!!!!! No base configuration found. This is an unrecoverable error. !!!!!!!!");
        }

        this.cache.set(config);
    }

}
