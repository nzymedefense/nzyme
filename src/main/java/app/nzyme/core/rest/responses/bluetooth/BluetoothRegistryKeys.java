package app.nzyme.core.rest.responses.bluetooth;

import app.nzyme.plugin.RegistryKey;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraint;

import java.util.ArrayList;
import java.util.Optional;

public class BluetoothRegistryKeys {

    public static final RegistryKey BLUETOOTH_RETENTION_TIME_DAYS = RegistryKey.create(
            "bluetooth_retention_time_days",
            Optional.of(new ArrayList<>() {{
                add(ConfigurationEntryConstraint.createNumberRangeConstraint(1, Integer.MAX_VALUE));
            }}),
            Optional.of("7"),
            false
    );

}
