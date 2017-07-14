package horse.wtf.nzyme.handlers;

import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.Dot11ManagementFrame;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.graylog.GraylogFieldNames;
import horse.wtf.nzyme.graylog.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.util.ByteArrays;

import java.nio.ByteOrder;

public class AssociationResponseFrameHandler extends FrameHandler {

    private static final Logger LOG = LogManager.getLogger(AssociationResponseFrameHandler.class);

    private static final int STATUS_CODE_POSITION = 26;
    private static final int STATUS_CODE_LENGTH = 2;

    public AssociationResponseFrameHandler(Nzyme nzyme) {
        super(nzyme);
    }

    @Override
    public void handle(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException {
        tick();

        Dot11ManagementFrame associationResponse = Dot11ManagementFrame.newPacket(payload, 0, payload.length);

        // Check bounds for response code field.
        try {
            ByteArrays.validateBounds(payload, 0, STATUS_CODE_POSITION+STATUS_CODE_LENGTH-1);
        } catch(Exception e) {
            malformed();
            LOG.trace("Payload out of bounds. (1) Ignoring.");
            return;
        }

        // Parse the response code. 0 means success any other value means failure.
        short responseCode = ByteArrays.getShort(new byte[]{payload[26], payload[27]}, 0, ByteOrder.LITTLE_ENDIAN);

        if(responseCode < 0) {
            LOG.trace("Invalid response code <{}>.", responseCode);
            return;
        }

        String response = "refused";
        if (responseCode == 0) {
            response = "success";
        }

        String destination = "";
        if(associationResponse.getHeader().getAddress1() != null) {
            destination = associationResponse.getHeader().getAddress1().toString();
        }

        String transmitter = "";
        if(associationResponse.getHeader().getAddress2() != null) {
            transmitter = associationResponse.getHeader().getAddress2().toString();
        }

        String message = transmitter + " answered association request from " + destination
                + ". Response: " + response.toUpperCase() + " (" + responseCode + ")";

        nzyme.notify(
                new Notification(message, nzyme.getChannelHopper().getCurrentChannel())
                        .addField(GraylogFieldNames.TRANSMITTER, transmitter)
                        .addField(GraylogFieldNames.DESTINATION, destination)
                        .addField(GraylogFieldNames.RESPONSE_CODE, responseCode)
                        .addField(GraylogFieldNames.RESPONSE_STRING, response)
                        .addField(GraylogFieldNames.SUBTYPE, "assoc-resp"),
                meta
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "assoc-resp";
    }
}
