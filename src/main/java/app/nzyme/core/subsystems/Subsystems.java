package app.nzyme.core.subsystems;

import app.nzyme.core.NzymeNode;
import app.nzyme.plugin.RegistryKey;
import jakarta.annotation.Nullable;

import java.util.Optional;
import java.util.UUID;

public class Subsystems {

    private final NzymeNode nzyme;

    public Subsystems(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public boolean isEnabled(Subsystem subsystem, @Nullable UUID organizationId, @Nullable UUID tenantId) {
        if (organizationId == null && tenantId == null) {
            // Superadmin / System setting.
            return isGloballyEnabled(subsystem);
        } else if (organizationId != null && tenantId == null) {
            // Organization setting.
            return isGloballyEnabled(subsystem)
                    && isEnabledForOrganization(subsystem, organizationId);
        } else {
            // Tenant setting.
            return isGloballyEnabled(subsystem)
                    && isEnabledForOrganization(subsystem, organizationId)
                    && isEnabledForTenant(subsystem, organizationId, tenantId);
        }
    }

    private boolean isGloballyEnabled(Subsystem subsystem) {
        RegistryKey key = subsystemToRegistryKey(subsystem);
        Optional<String> value = nzyme.getDatabaseCoreRegistry().getValue(key.key());

        return getValue(key, value);
    }

    private boolean isEnabledForOrganization(Subsystem subsystem, UUID organizationId) {
        RegistryKey key = subsystemToRegistryKey(subsystem);
        Optional<String> value = nzyme.getDatabaseCoreRegistry().getValue(key.key(), organizationId);

        return getValue(key, value);
    }

    private boolean isEnabledForTenant(Subsystem subsystem, UUID organizationId, UUID tenantId) {
        RegistryKey key = subsystemToRegistryKey(subsystem);
        Optional<String> value = nzyme.getDatabaseCoreRegistry().getValue(key.key(), organizationId, tenantId);

        return getValue(key, value);
    }

    private RegistryKey subsystemToRegistryKey(Subsystem subsystem) {
        switch (subsystem) {
            case DOT11 -> {
                return SubsystemRegistryKeys.DOT11_ENABLED;
            }
            case ETHERNET -> {
                return SubsystemRegistryKeys.ETHERNET_ENABLED;
            }
            case BLUETOOTH -> {
                return SubsystemRegistryKeys.BLUETOOTH_ENABLED;
            }
            default -> throw new RuntimeException("Subsystem [" + subsystem + "] cannot be enabled/disabled.");
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private boolean getValue(RegistryKey key, Optional<String> value) {
        if (value.isPresent()) {
            return Boolean.parseBoolean(value.get());
        } else {
            if (key.defaultValue().isPresent()) {
                return Boolean.parseBoolean(key.defaultValue().get());
            } else {
                return false;
            }
        }
    }

}
