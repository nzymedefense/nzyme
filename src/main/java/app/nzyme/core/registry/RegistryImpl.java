package app.nzyme.core.registry;

import app.nzyme.plugin.Registry;
import app.nzyme.plugin.RegistryCryptoException;
import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;
import app.nzyme.core.NzymeNode;
import app.nzyme.core.crypto.Crypto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class RegistryImpl implements Registry {

    private static final Logger LOG = LogManager.getLogger(RegistryImpl.class);

    private final NzymeNode nzyme;
    private final String namespace;


    public RegistryImpl(NzymeNode nzyme, String namespace) {
        this.nzyme = nzyme;
        this.namespace = namespace;
    }

    @Override
    public Optional<String> getValue(String key) {
        LOG.debug("Getting value for [{}] from registry.", buildNamespacedKey(namespace, key));
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT value FROM registry WHERE key = :key " +
                                "AND organization_id IS NULL AND tenant_id IS NULL")
                        .bind("key", buildNamespacedKey(namespace, key))
                        .mapTo(String.class)
                        .findOne()
        );
    }

    @Override
    public Optional<String> getValue(String key, UUID organizationId, UUID tenantId) {
        LOG.debug("Getting value for [{}] (Org <{}>, Tenant <{}>) from registry.",
                buildNamespacedKey(namespace, key), organizationId, tenantId);
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT value FROM registry WHERE key = :key " +
                                "AND organization_id = :organization_id AND tenant_id = :tenant_id")
                        .bind("key", buildNamespacedKey(namespace, key))
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .mapTo(String.class)
                        .findOne()
        );
    }

    @Override
    public Optional<String> getEncryptedValue(String key) throws RegistryCryptoException {
        LOG.debug("Getting encrypted value for [{}] from registry.", buildNamespacedKey(namespace, key));
        Optional<String> encrypted = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT value FROM registry_encrypted WHERE key = :key")
                        .bind("key", buildNamespacedKey(namespace, key))
                        .mapTo(String.class)
                        .findOne()
        );

        return dbResultToResult(key, encrypted);
    }

    @Override
    public Optional<String> getEncryptedValue(String key, UUID organizationId, UUID tenantId) throws RegistryCryptoException {
        LOG.debug("Getting encrypted value for [{}] (Org <{}>, Tenant <{}>) from registry.",
                buildNamespacedKey(namespace, key), organizationId, tenantId);

        Optional<String> encrypted = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT value FROM registry_encrypted WHERE key = :key " +
                                "AND organization_id = :organization_id AND tenant_id = :tenant_id")
                        .bind("key", buildNamespacedKey(namespace, key))
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .mapTo(String.class)
                        .findOne()
        );

        return dbResultToResult(key, encrypted);
    }

    private Optional<String> dbResultToResult(String key, Optional<String> encrypted) throws RegistryCryptoException {
        if (encrypted.isEmpty()) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(
                        new String(nzyme.getCrypto().decryptWithClusterKey(
                                BaseEncoding.base64().decode(encrypted.get())
                        ), Charsets.UTF_8)
                );
            } catch (Crypto.CryptoOperationException e) {
                throw new RegistryCryptoException("Could not decrypt registry value for key [" + key + "]", e);
            }
        }
    }

    @Override
    @Nullable
    public String getValueOrNull(String key) {
        return getValue(key).orElse(null);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public String getValueOrNull(String key, UUID organizationId, UUID tenantId) {
        return getValue(key, organizationId, tenantId).orElse(null);
    }

    @Override
    @Nullable
    public String getEncryptedValueOrNull(String key)  throws RegistryCryptoException {
        return getEncryptedValue(key).orElse(null);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public String getEncryptedValueOrNull(String key, UUID organizationId, UUID tenantId) throws RegistryCryptoException {
        return getEncryptedValue(key, organizationId, tenantId).orElse(null);
    }

    @Override
    public void setValue(String key, String value) {
        setValuePreflightChecks(key, value);

        if (getValue(key).isPresent()) {
            // Update existing entry.
            LOG.debug("Updating existing value for key [{}] in registry.", buildNamespacedKey(namespace, key));

            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("UPDATE registry SET value = :value WHERE key = :key")
                            .bind("key", buildNamespacedKey(namespace, key))
                            .bind("value", value)
                            .execute()
            );
        } else {
            // Insert new entry.
            LOG.debug("Inserting new entry for key [{}] in registry.", buildNamespacedKey(namespace, key));

            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("INSERT INTO registry(key, value) VALUES(:key, :value)")
                            .bind("key", buildNamespacedKey(namespace, key))
                            .bind("value", value)
                            .execute()
            );
        }
    }

    @Override
    public void setValue(String key, String value, UUID organizationId, UUID tenantId) {
        setValuePreflightChecks(key, value);

        if (getValue(key, organizationId, tenantId).isPresent()) {
            // Update existing entry.
            LOG.debug("Updating existing value for key [{}] (Org <{}>, Tenant <{}>) in registry.",
                    buildNamespacedKey(namespace, key), organizationId, tenantId);

            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("UPDATE registry SET value = :value WHERE key = :key " +
                                    "AND organization_id = :organization_id AND tenant_id = :tenant_id")
                            .bind("key", buildNamespacedKey(namespace, key))
                            .bind("value", value)
                            .bind("organization_id", organizationId)
                            .bind("tenant_id", tenantId)
                            .execute()
            );
        } else {
            // Insert new entry.
            LOG.debug("Inserting new entry for key [{}] (Org <{}>, Tenant <{}>) in registry.",
                    buildNamespacedKey(namespace, key), organizationId, tenantId);

            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("INSERT INTO registry(key, value, organization_id, tenant_id) " +
                                    "VALUES(:key, :value, :organization_id, :tenant_id)")
                            .bind("key", buildNamespacedKey(namespace, key))
                            .bind("value", value)
                            .bind("organization_id", organizationId)
                            .bind("tenant_id", tenantId)
                            .execute()
            );
        }
    }

    @Override
    public void setEncryptedValue(String key, String value) throws RegistryCryptoException {
        setValuePreflightChecks(key, value);

        String encrypted;
        String keyFingerprint;
        try {
            Crypto crypto = nzyme.getCrypto();
            encrypted = BaseEncoding.base64().encode(crypto.encryptWithClusterKey(value.getBytes(Charsets.UTF_8)));
            keyFingerprint = crypto.getLocalPGPKeyFingerprint();

            value = ""; // Just to make sure it's not accidentally used from here on.
        } catch (Crypto.CryptoOperationException e) {
            throw new RegistryCryptoException("Could not encrypt registry value for key [" + key + "]", e);
        }

        if (getEncryptedValue(key).isPresent()) {
            // Update existing entry.
            LOG.debug("Updating existing encrypted value for key [{}] in registry.", buildNamespacedKey(namespace, key));

            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("UPDATE registry_encrypted SET value = :value, key_signature = :key_signature " +
                                    "WHERE key = :key")
                            .bind("key", buildNamespacedKey(namespace, key))
                            .bind("value", encrypted)
                            .bind("key_signature", keyFingerprint)
                            .execute()
            );
        } else {
            // Insert new entry.
            LOG.debug("Inserting new encrypted entry for key [{}] in registry.", buildNamespacedKey(namespace, key));
            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("INSERT INTO registry_encrypted(key, value, key_signature) " +
                                    "VALUES(:key, :value, :key_signature)")
                            .bind("key", buildNamespacedKey(namespace, key))
                            .bind("value", encrypted)
                            .bind("key_signature", keyFingerprint)
                            .execute()
            );
        }
    }

    @Override
    public void setEncryptedValue(String key, String value, UUID organizationId, UUID tenantId) throws RegistryCryptoException {
        throw new RuntimeException("Encrypted database registry with tenant scope not implemented yet.");
    }

    @Override
    public void deleteValue(String key) {
        LOG.debug("Deleting registry value for key [{}]", key);
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM registry WHERE key = :key")
                        .bind("key", buildNamespacedKey(namespace, key))
                        .execute()
        );
    }

    @Override
    public void deleteValue(String key, UUID organizationId, UUID tenantId) {
        LOG.debug("Deleting registry value for key [{}] (Org <{}>, Tenant <{}>)",
                key, organizationId, tenantId);
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM registry WHERE key = :key " +
                                "AND organization_id = :organization_id AND tenant_id = :tenant_id")
                        .bind("key", buildNamespacedKey(namespace, key))
                        .bind("organization_id", organizationId)
                        .bind("tenant_id", tenantId)
                        .execute()
        );
    }

    private void setValuePreflightChecks(String key, String value) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty or null registry key.");
        }

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty or null registry value for key [" + buildNamespacedKey(namespace, key) + "].");
        }

        if (buildNamespacedKey(namespace, key).length() > 128) {
            LOG.error("Registry key length cannot exceed 128 characters. Provided <{}> characters: {}",
                    buildNamespacedKey(namespace, key).length(), buildNamespacedKey(namespace, key));

            throw new IllegalArgumentException("Key length exceeded.");
        }
    }

    public static String buildNamespacedKey(String namespace, String key) {
        return namespace + "." + key;
    }

}
