package app.nzyme.core.dot11.monitoring.disco.monitormethods;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.monitoring.MonitoredSSID;
import app.nzyme.core.dot11.monitoring.disco.monitormethods.manualthreshold.ManualThresholdDiscoMonitor;
import app.nzyme.core.dot11.monitoring.disco.monitormethods.noop.NoOpDiscoMonitor;

import javax.annotation.Nullable;
import java.util.Map;

public class DiscoMonitorFactory {

    public static Dot11DiscoMonitorMethod build(NzymeNode nzyme,
                                                DiscoMonitorMethodType method,
                                                MonitoredSSID monitoredNetwork) {
        return build(nzyme, method, monitoredNetwork, null);
    }

    public static Dot11DiscoMonitorMethod build(NzymeNode nzyme,
                                                DiscoMonitorMethodType method,
                                                MonitoredSSID monitoredNetwork,
                                                @Nullable Map<String, Object> customConfig) {
        switch (method) {
            case NOOP:
                return new NoOpDiscoMonitor(nzyme, monitoredNetwork, customConfig);
            case STATIC_THRESHOLD:
                return new ManualThresholdDiscoMonitor(nzyme, monitoredNetwork, customConfig);
            default:
                throw new RuntimeException("Unknown Disco Monitor method of type [" + method.name() + "].");
        }
    }

}
