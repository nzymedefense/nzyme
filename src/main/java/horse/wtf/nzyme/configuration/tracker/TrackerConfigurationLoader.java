/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package horse.wtf.nzyme.configuration.tracker;

import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.bandits.trackers.devices.TrackerDevice;
import horse.wtf.nzyme.bandits.trackers.hid.TrackerHID;
import horse.wtf.nzyme.configuration.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.List;

public class TrackerConfigurationLoader {

    private final Config root;
    private final Config general;
    private final Config uplinkDevice;

    private final BaseDot11ConfigurationLoader baseDot11ConfigurationLoader;

    public TrackerConfigurationLoader(File configFile) throws InvalidConfigurationException, IncompleteConfigurationException, FileNotFoundException {
        if (!Files.isReadable(configFile.toPath())) {
            throw new FileNotFoundException("File at [" + configFile.getPath() + "] does not exist or is not readable. Check path and permissions.");
        }

        this.root = ConfigFactory.parseFile(configFile).resolve();

        this.baseDot11ConfigurationLoader = new BaseDot11ConfigurationLoader(root);

        try {
            this.general = root.getConfig(ConfigurationKeys.GENERAL);
            this.uplinkDevice = root.getConfig(ConfigurationKeys.UPLINK_DEVICE);
        } catch(ConfigException e) {
            throw new IncompleteConfigurationException("Incomplete configuration.", e);
        }

        validate();
    }

    public TrackerConfiguration get() {
        return TrackerConfiguration.create(
                parseRole(),
                parseUplinkDevice(),
                parseHIDs(),
                baseDot11ConfigurationLoader.parseDot11Monitors()
        );
    }

    private Role parseRole() {
        return general.getEnum(Role.class, ConfigurationKeys.ROLE);
    }

    private UplinkDeviceConfiguration parseUplinkDevice() {
        return UplinkDeviceConfiguration.create(
                uplinkDevice.getString(ConfigurationKeys.TYPE),
                uplinkDevice.getConfig(ConfigurationKeys.PARAMETERS)
        );
    }

    private List<TrackerHID.TYPE> parseHIDs() {
        List<TrackerHID.TYPE> hids = Lists.newArrayList();
        for (String hid : general.getStringList(ConfigurationKeys.HIDS)) {
            hids.add(TrackerHID.TYPE.valueOf(hid.toUpperCase()));
        }

        return hids;
    }

    private void validate() throws IncompleteConfigurationException, InvalidConfigurationException {
        ConfigurationValidator.expectEnum(general, ConfigurationKeys.ROLE, ConfigurationKeys.GENERAL, Role.class);
        ConfigurationValidator.expect(general, ConfigurationKeys.DATA_DIRECTORY, ConfigurationKeys.GENERAL, String.class);
        ConfigurationValidator.expect(root, ConfigurationKeys.UPLINK_DEVICE, "<root>", Config.class);
        ConfigurationValidator.expect(general, ConfigurationKeys.HIDS, ConfigurationKeys.GENERAL, List.class);

        // Tracker config.
        ConfigurationValidator.expect(uplinkDevice, ConfigurationKeys.TYPE, ConfigurationKeys.UPLINK_DEVICE, String.class);
        ConfigurationValidator.expect(uplinkDevice, ConfigurationKeys.PARAMETERS, ConfigurationKeys.UPLINK_DEVICE, Config.class);

        ConfigurationValidator.expect(uplinkDevice, ConfigurationKeys.TYPE, ConfigurationKeys.UPLINK_DEVICE, String.class);
        ConfigurationValidator.expect(uplinkDevice, ConfigurationKeys.PARAMETERS, ConfigurationKeys.UPLINK_DEVICE, Config.class);

        // HIDs.
        for (String hid : general.getStringList(ConfigurationKeys.HIDS)) {
            try {
                TrackerHID.TYPE.valueOf(hid.toUpperCase());
            } catch(Exception e) {
                throw new InvalidConfigurationException("Invalid HID [" + hid + "] configured.", e);
            }
        }

        // Validate parameters of SX126X LoRa HAT.
        if (uplinkDevice.getString(ConfigurationKeys.TYPE).equals(TrackerDevice.TYPE.SX126X_LORA.toString())) {
            Config loraConfig = uplinkDevice.getConfig(ConfigurationKeys.PARAMETERS);

            // Serial port must exist.
            ConfigurationValidator.expect(loraConfig, ConfigurationKeys.SERIAL_PORT, ConfigurationKeys.UPLINK_DEVICE + "." + ConfigurationKeys.PARAMETERS, String.class);
            String serialPortPath = loraConfig.getString(ConfigurationKeys.SERIAL_PORT);
            if (!new File(serialPortPath).exists()) {
                throw new InvalidConfigurationException("Parameter " + ConfigurationKeys.UPLINK_DEVICE + "." + ConfigurationKeys.PARAMETERS + "."
                        + ConfigurationKeys.SERIAL_PORT + " does not point to an existing serial port path at [" + serialPortPath + "].");
            }

            // Encryption key must exist and be exactly 32 characters.
            ConfigurationValidator.expect(loraConfig, ConfigurationKeys.ENCRYPTION_KEY, ConfigurationKeys.UPLINK_DEVICE + "." + ConfigurationKeys.PARAMETERS, String.class);
            String encryptionKey = loraConfig.getString(ConfigurationKeys.ENCRYPTION_KEY);
            if (encryptionKey.length() != 32) {
                throw new InvalidConfigurationException("Parameter " + ConfigurationKeys.UPLINK_DEVICE + "." + ConfigurationKeys.PARAMETERS + "."
                        + ConfigurationKeys.ENCRYPTION_KEY + " must be exactly 32 characters long.");
            }
        }

        // Validate shared/base 802.11 config.
        baseDot11ConfigurationLoader.validateMonitors();

        // Require at least one monitor.
        List<Dot11MonitorDefinition> monitors = baseDot11ConfigurationLoader.parseDot11Monitors();
        if (monitors.isEmpty()) {
            throw new IncompleteConfigurationException("No 802.11 monitors configured. The tracker needs at least one monitor to work. " +
                    "Configure in nzyme configuration file at [802_11_monitors].");
        }
    }
}
