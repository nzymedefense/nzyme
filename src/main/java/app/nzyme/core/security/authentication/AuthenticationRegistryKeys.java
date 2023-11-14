package app.nzyme.core.security.authentication;

import app.nzyme.plugin.RegistryKey;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraint;

import java.util.ArrayList;
import java.util.Optional;

public class AuthenticationRegistryKeys {

    public static final RegistryKey SESSION_TIMEOUT_MINUTES = RegistryKey.create(
            "session_timeout_minutes",
            Optional.of(new ArrayList<>(){{
                add(ConfigurationEntryConstraint.createNumberRangeConstraint(1, Integer.MAX_VALUE));
            }}),
            Optional.of("720"),
            false
    );

    public static final RegistryKey SESSION_INACTIVITY_TIMEOUT_MINUTES = RegistryKey.create(
            "session_inactivity_timeout_minutes",
            Optional.of(new ArrayList<>(){{
                add(ConfigurationEntryConstraint.createNumberRangeConstraint(1, Integer.MAX_VALUE));
            }}),
            Optional.of("15"),
            false
    );

    public static final RegistryKey MFA_TIMEOUT_MINUTES = RegistryKey.create(
            "mfa_timeout_minutes",
            Optional.of(new ArrayList<>(){{
                add(ConfigurationEntryConstraint.createNumberRangeConstraint(1, Integer.MAX_VALUE));
            }}),
            Optional.of("5"),
            false
    );


}
