/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.configuration.tracker;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.bandits.trackers.devices.TrackerDevice;
import horse.wtf.nzyme.configuration.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;

public class TrackerConfigurationLoader {

    private final Config root;
    private final Config general;
    private final Config trackerDevice;

    public TrackerConfigurationLoader(File configFile) throws InvalidConfigurationException, IncompleteConfigurationException, FileNotFoundException {
        if (!Files.isReadable(configFile.toPath())) {
            throw new FileNotFoundException("File at [" + configFile.getPath() + "] does not exist or is not readable. Check path and permissions.");
        }

        this.root = ConfigFactory.parseFile(configFile).resolve();

        try {
            this.general = root.getConfig(ConfigurationKeys.GENERAL);
            this.trackerDevice = root.getConfig(ConfigurationKeys.TRACKER_DEVICE);
        } catch(ConfigException e) {
            throw new IncompleteConfigurationException("Incomplete configuration.", e);
        }

        validate();
    }

    public TrackerConfiguration get() {
        return TrackerConfiguration.create(parseRole(), parseNzymeId(), parseTrackerDevice());
    }

    private Role parseRole() {
        return general.getEnum(Role.class, ConfigurationKeys.ROLE);
    }

    private String parseNzymeId() {
        return general.getString(ConfigurationKeys.ID);
    }

    private TrackerDeviceConfiguration parseTrackerDevice() {
        return TrackerDeviceConfiguration.create(
                trackerDevice.getString(ConfigurationKeys.TYPE),
                trackerDevice.getConfig(ConfigurationKeys.PARAMETERS)
        );
    }

    private void validate() throws IncompleteConfigurationException, InvalidConfigurationException {
        ConfigurationValidator.expectEnum(general, ConfigurationKeys.ROLE, ConfigurationKeys.GENERAL, Role.class);
        ConfigurationValidator.expect(general, ConfigurationKeys.ID, ConfigurationKeys.GENERAL, String.class);
        ConfigurationValidator.expect(root, ConfigurationKeys.TRACKER_DEVICE, "<root>", Config.class);

        // Tracker config.
        ConfigurationValidator.expect(trackerDevice, ConfigurationKeys.TYPE, ConfigurationKeys.TRACKER_DEVICE, String.class);
        ConfigurationValidator.expect(trackerDevice, ConfigurationKeys.PARAMETERS, ConfigurationKeys.TRACKER_DEVICE, Config.class);

        ConfigurationValidator.expect(trackerDevice, ConfigurationKeys.TYPE, ConfigurationKeys.TRACKER_DEVICE, String.class);
        ConfigurationValidator.expect(trackerDevice, ConfigurationKeys.PARAMETERS, ConfigurationKeys.TRACKER_DEVICE, Config.class);

        // Validate parameters of SX126X LoRa HAT.
        if (trackerDevice.getString(ConfigurationKeys.TYPE).equals(TrackerDevice.TYPE.SX126X_LORA.toString())) {
            Config loraConfig = trackerDevice.getConfig(ConfigurationKeys.PARAMETERS);

            // Serial port must exist.
            ConfigurationValidator.expect(loraConfig, ConfigurationKeys.SERIAL_PORT, ConfigurationKeys.TRACKER_DEVICE + "." + ConfigurationKeys.PARAMETERS, String.class);
            String serialPortPath = loraConfig.getString(ConfigurationKeys.SERIAL_PORT);
            if (!new File(serialPortPath).exists()) {
                throw new InvalidConfigurationException("Parameter " + ConfigurationKeys.TRACKER_DEVICE + "." + ConfigurationKeys.PARAMETERS + "."
                        + ConfigurationKeys.SERIAL_PORT + " does not point to an existing serial port path at [" + serialPortPath + "].");
            }

            // Encryption key must exist and be exactly 32 characters.
            ConfigurationValidator.expect(loraConfig, ConfigurationKeys.ENCRYPTION_KEY, ConfigurationKeys.TRACKER_DEVICE + "." + ConfigurationKeys.PARAMETERS, String.class);
            String encryptionKey = loraConfig.getString(ConfigurationKeys.ENCRYPTION_KEY);
            if (encryptionKey.length() != 32) {
                throw new InvalidConfigurationException("Parameter " + ConfigurationKeys.TRACKER_DEVICE + "." + ConfigurationKeys.PARAMETERS + "."
                        + ConfigurationKeys.ENCRYPTION_KEY + " must be exactly 32 characters long.");
            }
        }
    }
}
