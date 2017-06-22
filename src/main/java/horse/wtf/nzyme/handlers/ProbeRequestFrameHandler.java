package horse.wtf.nzyme.handlers;

import horse.wtf.nzyme.*;
import horse.wtf.nzyme.graylog.GraylogFieldNames;
import horse.wtf.nzyme.graylog.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.packet.Dot11ProbeRequestPacket;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.RadiotapPacket;

import java.text.Normalizer;

public class ProbeRequestFrameHandler extends FrameHandler {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    public ProbeRequestFrameHandler(Nzyme nzyme) {
        super(nzyme);
    }

    @Override
    public void handle(byte[] payload, RadiotapPacket.RadiotapHeader header) throws IllegalRawDataException  {
        tick();

        Dot11ProbeRequestPacket probeRequest = Dot11ProbeRequestPacket.newPacket(payload, 0, payload.length);

        if (probeRequest.getHeader() == null) {
            malformed();
            LOG.trace("Malformed header in probe request packet. Skipping.");
            return;
        }

        String ssid;
        boolean nullProbe = false;
        if (probeRequest.getHeader().getSsid() != null) {
            // Check if the SSID is valid UTF-8 (might me malformed frame)
            if(!Tools.isValidUTF8(probeRequest.getHeader().getSsid().getRawData())) {
                malformed();
                LOG.trace("Malformed SSID in probe request packet. Skipping.");
                return;
            }

            ssid = Normalizer.normalize(probeRequest.getHeader().getSsid().getSsid(), Normalizer.Form.NFD);

            if (ssid.trim().isEmpty()) {
                ssid = "NULL";
                nullProbe = true;
            }
        } else {
            malformed();
            LOG.trace("Malformed SSID in probe request packet. Skipping.");
            return;
        }

        String requester;
        if (probeRequest.getHeader().getAddress2() != null) {
            requester = Normalizer.normalize(probeRequest.getHeader().getAddress2().toString(), Normalizer.Form.NFD);
        } else {
            malformed();
            LOG.trace("Malformed SSID in probe request packet. Skipping.");
            return;
        }

        String message;
        if(!nullProbe) {
            message = "Probe request: " + requester + " is looking for " + ssid;
        } else {
            message = "Probe request: " + requester + " is looking for any network. (null probe request)";
        }

        nzyme.getStatistics().tickProbingDevice(requester);

        nzyme.getGraylogUplink().notify(
                new Notification(message)
                        .addField(GraylogFieldNames.SSID, ssid)
                        .addField(GraylogFieldNames.TRANSMITTER, requester)
                        .addField(GraylogFieldNames.SUBTYPE, "probe-req")
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "probe-req";
    }

}
