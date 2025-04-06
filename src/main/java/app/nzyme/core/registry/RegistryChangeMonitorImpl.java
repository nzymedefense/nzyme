package app.nzyme.core.registry;

import app.nzyme.core.NzymeNode;
import app.nzyme.plugin.RegistryChangeMonitor;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RegistryChangeMonitorImpl implements RegistryChangeMonitor {

    private static final Logger LOG = LogManager.getLogger(RegistryChangeMonitorImpl.class);

    private final NzymeNode nzyme;

    private final Map<String, List<Runnable>> subscribers;

    private Map<String, String> snapshot = null;

    private final List<String> ignoredKeys;

    public RegistryChangeMonitorImpl(NzymeNode nzyme) {
        this.nzyme = nzyme;

        this.ignoredKeys = new ArrayList<>(){{
            add("core.connect_last_successful_report");
        }};

        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                        .setNameFormat("registry-change-processor-%d")
                        .build())
                .scheduleAtFixedRate(this::processChanges, 0, 5, TimeUnit.SECONDS);

        this.subscribers = Maps.newHashMap();
    }

    private void processChanges() {
        if (snapshot == null) {
            // First run. Fill image.
            snapshot = takeSnapshot();

            // Exit. (No changes on first run)
            return;
        }

        Map<String, String> previous = Maps.newHashMap(this.snapshot);

        // Create new snapshot. Do this immediately to avoid missing changes that happen during processing.
        this.snapshot = takeSnapshot();

        Map<String, String> current = takeSnapshot();

        for (Map.Entry<String, String> entry : current.entrySet()) {
            if (ignoredKeys.contains(entry.getKey())) {
                continue;
            }

            if (!previous.containsKey(entry.getKey())) {
                // New entry.
                LOG.info("Registry key [{}] now has a value. Notifying subscribers.", entry.getKey());
                notifyChange(entry.getKey());
            } else {
                // Existing entry. Compare.
                if (!entry.getValue().equals(previous.get(entry.getKey()))) {
                    // Value changed.
                    LOG.info("Registry key [{}] has a new value. Notifying subscribers.", entry.getKey());
                    notifyChange(entry.getKey());
                }
            }
        }

        for (Map.Entry<String, String> entry : previous.entrySet()) {
            if (ignoredKeys.contains(entry.getKey())) {
                continue;
            }
            // Did a value disappear?
            if (!current.containsKey(entry.getKey())) {
                LOG.info("Registry key [{}] has disappeared. Notifying subscribers.", entry.getKey());
                notifyChange(entry.getKey());
            }
        }
    }

    private void notifyChange(String key) {
        /*
         * WARNING:
         *
         * Never return values here to avoid leaking of out-of-namespace keys.
         * Everything can subscribe to all changes by design, including plugins.
         */

        List<Runnable> reactions = subscribers.get(key);

        for (Runnable reaction : reactions) {
            if (reaction != null) {
                try {
                    reaction.run();
                } catch (Exception e) {
                    LOG.error("Could not execute registry change reaction.", e);
                }
            }
        }
    }

    private Map<String, String> takeSnapshot() {
        List<RegistryEntry> entries = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT key, value FROM registry")
                        .mapTo(RegistryEntry.class)
                        .list()
        );

        List<RegistryEntry> encryptedEntries = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT key, value FROM registry_encrypted")
                        .mapTo(RegistryEntry.class)
                        .list()
        );

        Map<String, String> result = Maps.newHashMap();

        for (RegistryEntry entry : entries) {
            result.put(entry.key(), entry.value());
        }

        for (RegistryEntry entry : encryptedEntries) {
            result.put(entry.key(), entry.value());
        }

        return result;
    }

    @Override
    public void onChange(String namespace, String key, Runnable runnable) {
        String namespacedKey = RegistryImpl.buildNamespacedKey(namespace, key);

        if (!subscribers.containsKey(namespacedKey)) {
            subscribers.put(namespacedKey, Lists.newArrayList());
        }

        subscribers.get(namespacedKey).add(runnable);
    }

}
