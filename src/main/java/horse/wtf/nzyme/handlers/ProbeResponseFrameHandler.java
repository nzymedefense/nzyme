package horse.wtf.nzyme.handlers;

import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.Tools;
import horse.wtf.nzyme.dot11.Dot11ManagementFrame;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.graylog.GraylogFieldNames;
import horse.wtf.nzyme.graylog.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.util.ByteArrays;

import java.nio.charset.Charset;

public class ProbeResponseFrameHandler extends FrameHandler {

    private static final Logger LOG = LogManager.getLogger(ProbeResponseFrameHandler.class);

    // TODO kek this should really be handled in the Dot11ProbeResponsePacket at some point
    private static final int SSID_LENGTH_POSITION = 37;
    private static final int SSID_POSITION = 38;

    public ProbeResponseFrameHandler(Nzyme nzyme) {
        super(nzyme);
    }

    // TODO extract and share SSID parsing

    @Override
    public void handle(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException {
        tick();

        Dot11ManagementFrame probeReponse = Dot11ManagementFrame.newPacket(payload, 0, payload.length);

        // Check bounds for SSID length field.
        try {
            ByteArrays.validateBounds(payload, 0, SSID_LENGTH_POSITION + 1);
        } catch (Exception e) {
            malformed();
            LOG.trace("Payload out of bounds. (1) Ignoring.");
            return;
        }

        byte ssidLength = payload[SSID_LENGTH_POSITION];

        if (ssidLength < 0) {
            malformed();
            LOG.trace("Negative SSID length. Ignoring.");
            return;
        }

        // Check bounds for SSID field.
        try {
            ByteArrays.validateBounds(payload, SSID_POSITION, ssidLength);
        } catch (Exception e) {
            malformed();
            LOG.trace("Payload out of bounds. (2) Ignoring.");
            return;
        }

        // Extract SSID
        byte[] ssidBytes = ByteArrays.getSubArray(payload, SSID_POSITION, ssidLength);

        // Check if the SSID is valid UTF-8 (might me malformed frame)
        if (!Tools.isValidUTF8(ssidBytes)) {
            malformed();
            LOG.trace("SSID not valid UTF8. Ignoring.");
            return;
        }

        String ssid = null;
        if (ssidLength >= 0) {
            ssid = new String(ssidBytes, Charset.forName("UTF-8"));
        }

        String destination = "";
        if (probeReponse.getHeader().getAddress1() != null) {
            destination = probeReponse.getHeader().getAddress1().toString();
        }

        String transmitter = "";
        if (probeReponse.getHeader().getAddress2() != null) {
            transmitter = probeReponse.getHeader().getAddress2().toString();
        }

        String message = transmitter + " responded to probe request from " + destination + " for " + ssid;

        nzyme.getGraylogUplink().notify(
                new Notification(message, nzyme.getChannelHopper().getCurrentChannel())
                        .addField(GraylogFieldNames.DESTINATION, destination)
                        .addField(GraylogFieldNames.TRANSMITTER, transmitter)
                        .addField(GraylogFieldNames.SSID, ssid)
                        .addField(GraylogFieldNames.SUBTYPE, "probe-resp"),
                meta
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "probe-resp";
    }

}
