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
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ConfigurationLoader {

    private static final Logger LOG = LogManager.getLogger(Nzyme.class);

    private final Config root;
    private final Config general;
    private final Config interfaces;
    private final Config python;
    private final Config alerting;
    private final Config tuningParams;

    public ConfigurationLoader(File configFile, boolean skipValidation) throws InvalidConfigurationException, IncompleteConfigurationException, FileNotFoundException {
        if (!Files.isReadable(configFile.toPath())) {
            throw new FileNotFoundException("File at [" + configFile.getPath() + "] does not exist or is not readable. Check path and permissions.");
        }

        this.root = ConfigFactory.parseFile(configFile);

        try {
            this.general = root.getConfig(ConfigurationKeys.GENERAL);
            this.python = general.getConfig(ConfigurationKeys.PYTHON);
            this.alerting = general.getConfig(ConfigurationKeys.ALERTING);
            this.interfaces = root.getConfig(ConfigurationKeys.INTERFACES);
            this.tuningParams = root.getConfig(ConfigurationKeys.TUNING_PARAMETERS);
        } catch(ConfigException e) {
            throw new IncompleteConfigurationException("Incomplete configuration.", e);
        }

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
                parseUseTls(),
                parseTlsCertificatePath(),
                parseTlsKeyPath(),
                parseRestListenUri(),
                parseHttpExternalUri(),
                parseDot11Monitors(),
                parseDot11Networks(),
                parseDot11Alerts(),
                parseAlertingRetentionPeriodMinutes(),
                parseAlertingTrainingPeriodSeconds(),
                parseKnownBanditFingerprints(),
                parseSignalQualityTableSizeMinutes(),
                parseExpectedSignalDeltaModifier(),
                parseAnomalyAlertLookbackMinutes(),
                parseAnomalyAlertTriggerRatio(),
                parseGraylogUplinks()
        );
    }

    private Role parseRole() {
        return general.getEnum(Role.class, ConfigurationKeys.ROLE);
    }

    private String parseNzymeId() {
        return general.getString(ConfigurationKeys.ID);
    }

    private String parseDatabasePath() {
        return general.getString(ConfigurationKeys.DATABASE_PATH);
    }

    private String parsePythonExecutable() {
        return python.getString(ConfigurationKeys.PYTHON_PATH);
    }

    private String parsePythonScriptDirectory() {
        return python.getString(ConfigurationKeys.PYTHON_SCRIPT_DIR);
    }

    private String parsePythonScriptPrefix() {
        return python.getString(ConfigurationKeys.PYTHON_SCRIPT_PREFIX);
    }

    private boolean parseVersionchecksEnabled() {
        return general.getBoolean(ConfigurationKeys.VERSIONCHECKS);
    }

    private boolean parseFetchOUIsEnabled() {
        return general.getBoolean(ConfigurationKeys.FETCH_OUIS);
    }

    private boolean parseUseTls() {
        return interfaces.getBoolean(ConfigurationKeys.USE_TLS);
    }

    private Path parseTlsCertificatePath() {
        return new File(interfaces.getString(ConfigurationKeys.TLS_CERTIFICATE_PATH)).toPath();
    }

    private Path parseTlsKeyPath() {
        return new File(interfaces.getString(ConfigurationKeys.TLS_KEY_PATH)).toPath();
    }

    private URI parseRestListenUri() {
        return URI.create(interfaces.getString(ConfigurationKeys.REST_LISTEN_URI));
    }

    private URI parseHttpExternalUri() {
        return URI.create(interfaces.getString(ConfigurationKeys.HTTP_EXTERNAL_URI));
    }

    private Integer parseAlertingRetentionPeriodMinutes() {
        return alerting.getInt(ConfigurationKeys.CLEAN_AFTER_MINUTES);
    }

    private Integer parseAlertingTrainingPeriodSeconds() {
        return alerting.getInt(ConfigurationKeys.TRAINING_PERIOD_SECONDS);
    }

    private List<Dot11MonitorDefinition> parseDot11Monitors() {
        ImmutableList.Builder<Dot11MonitorDefinition> result = new ImmutableList.Builder<>();

        for (Config config : root.getConfigList(ConfigurationKeys.DOT11_MONITORS)) {
            if (!Dot11MonitorDefinition.checkConfig(config)) {
                LOG.info("Skipping 802.11 monitor with invalid configuration. Invalid monitor: [{}]", config);
                continue;
            }

            result.add(Dot11MonitorDefinition.create(
                    config.getString(ConfigurationKeys.DEVICE),
                    config.getIntList(ConfigurationKeys.CHANNELS),
                    config.getString(ConfigurationKeys.HOP_COMMAND),
                    config.getInt(ConfigurationKeys.HOP_INTERVAL)
            ));
        }

        return result.build();
    }

    private List<Dot11NetworkDefinition> parseDot11Networks() {
        ImmutableList.Builder<Dot11NetworkDefinition> result = new ImmutableList.Builder<>();

        for (Config config : root.getConfigList(ConfigurationKeys.DOT11_NETWORKS)) {
            if (!Dot11NetworkDefinition.checkConfig(config)) {
                LOG.info("Skipping 802.11 network with invalid configuration. Invalid network: [{}]", config);
                continue;
            }

            ImmutableList.Builder<Dot11BSSIDDefinition> lowercaseBSSIDs = new ImmutableList.Builder<>();
            for (Config bssid : config.getConfigList(ConfigurationKeys.BSSIDS)) {
                lowercaseBSSIDs.add(Dot11BSSIDDefinition.create(
                        bssid.getString(ConfigurationKeys.ADDRESS).toLowerCase(),
                        bssid.getStringList(ConfigurationKeys.FINGERPRINTS)
                ));
            }

            result.add(Dot11NetworkDefinition.create(
                    config.getString(ConfigurationKeys.SSID),
                    lowercaseBSSIDs.build(),
                    config.getIntList(ConfigurationKeys.CHANNELS),
                    config.getStringList(ConfigurationKeys.SECURITY)
            ));
        }

        return result.build();
    }

    private List<Alert.TYPE_WIDE> parseDot11Alerts() {
        ImmutableList.Builder<Alert.TYPE_WIDE> result = new ImmutableList.Builder<>();

        for (String alert : root.getStringList(ConfigurationKeys.DOT11_ALERTS)) {
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
            List<String> graylogAddresses = root.getStringList(ConfigurationKeys.GRAYLOG_UPLINKS);
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

        for (Config def : root.getConfigList(ConfigurationKeys.KNOWN_BANDIT_FINGERPRINTS)) {
            String fingerprint = def.getString(ConfigurationKeys.FINGERPRINT);
            fingerprints.put(
                    fingerprint,
                    BanditFingerprintDefinition.create(fingerprint, def.getStringList(ConfigurationKeys.BANDIT_NAMES)))
            ;
        }

        return fingerprints.build();
    }

    private double parseAnomalyAlertTriggerRatio() {
        return tuningParams.getDouble(ConfigurationKeys.ANOMALY_ALERT_TRIGGER_RATIO);
    }

    private int parseAnomalyAlertLookbackMinutes() {
        return tuningParams.getInt(ConfigurationKeys.ANOMALY_ALERT_LOOKBACK_MINUTES);
    }

    private double parseExpectedSignalDeltaModifier() {
        return tuningParams.getDouble(ConfigurationKeys.EXPECTED_SIGNAL_DELTA_MODIFIER);
    }

    private int parseSignalQualityTableSizeMinutes() {
        return tuningParams.getInt(ConfigurationKeys.SIGNAL_QUALITY_TABLE_SIZE_MINUTES);
    }

    private void validate() throws IncompleteConfigurationException, InvalidConfigurationException {
        // Completeness and type validity.
        expectEnum(general, ConfigurationKeys.ROLE, ConfigurationKeys.GENERAL, Role.class);
        expect(general, ConfigurationKeys.ID, ConfigurationKeys.GENERAL, String.class);
        expect(general, ConfigurationKeys.DATABASE_PATH, ConfigurationKeys.GENERAL, String.class);
        expect(general, ConfigurationKeys.VERSIONCHECKS, ConfigurationKeys.GENERAL, Boolean.class);
        expect(general, ConfigurationKeys.FETCH_OUIS, ConfigurationKeys.GENERAL, Boolean.class);
        expect(python, ConfigurationKeys.PYTHON_PATH, ConfigurationKeys.GENERAL + "." + ConfigurationKeys.PYTHON, String.class);
        expect(python, ConfigurationKeys.PYTHON_SCRIPT_DIR, ConfigurationKeys.GENERAL + "." + ConfigurationKeys.PYTHON, String.class);
        expect(python, ConfigurationKeys.PYTHON_SCRIPT_PREFIX, ConfigurationKeys.GENERAL + "." + ConfigurationKeys.PYTHON, String.class);
        expect(alerting, ConfigurationKeys.CLEAN_AFTER_MINUTES, ConfigurationKeys.GENERAL + "." + ConfigurationKeys.ALERTING, Integer.class);
        expect(alerting, ConfigurationKeys.TRAINING_PERIOD_SECONDS, ConfigurationKeys.GENERAL + "." + ConfigurationKeys.ALERTING, Integer.class);
        expect(interfaces, ConfigurationKeys.REST_LISTEN_URI, ConfigurationKeys.INTERFACES, String.class);
        expect(interfaces, ConfigurationKeys.HTTP_EXTERNAL_URI, ConfigurationKeys.INTERFACES, String.class);
        expect(root, ConfigurationKeys.DOT11_MONITORS, "<root>", List.class);
        expect(root, ConfigurationKeys.DOT11_NETWORKS, "<root>", List.class);
        expect(root, ConfigurationKeys.DOT11_ALERTS, "<root>", List.class);
        expect(root, ConfigurationKeys.KNOWN_BANDIT_FINGERPRINTS, "<root>", List.class);
        expect(root, ConfigurationKeys.TUNING_PARAMETERS, "<root>", Map.class);

        // 802.11 Monitors.
        int i = 0;
        for (Config c : root.getConfigList(ConfigurationKeys.DOT11_MONITORS)) {
            String where = ConfigurationKeys.DOT11_MONITORS + "." + "#" + i;
            expect(c, ConfigurationKeys.DEVICE, where, String.class);
            expect(c, ConfigurationKeys.CHANNELS, where, List.class);
            expect(c, ConfigurationKeys.HOP_COMMAND, where, String.class);
            expect(c, ConfigurationKeys.HOP_INTERVAL, where, Integer.class);
            i++;
        }

        // 802.11 Trap Pairs
        i = 0;
        for (Config c : root.getConfigList(ConfigurationKeys.DOT11_TRAP_PAIRS)) {
            String where = ConfigurationKeys.DOT11_TRAP_PAIRS + "." + "#" + i;
            expect(c, ConfigurationKeys.DEVICE_SENDER, where, String.class);
            expect(c, ConfigurationKeys.DEVICE_MONITOR, where, String.class);
            expect(c, ConfigurationKeys.CHANNELS, where, List.class);
            expect(c, ConfigurationKeys.HOP_COMMAND, where, String.class);
            expect(c, ConfigurationKeys.HOP_INTERVAL, where, Integer.class);
            expect(c, ConfigurationKeys.TRAPS, where, List.class);
        }

        // Tuning parameters
        expect(tuningParams, ConfigurationKeys.SIGNAL_QUALITY_TABLE_SIZE_MINUTES, ConfigurationKeys.TUNING_PARAMETERS, Integer.class);
        expect(tuningParams, ConfigurationKeys.EXPECTED_SIGNAL_DELTA_MODIFIER, ConfigurationKeys.TUNING_PARAMETERS, Float.class);
        expect(tuningParams, ConfigurationKeys.ANOMALY_ALERT_LOOKBACK_MINUTES, ConfigurationKeys.TUNING_PARAMETERS, Integer.class);
        expect(tuningParams, ConfigurationKeys.ANOMALY_ALERT_TRIGGER_RATIO, ConfigurationKeys.TUNING_PARAMETERS, Float.class);

        // Logical validity.
        // Python: executable is an executable file.
        if(!Files.isExecutable(new File(parsePythonExecutable()).toPath())) {
            throw new InvalidConfigurationException("Parameter [general.python." + ConfigurationKeys.PYTHON_PATH + "] does not point to an executable file: " + parsePythonExecutable());
        }

        // Python: script directory is a directory and writable.
        if (!Files.isDirectory(new File(parsePythonScriptDirectory()).toPath()) || !Files.isWritable(new File(parsePythonScriptDirectory()).toPath())) {
            throw new InvalidConfigurationException("Parameter [general.python." + ConfigurationKeys.PYTHON_SCRIPT_DIR + "] does not point to a writable directory: " + parsePythonScriptDirectory());
        }

        // REST listen URI can be parsed into a URI.
        try {
            parseRestListenUri();
        } catch(Exception e) {
            LOG.error(e);
            throw new InvalidConfigurationException("Parameter [interfaces." + ConfigurationKeys.REST_LISTEN_URI + "] cannot be parsed into a URI. Make sure it is correct.");
        }

        // HTTP external URI can be parsed into a URI.
        try {
            parseHttpExternalUri();
        } catch(Exception e) {
            LOG.error(e);
            throw new InvalidConfigurationException("Parameter [interfaces." + ConfigurationKeys.HTTP_EXTERNAL_URI + "] cannot be parsed into a URI. Make sure it is correct.");
        }

        // TLS, if TLS is enabled.
        if (parseUseTls()) {
            // URI schemes must be HTTPS if TLS is enabled.
            if (!parseRestListenUri().getScheme().equals("https")) {
                throw new InvalidConfigurationException("TLS is enabled but [interfaces." + ConfigurationKeys.REST_LISTEN_URI + "] is not configured to use HTTPS.");
            }

            if (!parseHttpExternalUri().getScheme().equals("https")) {
                throw new InvalidConfigurationException("TLS is enabled but [interfaces." + ConfigurationKeys.HTTP_EXTERNAL_URI + "] is not configured to use HTTPS.");
            }

            try {
                Path cert = parseTlsCertificatePath();
                if (!cert.toFile().canRead()) {
                    throw new InvalidConfigurationException("Parameter [interfaces." + ConfigurationKeys.TLS_CERTIFICATE_PATH + "] points to a file that is not readable.");
                }
            } catch(Exception e) {
                LOG.error(e);
                throw new InvalidConfigurationException("Parameter [interfaces." + ConfigurationKeys.TLS_CERTIFICATE_PATH + "] cannot be parsed into a path. Make sure it is correct.");
            }

            try {
                Path key = parseTlsKeyPath();
                if (!key.toFile().canRead()) {
                    throw new InvalidConfigurationException("Parameter [interfaces." + ConfigurationKeys.TLS_KEY_PATH + "] points to a file that is not readable.");
                }
            } catch(Exception e) {
                LOG.error(e);
                throw new InvalidConfigurationException("Parameter [interfaces." + ConfigurationKeys.TLS_KEY_PATH + "] cannot be parsed into a path. Make sure it is correct.");
            }
        } else {
            // URI schemes must be HTTP if TLS is DISABLED..
            if (!parseRestListenUri().getScheme().equals("http")) {
                throw new InvalidConfigurationException("TLS is disabled but [interfaces." + ConfigurationKeys.REST_LISTEN_URI + "] is not configured to use HTTP. Do not use HTTPS.");
            }

            if (!parseHttpExternalUri().getScheme().equals("http")) {
                throw new InvalidConfigurationException("TLS is disabled but [interfaces." + ConfigurationKeys.HTTP_EXTERNAL_URI + "] is not configured to use HTTP. Do not use HTTPS.");
            }
        }

        // All channels are all integers, larger than 0.
        validateChannelList(ConfigurationKeys.DOT11_MONITORS);

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
            for (Dot11BSSIDDefinition bssid : net.bssids()) {
                if(bssids.contains(bssid.address())) {
                    throw new InvalidConfigurationException("Network [" + net.ssid() + "] has at least one BSSID defined twice. You cannot define a BSSID for the same network more than once.");
                }
                bssids.add(bssid.address());
            }
        }

        // Known bandit fingerprints: Each fingerprint is unique.
        List<String> usedFingerprints = Lists.newArrayList();
        for (Config def : root.getConfigList(ConfigurationKeys.KNOWN_BANDIT_FINGERPRINTS)) {
            String fingerprint = def.getString(ConfigurationKeys.FINGERPRINT);
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
                for (Integer channel : c.getIntList(ConfigurationKeys.CHANNELS)) {
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

        public InvalidConfigurationException(String msg, Throwable e) {
            super(msg, e);
        }

    }

    public class IncompleteConfigurationException extends Exception {

        public IncompleteConfigurationException(String msg) {
            super(msg);
        }

        public IncompleteConfigurationException(String msg, Throwable t) {
            super(msg, t);
        }

    }

}
