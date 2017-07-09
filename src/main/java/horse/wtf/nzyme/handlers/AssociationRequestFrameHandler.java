package horse.wtf.nzyme.handlers;

import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.Dot11ManagementFrame;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.Dot11SSID;
import horse.wtf.nzyme.graylog.GraylogFieldNames;
import horse.wtf.nzyme.graylog.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.packet.IllegalRawDataException;

public class AssociationRequestFrameHandler extends FrameHandler {

    private static final Logger LOG = LogManager.getLogger(AssociationRequestFrameHandler.class);

    private static final int SSID_LENGTH_POSITION = 29;
    private static final int SSID_POSITION = 30;

    public AssociationRequestFrameHandler(Nzyme nzyme) {
        super(nzyme);
    }

    @Override
    public void handle(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException {
        tick();

        Dot11ManagementFrame associationRequest = Dot11ManagementFrame.newPacket(payload, 0, payload.length);

        String destination = "";
        if(associationRequest.getHeader().getAddress1() != null) {
            destination = associationRequest.getHeader().getAddress1().toString();
        }

        String transmitter = "";
        if(associationRequest.getHeader().getAddress2() != null) {
            transmitter = associationRequest.getHeader().getAddress2().toString();
        }

        String ssid = Dot11SSID.extractSSID(this, SSID_LENGTH_POSITION, SSID_POSITION, payload);

        if (ssid == null) {
            ssid = "[no SSID]";
        }

        String message = transmitter + " is requesting to associate with " + ssid + " at " + destination;

        nzyme.getGraylogUplink().notify(
                new Notification(message, nzyme.getChannelHopper().getCurrentChannel())
                        .addField(GraylogFieldNames.TRANSMITTER, transmitter)
                        .addField(GraylogFieldNames.DESTINATION, destination)
                        .addField(GraylogFieldNames.SSID, ssid)
                        .addField(GraylogFieldNames.SUBTYPE, "assoc-req"),
                meta
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "assoc-req";
    }

}
