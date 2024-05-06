package app.nzyme.core.distributed;

import app.nzyme.plugin.RegistryKey;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraint;

import java.util.ArrayList;
import java.util.Optional;

public class NodeRegistryKeys {

    public static final RegistryKey EPHEMERAL_NODES_REGEX = RegistryKey.create(
            "ephemeral_nodes_regex",
            Optional.of(new ArrayList<>() {{
                ConfigurationEntryConstraint.createStringLengthConstraint(1, 255);
            }}),
            Optional.empty(),
            false
    );

    public static final RegistryKey VERSIONCHECK_STATUS = RegistryKey.create(
            "versioncheck_status",
            Optional.of(new ArrayList<>() {{
                ConfigurationEntryConstraint.createSimpleBooleanConstraint();
            }}),
            Optional.empty(),
            false
    );

}
