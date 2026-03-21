package app.nzyme.core.monitors;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.constraints.UUID;
import app.nzyme.core.util.filters.Filters;

import java.util.List;

public class Monitors {

    private final NzymeNode nzyme;

    public Monitors(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public void createMonitor(MonitorType type,
                              String name,
                              String description,
                              List<String> taps,
                              int triggerCondition,
                              int interval,
                              Filters filters,
                              UUID organizationId,
                              UUID tenantId) {

    }

}
