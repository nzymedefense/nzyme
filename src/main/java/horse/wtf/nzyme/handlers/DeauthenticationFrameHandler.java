package horse.wtf.nzyme.handlers;

import horse.wtf.nzyme.*;
import horse.wtf.nzyme.dot11.Dot11DeauthPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.RadiotapPacket;
import org.pcap4j.util.ByteArrays;

public class DeauthenticationFrameHandler extends FrameHandler {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    public DeauthenticationFrameHandler(Nzyme nzyme) {
        super(nzyme);
    }

    private static final int DEAUTH_REASON_POSITION = 25;
    private static final int DEAUTH_REASON_LENGTH = 2;


    @Override
    public void handle(byte[] payload, RadiotapPacket.RadiotapHeader header) throws IllegalRawDataException {
        try {
            ByteArrays.validateBounds(payload, 0, DEAUTH_REASON_POSITION+DEAUTH_REASON_LENGTH+1);
        } catch(Exception e) { return; }

        Dot11DeauthPacket deauth = Dot11DeauthPacket.newPacket(payload, 0, payload.length);

        String receiver = "";
        if (deauth.getHeader().getAddress1() != null) {
            receiver = deauth.getHeader().getAddress1().toString();
        }

        String transmitter = "";
        if (deauth.getHeader().getAddress2() != null) {
            transmitter = deauth.getHeader().getAddress2().toString();
        }

        String bssid = "";
        if (deauth.getHeader().getAddress3() != null) {
            bssid = deauth.getHeader().getAddress3().toString();
        }

        String message = "Deauth: Transmitter " + transmitter + " is deauthenticating " + receiver
                + " from BSSID " + bssid;

        nzyme.getGraylogUplink().notify(
                new Notification(message)
                        .addField(GraylogFieldNames.TRANSMITTER, transmitter)
                        .addField(GraylogFieldNames.RECEIVER, receiver)
                        .addField(GraylogFieldNames.BSSID, bssid)
                        .addField(GraylogFieldNames.SUBTYPE, "deauth")
        );


        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "deauth";
    }

}
