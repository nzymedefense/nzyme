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
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.notifications.uplinks.graylog.GraylogAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;

public class Configuration {

    private static final Logger LOG = LogManager.getLogger(Nzyme.class);

    private final Config root;
    private final Config general;
    private final Config interfaces;
    private final Config python;

    // Manual properties.
    private boolean printPacketInfo = false;

    public Configuration(File configFile) throws InvalidConfigurationException, IncompleteConfigurationException {
        this.root = ConfigFactory.parseFile(configFile);

        this.general = root.getConfig(Keys.GENERAL);
        this.python = general.getConfig(Keys.PYTHON);
        this.interfaces = root.getConfig(Keys.INTERFACES);

        validate();
    }

    public Role getRole() {
        return general.getEnum(Role.class, Keys.ROLE);
    }

    public String getNzymeId() {
        return general.getString(Keys.ID);
    }

    public String getPythonExecutable() {
        return python.getString(Keys.PYTHON_PATH);
    }

    public String getPythonScriptDirectory() {
        return python.getString(Keys.PYTHON_SCRIPT_DIR);
    }

    public String getPythonScriptPrefix() {
        return python.getString(Keys.PYTHON_SCRIPT_PREFIX);
    }

    public boolean areVersionchecksEnabled() {
        return general.getBoolean(Keys.VERSIONCHECKS);
    }

    public URI getRestListenUri() {
        return URI.create(interfaces.getString(Keys.REST_LISTEN_URI));
    }

