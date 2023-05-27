package app.nzyme.core.integrations.smtp;

import app.nzyme.plugin.EncryptedRegistryKey;
import app.nzyme.plugin.RegistryKey;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraint;

import java.util.ArrayList;
import java.util.Optional;

public class SMTPConfigurationRegistryKeys {

    public static RegistryKey TRANSPORT_STRATEGY = RegistryKey.create(
            "smtp_transport_strategy",
            Optional.of(new ArrayList<>(){{
                add(ConfigurationEntryConstraint.createEnumStringsConstraint(
                        new ArrayList<>(){{
                            add("SMTP");
                            add("SMTPS");
                            add("SMTP TLS");
                        }}
                ));
            }}),
            Optional.empty(),
            false
    );

    public static RegistryKey HOST = RegistryKey.create(
            "smtp_host",
            Optional.empty(),
            Optional.empty(),
            false
    );

    public static RegistryKey PORT = RegistryKey.create(
            "smtp_port",
            Optional.of(new ArrayList<>(){{
                add(ConfigurationEntryConstraint.createNumberRangeConstraint(1, 65536));
            }}),
            Optional.empty(),
            false
    );

    public static RegistryKey USERNAME = RegistryKey.create(
            "smtp_username",
            Optional.empty(),
            Optional.empty(),
            false
    );

    public static EncryptedRegistryKey PASSWORD = EncryptedRegistryKey.create(
            "smtp_password",
            Optional.empty(),
            false
    );

    public static RegistryKey FROM_ADDRESS = RegistryKey.create(
            "smtp_from_address",
            Optional.empty(),
            Optional.empty(),
            false
    );

}
