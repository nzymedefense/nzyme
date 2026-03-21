package app.nzyme.core.monitors;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.util.filters.Filters;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.UUID;

public class Monitors {

    private final NzymeNode nzyme;

    public Monitors(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public void createMonitor(MonitorType type,
                              String name,
                              String description,
                              @Nullable List<UUID> taps,
                              int triggerCondition,
                              int interval,
                              Filters filters,
                              UUID organizationId,
                              UUID tenantId) {

    }

}
