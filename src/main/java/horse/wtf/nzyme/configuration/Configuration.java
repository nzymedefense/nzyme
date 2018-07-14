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
import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.notifications.uplinks.graylog.GraylogAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URI;
import java.util.List;

public class Configuration {

    // TODO XXX YOLO: validation (completeness, correctness), tests (against example.conf)

    private static final Logger LOG = LogManager.getLogger(Nzyme.class);

    private final Config root;
    private final Config general;
    private final Config interfaces;
    private final Config python;

    // Manual properties.
    private boolean printPacketInfo = false;

    public Configuration(File configFile) {
        this.root = ConfigFactory.parseFile(configFile);

        this.general = root.getConfig("general");
        this.python = general.getConfig("python");
        this.interfaces = root.getConfig("interfaces");
    }

    public Role getRole() {
        return general.getEnum(Role.class, "role");
    }

    public String getNzymeId() {
        return general.getString("id");
    }

    public String getPythonExecutable() {
        return python.getString("executable");
    }

    public String getPythonScriptDirectory() {
        return python.getString("script_directory");
    }

    public String getPythonScriptPrefix() {
        return python.getString("script_prefix");
    }

    public boolean areVersionchecksEnabled() {
        return general.getBoolean("versionchecks");
    }

    public URI getRestListenUri() {
        return URI.create(interfaces.getString("rest_listen_uri"));
    }

    public List<Dot11MonitorDefinition> getDot11Monitors() {
        ImmutableList.Builder<Dot11MonitorDefinition> result = new ImmutableList.Builder<>();

        for (Config config : root.getConfigList("802_11_monitors")) {
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

    @Nullable
    public List<GraylogAddress> getGraylogUplinks() {
        List<String> graylogAddresses = root.getStringList("graylog_uplinks");
        if(graylogAddresses == null) {
            return null;
        }

        List<GraylogAddress> result = Lists.newArrayList();
        for (String address : graylogAddresses) {
            String[] parts = address.split(":");
            result.add(new GraylogAddress(parts[0], Integer.parseInt(parts[1])));
        }

        return result;
    }

    public boolean isPrintPacketInfo() {
        return printPacketInfo;
    }

    public void setPrintPacketInfo(boolean printPacketInfo) {
        this.printPacketInfo = printPacketInfo;
    }

}
