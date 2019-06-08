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

package horse.wtf.nzyme.configuration;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Enums;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.notifications.uplinks.graylog.GraylogAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class ConfigurationLoader {

    private static final Logger LOG = LogManager.getLogger(Nzyme.class);

    private final Config root;
    private final Config general;
    private final Config interfaces;
    private final Config python;
    private final Config alerting;

    public ConfigurationLoader(File configFile, boolean skipValidation) throws InvalidConfigurationException, IncompleteConfigurationException, FileNotFoundException {
        if (!Files.isReadable(configFile.toPath())) {
            throw new FileNotFoundException("File at [" + configFile.getPath() + "] does not exist or is not readable. Check path and permissions.");
        }

        this.root = ConfigFactory.parseFile(configFile);

        this.general = root.getConfig(Keys.GENERAL);
        this.python = general.getConfig(Keys.PYTHON);
        this.alerting = general.getConfig(Keys.ALERTING);
        this.interfaces = root.getConfig(Keys.INTERFACES);

        if (!skipValidation) {
            validate();
        }
    }

    public Configuration get() {
        return Configuration.create(
                parseVersionchecksEnabled(),
                parseFetchOUIsEnabled(),
                parseRole(),
                parseNzymeId(),
                parseDatabasePath(),
                parsePythonExecutable(),
                parsePythonScriptDirectory(),
                parsePythonScriptPrefix(),
                parseRestListenUri(),
                parseDot11Monitors(),
                parseDot11Networks(),
                parseDot11Alerts(),
                parseAlertingRetentionPeriodMinutes(),
                parseAlertingTrainingPeriodSeconds(),
                parseKnownBanditFingerprints(),
                parseGraylogUplinks()
        );
    }

    private Role parseRole() {
        return general.getEnum(Role.class, Keys.ROLE);
    }

    private String parseNzymeId() {
        return general.getString(Keys.ID);
    }

    private String parseDatabasePath() {
        return general.getString(Keys.DATABASE_PATH);
    }

    private String parsePythonExecutable() {
        return python.getString(Keys.PYTHON_PATH);
    }

    private String parsePythonScriptDirectory() {
        return python.getString(Keys.PYTHON_SCRIPT_DIR);
    }

    private String parsePythonScriptPrefix() {
        return python.getString(Keys.PYTHON_SCRIPT_PREFIX);
    }

    private boolean parseVersionchecksEnabled() {
        return general.getBoolean(Keys.VERSIONCHECKS);
    }

    private boolean parseFetchOUIsEnabled() {
        return general.getBoolean(Keys.FETCH_OUIS);
    }

    private URI parseRestListenUri() {
        return URI.create(interfaces.getString(Keys.REST_LISTEN_URI));
    }

    private Integer parseAlertingRetentionPeriodMinutes() {
        return alerting.getInt(Keys.CLEAN_AFTER_MINUTES);
    }

    private Integer parseAlertingTrainingPeriodSeconds() {
        return alerting.getInt(Keys.TRAINING_PERIOD_SECONDS);
    }

    private List<Dot11MonitorDefinition> parseDot11Monitors() {
        ImmutableList.Builder<Dot11MonitorDefinition> result = new ImmutableList.Builder<>();

        for (Config config : root.getConfigList(Keys.DOT11_MONITORS)) {
            if (!Dot11MonitorDefinition.checkConfig(config)) {
                LOG.info("Skipping 802.11 monitor with invalid configuration. Invalid monitor: [{}]", config);
                continue;
            }

            result.add(Dot11MonitorDefinition.create(
                    config.getString(Keys.DEVICE),
                    config.getIntList(Keys.CHANNELS),
                    config.getString(Keys.HOP_COMMAND),
                    config.getInt(Keys.HOP_INTERVAL)
            ));
        }

        return result.build();
    }

    private List<Dot11NetworkDefinition> parseDot11Networks() {
        ImmutableList.Builder<Dot11NetworkDefinition> result = new ImmutableList.Builder<>();

        for (Config config : root.getConfigList(Keys.DOT11_NETWORKS)) {
            if (!Dot11NetworkDefinition.checkConfig(config)) {
                LOG.info("Skipping 802.11 network with invalid configuration. Invalid network: [{}]", config);
                continue;
            }

            ImmutableList.Builder<String> lowercaseBSSIDs = new ImmutableList.Builder<>();
            for (String bssid : config.getStringList(Keys.BSSIDS)) {
                lowercaseBSSIDs.add(bssid.toLowerCase());
            }

            result.add(Dot11NetworkDefinition.create(
                    config.getString(Keys.SSID),
                    lowercaseBSSIDs.build(),
                    config.getIntList(Keys.CHANNELS),
                    config.getStringList(Keys.SECURITY)
            ));
        }

        return result.build();
    }

    private List<Alert.TYPE_WIDE> parseDot11Alerts() {
        ImmutableList.Builder<Alert.TYPE_WIDE> result = new ImmutableList.Builder<>();

        for (String alert : root.getStringList(Keys.DOT11_ALERTS)) {
            String name = alert.toUpperCase();

            if (Enums.getIfPresent(Alert.TYPE_WIDE.class, name).isPresent()) {
                result.add(Alert.TYPE_WIDE.valueOf(name));
            }
        }

        return result.build();
    }

    @Nullable
    private List<GraylogAddress> parseGraylogUplinks() {
        try {
            List<String> graylogAddresses = root.getStringList(Keys.GRAYLOG_UPLINKS);
            if (graylogAddresses == null) {
                return null;
            }

            List<GraylogAddress> result = Lists.newArrayList();
            for (String address : graylogAddresses) {
                String[] parts = address.split(":");
                result.add(GraylogAddress.create(parts[0], Integer.parseInt(parts[1])));
            }

            return result;
        } catch (ConfigException e) {
            LOG.debug(e);
            return null;
        }
    }

    private Map<String, BanditFingerprintDefinition> parseKnownBanditFingerprints() {
        ImmutableMap.Builder<String, BanditFingerprintDefinition> fingerprints = new ImmutableMap.Builder<>();

        for (Config def : root.getConfigList(Keys.KNOWN_BANDIT_FINGERPRINTS)) {
            String fingerprint = def.getString(Keys.BANDIT_FINGERPRINT);
            fingerprints.put(
                    fingerprint,
                    BanditFingerprintDefinition.create(fingerprint, def.getString(Keys.BANDIT_NAME)))
            ;
        }

        return fingerprints.build();
    }

    private void validate() throws IncompleteConfigurationException, InvalidConfigurationException {
        // Completeness and type validity.
        expectEnum(general, Keys.ROLE, Keys.GENERAL, Role.class);
        expect(general, Keys.ID, Keys.GENERAL, String.class);
        expect(general, Keys.DATABASE_PATH, Keys.GENERAL, String.class);
        expect(general, Keys.VERSIONCHECKS, Keys.GENERAL, Boolean.class);
        expect(general, Keys.FETCH_OUIS, Keys.GENERAL, Boolean.class);
        expect(python, Keys.PYTHON_PATH, Keys.GENERAL + "." + Keys.PYTHON, String.class);
        expect(python, Keys.PYTHON_SCRIPT_DIR, Keys.GENERAL + "." + Keys.PYTHON, String.class);
        expect(python, Keys.PYTHON_SCRIPT_PREFIX, Keys.GENERAL + "." + Keys.PYTHON, String.class);
        expect(alerting, Keys.CLEAN_AFTER_MINUTES, Keys.GENERAL + "." + Keys.ALERTING, Integer.class);
        expect(alerting, Keys.TRAINING_PERIOD_SECONDS, Keys.GENERAL + "." + Keys.ALERTING, Integer.class);
        expect(interfaces, Keys.REST_LISTEN_URI, Keys.INTERFACES, String.class);
        expect(root, Keys.DOT11_MONITORS, "<root>", List.class);
        expect(root, Keys.DOT11_NETWORKS, "<root>", List.class);
        expect(root, Keys.DOT11_ALERTS, "<root>", List.class);
        expect(root, Keys.KNOWN_BANDIT_FINGERPRINTS, "<root>", List.class);

        // 802.11 Monitors.
        int i = 0;
        for (Config c : root.getConfigList(Keys.DOT11_MONITORS)) {
            String where = Keys.DOT11_MONITORS + "." + "#" + i;
            expect(c, Keys.DEVICE, where, String.class);
            expect(c, Keys.CHANNELS, where, List.class);
            expect(c, Keys.HOP_COMMAND, where, String.class);
            expect(c, Keys.HOP_INTERVAL, where, Integer.class);
            i++;
        }

        // 802.11 Trap Pairs
        i = 0;
        for (Config c : root.getConfigList(Keys.DOT11_TRAP_PAIRS)) {
            String where = Keys.DOT11_TRAP_PAIRS + "." + "#" + i;
            expect(c, Keys.DEVICE_SENDER, where, String.class);
            expect(c, Keys.DEVICE_MONITOR, where, String.class);
            expect(c, Keys.CHANNELS, where, List.class);
            expect(c, Keys.HOP_COMMAND, where, String.class);
            expect(c, Keys.HOP_INTERVAL, where, Integer.class);
            expect(c, Keys.TRAPS, where, List.class);
        }

        // Logical validity.
        // Python: executable is an executable file.
        if(!Files.isExecutable(new File(parsePythonExecutable()).toPath())) {
            throw new InvalidConfigurationException("Parameter [general.python." + Keys.PYTHON_PATH + "] does not point to an executable file: " + parsePythonExecutable());
        }

        // Python: script directory is a directory and writable.
        if (!Files.isDirectory(new File(parsePythonScriptDirectory()).toPath()) || !Files.isWritable(new File(parsePythonScriptDirectory()).toPath())) {
            throw new InvalidConfigurationException("Parameter [general.python." + Keys.PYTHON_SCRIPT_DIR + "] does not point to a writable directory: " + parsePythonScriptDirectory());
        }

        // REST listen URI can be parsed into a URI.
        try {
            parseRestListenUri();
        } catch(Exception e) {
            LOG.error(e);
            throw new InvalidConfigurationException("Parameter [interfaces." + Keys.REST_LISTEN_URI + "] cannot be parsed into a URI. Make sure it is correct.");
        }

        // All channels are all integers, larger than 0.
        validateChannelList(Keys.DOT11_MONITORS);

        // 802_11 monitors should be parsed and safe to use for further logical checks from here on.

        // 802_11 monitors: No channel is used in any other monitor.
        List<Integer> usedChannels = Lists.newArrayList();
        for (Dot11MonitorDefinition monitor : parseDot11Monitors()) {
            for (Integer channel : monitor.channels()) {
                if (usedChannels.contains(channel)) {
                    throw new InvalidConfigurationException("Channel [" + channel + "] is defined for multiple 802.11 monitors. You should not have multiple monitors tuned to the same channel.");
                }
            }
            usedChannels.addAll(monitor.channels());
        }

        // 802_11 monitors: Device is not used in any other configuration.
        List<String> devices = Lists.newArrayList();
        for (Dot11MonitorDefinition monitor : parseDot11Monitors()) {
            if (devices.contains(monitor.device())) {
                throw new InvalidConfigurationException("Device [" + monitor.device() + "] is defined for multiple 802.11 monitors. You should not have multiple monitors using the same device.");
            }
            devices.add(monitor.device());
        }

        // 802_11 networks: SSID is unique.
        List<String> ssids = Lists.newArrayList();
        for (Dot11NetworkDefinition net : parseDot11Networks()) {
            if (ssids.contains(net.ssid())) {
                throw new InvalidConfigurationException("SSID [" + net.ssid() + "] is defined multiple times. You cannot define a network with the same SSID more than once.");
            }
            ssids.add(net.ssid());
        }

        // 802.11 networks: BSSIDs are unique for this network. (note that a BSSID can be used in multiple networks)
        for (Dot11NetworkDefinition net : parseDot11Networks()) {
            List<String> bssids = Lists.newArrayList();
            for (String bssid : net.bssids()) {
                if(bssids.contains(bssid)) {
                    throw new InvalidConfigurationException("Network [" + net.ssid() + "] has at least one BSSID defined twice. You cannot define a BSSID for the same network more than once.");
                }
                bssids.add(bssid);
            }
        }

        // Known bandit fingerprints: Each fingerprint is unique.
        List<String> usedFingerprints = Lists.newArrayList();
        for (Config def : root.getConfigList(Keys.KNOWN_BANDIT_FINGERPRINTS)) {
            String fingerprint = def.getString(Keys.BANDIT_FINGERPRINT);
            if (usedFingerprints.contains(fingerprint)) {
                throw new InvalidConfigurationException("Duplicate Known Bandit Fingerprint [" + fingerprint + "].");
            }

            usedFingerprints.add(fingerprint);
        }

        // TODO: No trap pair device is used multiple times or as a monitor.
        // TODO: No trap pair device has a trap configured multiple times.
    }

    private void validateChannelList(String key) throws InvalidConfigurationException {
        int x = 0;
        List<Integer> usedChannels = Lists.newArrayList();
        for (Config c : root.getConfigList(key)) {
            String where = key + "." + "#" + x;
            try {
                for (Integer channel : c.getIntList(Keys.CHANNELS)) {
                    if (channel < 1) {
                        throw new InvalidConfigurationException("Invalid channels in list for [" + where + "}. All channels must be integers larger than 0.");
                    }

                    if (usedChannels.contains(channel)) {
                        throw new InvalidConfigurationException("Duplicate channel <" + channel + "> in list for [ " + where + " ]. Channels cannot be duplicate per monitor or across multiple monitors.");
                    }

                    usedChannels.add(channel);
                }
            } catch(ConfigException e) {
                LOG.error(e);
                throw new InvalidConfigurationException("Invalid channels list for [" + where + "}. All channels must be integers larger than 0.");
            } finally {
                x++;
            }
        }
    }

    private void expect(Config c, String key, String where, Class clazz) throws IncompleteConfigurationException, InvalidConfigurationException {
        boolean incomplete = false;
        boolean invalid = false;

        try {
            // String.
            if (clazz.equals(String.class)) {
                incomplete = Strings.isNullOrEmpty(c.getString(key));
            }

            // Boolean.
            if (clazz.equals(Boolean.class)) {
                c.getBoolean(key);
            }

            // List
            if (clazz.equals(List.class)) {
                c.getList(key);
            }

            if (clazz.equals(Integer.class)) {
                c.getInt(key);
            }
        } catch(ConfigException.Missing e) {
            LOG.error(e);
            incomplete = true;
        } catch(ConfigException.WrongType e) {
            LOG.error(e);
            invalid = true;
        } catch (ConfigException e) {
            LOG.error(e);
            throw new InvalidConfigurationException("Parsing error for parameter [" + key + "] in section [" + where + "].");
        }

        if (incomplete) {
            throw new IncompleteConfigurationException("Missing parameter [" + key + "] in section [" + where + "].");
        }

        if (invalid) {
            throw new InvalidConfigurationException("Invalid value for parameter [" + key + "] in section [" + where + "].");
        }
    }

    private void expectEnum(Config c, String key, String where, Class enumClazz) throws InvalidConfigurationException, IncompleteConfigurationException {
        try {
            c.getEnum(enumClazz, key);
        } catch(ConfigException.Missing e) {
            throw new IncompleteConfigurationException("Missing parameter [" + key + "] in section [" + where + "].");
        } catch(ConfigException.BadValue e) {
            LOG.error(e);
            throw new InvalidConfigurationException("Invalid value for parameter [" + key + "] in section [" + where + "].");
        }
    }

    public class InvalidConfigurationException extends Exception {

        public InvalidConfigurationException(String msg) {
            super(msg);
        }

    }

    public class IncompleteConfigurationException extends Exception {

        public IncompleteConfigurationException(String msg) {
            super(msg);
        }

    }

}