    public List<Dot11MonitorDefinition> getDot11Monitors() {
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

    public List<Dot11NetworkDefinition> getDot11Networks() {
        ImmutableList.Builder<Dot11NetworkDefinition> result = new ImmutableList.Builder<>();

        for (Config config : root.getConfigList(Keys.DOT11_NETWORKS)) {
            if (!Dot11NetworkDefinition.checkConfig(config)) {
                LOG.info("Skipping 802.11 network with invalid configuration. Invalid network: [{}]", config);
                continue;
            }

            result.add(Dot11NetworkDefinition.create(
                    config.getString(Keys.SSID),
                    config.getStringList(Keys.BSSIDS),
                    config.getIntList(Keys.CHANNELS)
            ));
        }

        return result.build();
    }

    @Nullable
    public List<GraylogAddress> getGraylogUplinks() {
        try {
            List<String> graylogAddresses = root.getStringList(Keys.GRAYLOG_UPLINKS);
            if (graylogAddresses == null) {
                return null;
            }

            List<GraylogAddress> result = Lists.newArrayList();
            for (String address : graylogAddresses) {
                String[] parts = address.split(":");
                result.add(new GraylogAddress(parts[0], Integer.parseInt(parts[1])));
            }

            return result;
        } catch (ConfigException e) {
            LOG.debug(e);
            return null;
        }
    }

    public boolean isPrintPacketInfo() {
        return printPacketInfo;
    }

    public void setPrintPacketInfo(boolean printPacketInfo) {
        this.printPacketInfo = printPacketInfo;
    }

    private void validate() throws IncompleteConfigurationException, InvalidConfigurationException {
        // Completeness and type validity.
        expectEnum(general, Keys.ROLE, Keys.GENERAL, Role.class);
        expect(general, Keys.ID, Keys.GENERAL, String.class);
        expect(general, Keys.VERSIONCHECKS, Keys.GENERAL, Boolean.class);
        expect(python, Keys.PYTHON_PATH, Keys.GENERAL + "." + Keys.PYTHON, String.class);
        expect(python, Keys.PYTHON_SCRIPT_DIR, Keys.GENERAL + "." + Keys.PYTHON, String.class);
        expect(python, Keys.PYTHON_SCRIPT_PREFIX, Keys.GENERAL + "." + Keys.PYTHON, String.class);
        expect(interfaces, Keys.REST_LISTEN_URI, Keys.INTERFACES, String.class);
        expect(root, Keys.DOT11_MONITORS, "<root>", List.class);
        expect(root, Keys.DOT11_NETWORKS, "<root>", List.class);

        int i = 0;
        for (Config c : root.getConfigList(Keys.DOT11_MONITORS)) {
            String where = Keys.DOT11_MONITORS + "." + "#" + i;
            expect(c, Keys.DEVICE, where, String.class);
            expect(c, Keys.CHANNELS, where, List.class);
            expect(c, Keys.HOP_COMMAND, where, String.class);
            expect(c, Keys.HOP_INTERVAL, where, Integer.class);
            i++;
        }

        // Logical validity.
        // Python: executable is an executable file.
        if(!Files.isExecutable(new File(getPythonExecutable()).toPath())) {
            throw new InvalidConfigurationException("Parameter [general.python." + Keys.PYTHON_PATH + "] does not point to an executable file: " + getPythonExecutable());
        }

        // Python: script directory is a directory and writable.
        if (!Files.isDirectory(new File(getPythonScriptDirectory()).toPath()) || !Files.isWritable(new File(getPythonScriptDirectory()).toPath())) {
            throw new InvalidConfigurationException("Parameter [general.python." + Keys.PYTHON_SCRIPT_DIR + "] does not point to a writable directory: " + getPythonScriptDirectory());
        }

        // REST listen URI can be parsed into a URI.
        try {
            getRestListenUri();
        } catch(Exception e) {
            LOG.error(e);
            throw new InvalidConfigurationException("Parameter [interfaces." + Keys.REST_LISTEN_URI + "] cannot be parsed into a URI. Make sure it is correct.");
        }

        // All channels are all integers, larger than 0.
        validateChannelList(Keys.DOT11_MONITORS);
        validateChannelList(Keys.DOT11_NETWORKS);

        // 802_11 monitors should be parsed and safe to use for further logical checks from here on.

        // 802_11 monitors: No channel is used in any other monitor.
        List<Integer> usedChannels = Lists.newArrayList();
        for (Dot11MonitorDefinition monitor : getDot11Monitors()) {
            for (Integer channel : monitor.channels()) {
                if (usedChannels.contains(channel)) {
                    throw new InvalidConfigurationException("Channel [" + channel + "] is defined for multiple 802.11 monitors. You should not have multiple monitors tuned to the same channel.");
                }
            }
            usedChannels.addAll(monitor.channels());
        }

        // 802_11 monitors: Device is not used in any other configuration.
        List<String> devices = Lists.newArrayList();
        for (Dot11MonitorDefinition monitor : getDot11Monitors()) {
            if (devices.contains(monitor.device())) {
                throw new InvalidConfigurationException("Device [" + monitor.device() + "] is defined for multiple 802.11 monitors. You should not have multiple monitors using the same device.");
            }
            devices.add(monitor.device());
        }

        // 802_11 networks: SSID is unique.
        List<String> ssids = Lists.newArrayList();
        for (Dot11NetworkDefinition net : getDot11Networks()) {
            if (ssids.contains(net.ssid())) {
                throw new InvalidConfigurationException("SSID [" + net.ssid() + "] is defined multiple times. You cannot define a network with the same SSID more than once.");
            }
            ssids.add(net.ssid());
        }

        // 802.11 networks: BSSIDs are unique for this network. (note that a BSSID can be used in multiple networks)
        for (Dot11NetworkDefinition net : getDot11Networks()) {
            List<String> bssids = Lists.newArrayList();
            for (String bssid : net.bssids()) {
                if(bssids.contains(bssid)) {
                    throw new InvalidConfigurationException("Network [" + net.ssid() + "] has at least one BSSID defined twice. You cannot define a BSSID for the same network more than once.");
                }
                bssids.add(bssid);
            }
        }
    }

    private void validateChannelList(String key) throws InvalidConfigurationException {
        int x = 0;
        for (Config c : root.getConfigList(key)) {
            String where = key + "." + "#" + x;
            try {
                for (Integer channel : c.getIntList(Keys.CHANNELS)) {
                    if (channel < 1) {
                        throw new InvalidConfigurationException("Invalid channels in list for [" + where + "}. All channels must be integers larger than 0.");
                    }
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
