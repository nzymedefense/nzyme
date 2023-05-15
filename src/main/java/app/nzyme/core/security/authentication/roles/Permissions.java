package app.nzyme.core.security.authentication.roles;

import java.util.HashMap;
import java.util.Map;

public class Permissions {

    public static final Map<String, Permission> ALL = new HashMap<>(){{
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
        put("reports_manage", Permission.create(
                "reports_manage",
                "Manage Reports",
                "Allows user to create, edit and delete report configurations across data from all taps of " +
                        "tenant the user belongs to.",
                false
        ));
        put("reports_view", Permission.create(
                "reports_view",
                "View Reports",
                "Allows user to view generated reports across data from all taps of tenant the user belongs to.",
                false
        ));
        put("retrospective_view", Permission.create(
                "retrospective_view",
                "View/Query Retrospective",
                "Allows user to use the Retrospective feature using data from all taps the user has access to.",
                true
        ));
    }};

}
