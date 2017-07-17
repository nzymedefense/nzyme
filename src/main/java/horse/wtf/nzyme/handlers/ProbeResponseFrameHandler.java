package horse.wtf.nzyme.handlers;

import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.Tools;
import horse.wtf.nzyme.dot11.Dot11ManagementFrame;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.Dot11SSID;
import horse.wtf.nzyme.graylog.GraylogFieldNames;
import horse.wtf.nzyme.graylog.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.util.ByteArrays;

import java.nio.charset.Charset;
import java.util.Arrays;

public class ProbeResponseFrameHandler extends FrameHandler {

    private static final Logger LOG = LogManager.getLogger(ProbeResponseFrameHandler.class);

    public static final int SSID_LENGTH_POSITION = 37;
    public static final int SSID_POSITION = 38;

    public ProbeResponseFrameHandler(Nzyme nzyme) {
        super(nzyme);
    }

    @Override
    public void handle(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException {
        tick();

        Dot11ManagementFrame probeReponse = Dot11ManagementFrame.newPacket(payload, 0, payload.length);

        String ssid = Dot11SSID.extractSSID(this, SSID_LENGTH_POSITION, SSID_POSITION, payload);

        if (ssid == null) {
            ssid = "[no SSID]";
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

        nzyme.notify(
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
