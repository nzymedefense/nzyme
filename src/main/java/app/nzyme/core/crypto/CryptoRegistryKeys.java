package app.nzyme.core.crypto;

import app.nzyme.plugin.RegistryKey;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraint;

import java.util.ArrayList;
import java.util.Optional;

public class CryptoRegistryKeys {

    public static final RegistryKey PGP_KEY_SYNC_ENABLED = RegistryKey.create(
            "pgp_key_sync_enabled",
            Optional.of(new ArrayList<>(){{
                add(ConfigurationEntryConstraint.createSimpleBooleanConstraint());
            }}),
            Optional.of("true"),
            false
    );


}
