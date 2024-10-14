package app.nzyme.core.subsystems;

import app.nzyme.core.NzymeNode;
import app.nzyme.plugin.RegistryKey;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

import java.util.Optional;
import java.util.UUID;

@AutoValue
public class Subsystems {

    private final NzymeNode nzyme;

    public Subsystems(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public boolean isEnabled(Subsystem subsystem, @Nullable UUID organizationId, @Nullable UUID tenantId) {
        RegistryKey key;
        switch (subsystem) {
            case DOT11 -> key = SubsystemRegistryKeys.DOT11_ENABLED;
            case ETHERNET -> key = SubsystemRegistryKeys.ETHERNET_ENABLED;
            case BLUETOOTH -> key = SubsystemRegistryKeys.BLUETOOTH_ENABLED;
            default -> throw new RuntimeException("Subsystem [" + subsystem + "] cannot be enabled/disabled.");
        }

        if (organizationId != null || tenantId != null) {
            throw new RuntimeException("NOT SUPPORTED YET."); // TODO implement org selection in registry APIs.
        }

        Optional<String> value;
        if (organizationId == null && tenantId == null) {
            // Superadmin / System setting.
            value = nzyme.getDatabaseCoreRegistry().getValue(key.key());
        } else if (organizationId != null && tenantId == null) {
            // Organization setting. TODO
            value = Optional.empty();
        } else {
            // Tenant setting.
            value = nzyme.getDatabaseCoreRegistry().getValue(key.key(), organizationId, tenantId);
        }

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
