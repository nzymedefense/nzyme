package horse.wtf.nzyme.registry;

import app.nzyme.plugin.Registry;
import com.google.common.base.Strings;
import horse.wtf.nzyme.NzymeLeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class RegistryImpl implements Registry {

    private static final Logger LOG = LogManager.getLogger(RegistryImpl.class);

    private final NzymeLeader nzyme;
    private final String namespace;

    public RegistryImpl(NzymeLeader nzyme, String namespace) {
        this.nzyme = nzyme;
        this.namespace = namespace;
    }

    @Override
    public Optional<String> getValue(String key) {
        LOG.debug("Getting value for [{}] from registry.", buildNamespacedKey(key));
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT value FROM registry WHERE key = :key")
                        .bind("key", buildNamespacedKey(key))
                        .mapTo(String.class)
                        .findOne()
        );
    }

    @Override
    public void setValue(String key, String value) {
        if (Strings.isNullOrEmpty(key)) {
            throw new IllegalArgumentException("Empty or null registry key.");
        }

        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException("Empty or null registry value for key [" + buildNamespacedKey(key) + "].");
        }

        if (buildNamespacedKey(key).length() > 128) {
            LOG.error("Registry key length cannot exceed 128 characters. Provided <{}> characters: {}",
                    buildNamespacedKey(key).length(), buildNamespacedKey(key));

            throw new IllegalArgumentException("Key length exceeded.");
        }

        if (buildNamespacedKey(value).length() > 255) {
            LOG.error("Registry value length cannot exceed 255 characters. Provided <{}> characters for key [{}].",
                    value.length(), buildNamespacedKey(key));

            throw new IllegalArgumentException("Value length exceeded.");
        }

        if (getValue(key).isPresent()) {
            // Update existing entry.
            LOG.debug("Updating existing value for key [{}] in registry.", buildNamespacedKey(key));

            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("UPDATE registry SET value = :value WHERE key = :key")
                            .bind("key", buildNamespacedKey(key))
                            .bind("value", value)
                            .execute()
            );
        } else {
            // Insert new entry.
            LOG.debug("Inserting new entry for key [{}] in registry.", buildNamespacedKey(key));

            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("INSERT INTO registry(key, value) VALUES(:key, :value)")
                            .bind("key", buildNamespacedKey(key))
                            .bind("value", value)
                            .execute()
            );
        }
    }

    private String buildNamespacedKey(String key) {
        return namespace + "." + key;
    }

}
