package app.nzyme.core.security.authentication.roles;

import java.util.Map;
import java.util.TreeMap;

public class Permissions {

    public static final Map<String, Permission> ALL = new TreeMap<>(){{
        put("alerts_manage", Permission.create(
                "alerts_manage",
                "Manage Alerts",
                "Allows user to create, edit and delete alert definitions across data from all taps of tenant " +
                        "the user belongs to.",
                false
        ));
        put("alerts_view", Permission.create(
                "alerts_view",
                "View/Handle Alerts",
                "Allows user to view triggered alerts across data from all taps of tenant the user belongs to.",
                false
        ));
        put("dot11_monitoring_manage", Permission.create(
                "dot11_monitoring_manage",
                "Manage Monitored WiFi Networks",
                "Allows user to create, edit and delete configurations of WiFi network monitoring, This can " +
                        "potentially trigger alerts based on data from all taps of tenant the user belongs to.",
                false
        ));
        put("mac_aliases_manage", Permission.create(
                "mac_aliases_manage",
                "Manage MAC Address Aliases",
                "Allows user to create, edit and delete MAC address aliases for all subsystems, " +
                        "e.g. 802.11/WiFi or Ethernet.",
                true
        ));
    }};

}
