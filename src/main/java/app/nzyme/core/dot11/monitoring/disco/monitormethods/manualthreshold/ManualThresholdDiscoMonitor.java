package app.nzyme.core.dot11.monitoring.disco.monitormethods.manualthreshold;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.DiscoHistogramEntry;
import app.nzyme.core.dot11.db.monitoring.MonitoredSSID;
import app.nzyme.core.dot11.monitoring.disco.DiscoMonitorAnomaly;
import app.nzyme.core.dot11.monitoring.disco.monitormethods.Dot11DiscoMonitorMethod;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class ManualThresholdDiscoMonitor extends Dot11DiscoMonitorMethod {

    private final int threshold;

    public ManualThresholdDiscoMonitor(NzymeNode nzyme,
                                       MonitoredSSID monitoredNetwork,
                                       @Nullable Map<String, Object> customConfig) {
        super(nzyme, monitoredNetwork, customConfig);


        try {
            ManualThresholdConfiguration config;
            if (customConfig == null) {
                config = new ObjectMapper().readValue(
                        monitoredNetwork.discoMonitorConfiguration(),
                        ManualThresholdConfiguration.class
                );
            } else {
                config = new ObjectMapper().convertValue(customConfig, ManualThresholdConfiguration.class);
            }

            this.threshold = config.threshold();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not parse configuration for monitored network " +
                    "[" + monitoredNetwork.uuid() + "].");
        }
    }

    @Override
    protected List<DiscoMonitorAnomaly> calculate(List<DiscoHistogramEntry> histogram) {
        List<DiscoMonitorAnomaly> anomalies = Lists.newArrayList();

        for (DiscoHistogramEntry reading : histogram) {
            if (reading.frameCount() > threshold) {
                anomalies.add(DiscoMonitorAnomaly.create(reading.bucket(), reading.frameCount()));
            }
        }

        return anomalies;
    }

}
