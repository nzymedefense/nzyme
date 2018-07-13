package horse.wtf.nzyme.configuration;

import com.beust.jcommander.internal.Lists;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.notifications.uplinks.graylog.GraylogAddress;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URI;
import java.util.List;

public class Configuration {

    // TODO XXX YOLO: validation (completeness, correctness), tests (against example.conf)

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

    public URI getWebInterfaceListenUri() {
        return URI.create(interfaces.getString("web_interface_listen_uri"));
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
