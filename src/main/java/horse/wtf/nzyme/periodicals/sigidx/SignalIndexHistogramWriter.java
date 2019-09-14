package horse.wtf.nzyme.periodicals.sigidx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.networks.BSSID;
import horse.wtf.nzyme.dot11.networks.Channel;
import horse.wtf.nzyme.dot11.networks.SSID;
import horse.wtf.nzyme.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SignalIndexHistogramWriter extends Periodical {

    private static final Logger LOG = LogManager.getLogger(SignalIndexHistogramWriter.class);

    private final Nzyme nzyme;
    private final ObjectMapper om;

    public SignalIndexHistogramWriter(Nzyme nzyme) {
        this.nzyme = nzyme;
        this.om = new ObjectMapper();
    }

    @Override
    protected void execute() {
        LOG.debug("Updating signal index histograms.");

        for (BSSID bssid : nzyme.getNetworks().getBSSIDs().values()) {
            for (SSID ssid : bssid.ssids().values()) {
                if (!ssid.isHumanReadable()) {
                    continue;
                }

                for (Channel channel : ssid.channels().values()) {
                    final String histogram;
                    try {
                        histogram = om.writeValueAsString(channel.signalStrengthTable().getZScoreDistributionHistogram());
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
