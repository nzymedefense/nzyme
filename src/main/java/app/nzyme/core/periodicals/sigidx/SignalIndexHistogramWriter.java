package app.nzyme.core.periodicals.sigidx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import app.nzyme.core.NzymeLeader;
import app.nzyme.core.dot11.networks.BSSID;
import app.nzyme.core.dot11.networks.Channel;
import app.nzyme.core.dot11.networks.SSID;
import app.nzyme.core.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class SignalIndexHistogramWriter extends Periodical {

    private static final Logger LOG = LogManager.getLogger(SignalIndexHistogramWriter.class);

    private final NzymeLeader nzyme;
    private final ObjectMapper om;

    public SignalIndexHistogramWriter(NzymeLeader nzyme) {
        this.nzyme = nzyme;
        this.om = new ObjectMapper();
    }

    @Override
    protected void execute() {
        LOG.debug("Updating signal index histograms.");

        List<BSSID> bssids = Lists.newArrayList(nzyme.getNetworks().getBSSIDs().values());
        for (BSSID bssid : bssids) {
            List<SSID> ssids = Lists.newArrayList(bssid.ssids().values());
            for (SSID ssid : ssids) {
                if (!ssid.isHumanReadable()) {
                    continue;
                }

                List<Channel> channels = Lists.newArrayList(ssid.channels().values());
                for (Channel channel : channels) {
                    final String histogram;
                    try {
                        histogram = om.writeValueAsString(channel.signalStrengthTable().getSignalDistributionHistogram());
                    } catch (JsonProcessingException e) {
                        LOG.error("Could not write signal index histogram to JSON for BSSID [{}].", bssid, e);
                        continue;
                    }

                    nzyme.getDatabase().useHandle(handle -> {
                        handle.execute("INSERT INTO sigidx_histogram_history(bssid, ssid, channel, histogram, created_at) " +
                                "VALUES(?, ?, ?, ?, current_timestamp at time zone 'UTC')",
                                bssid.bssid(), ssid.nameSafe(), channel.channelNumber(), histogram);
                    });
                }
            }
        }
    }

    @Override
    public String getName() {
        return "SignalIndexHistogramWriter";
    }

}
