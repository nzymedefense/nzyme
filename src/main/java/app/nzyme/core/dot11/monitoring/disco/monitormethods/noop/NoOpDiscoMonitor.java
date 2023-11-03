package app.nzyme.core.dot11.monitoring.disco.monitormethods.noop;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.DiscoHistogramEntry;
import app.nzyme.core.dot11.db.monitoring.MonitoredSSID;
import app.nzyme.core.dot11.monitoring.disco.DiscoMonitorAnomaly;
import app.nzyme.core.dot11.monitoring.disco.monitormethods.Dot11DiscoMonitorMethod;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NoOpDiscoMonitor extends Dot11DiscoMonitorMethod {

    public NoOpDiscoMonitor(NzymeNode nzyme,
                            MonitoredSSID monitoredNetwork,
                            @Nullable Map<String, Object> customConfig) {
        super(nzyme, monitoredNetwork, customConfig);
    }

    @Override
    protected List<DiscoMonitorAnomaly> calculate(List<DiscoHistogramEntry> histogram) {
        return Collections.emptyList();
    }

}
