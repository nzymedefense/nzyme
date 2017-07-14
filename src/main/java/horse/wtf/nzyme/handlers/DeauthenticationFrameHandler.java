package horse.wtf.nzyme.handlers;

import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.Dot11LeavingReason;
import horse.wtf.nzyme.dot11.Dot11ManagementFrame;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.graylog.GraylogFieldNames;
import horse.wtf.nzyme.graylog.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.packet.IllegalRawDataException;

public class DeauthenticationFrameHandler extends FrameHandler {

    private static final Logger LOG = LogManager.getLogger(DeauthenticationFrameHandler.class);

    public DeauthenticationFrameHandler(Nzyme nzyme) {
        super(nzyme);
    }

    @Override
    public void handle(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException {
        tick();

        Dot11ManagementFrame deauth = Dot11ManagementFrame.newPacket(payload, 0, payload.length);

        String destination = "";
        if (deauth.getHeader().getAddress1() != null) {
            destination = deauth.getHeader().getAddress1().toString();
        }

        String transmitter = "";
        if (deauth.getHeader().getAddress2() != null) {
            transmitter = deauth.getHeader().getAddress2().toString();
        }

        String bssid = "";
        if (deauth.getHeader().getAddress3() != null) {
            bssid = deauth.getHeader().getAddress3().toString();
        }

        // Reason.
        short reasonCode = Dot11LeavingReason.extract(payload, header);
        String reasonString = Dot11LeavingReason.lookup(reasonCode);

        String message = "Deauth: Transmitter " + transmitter + " is deauthenticating " + destination
                + " from BSSID " + bssid + " (" + reasonString + ")";

        nzyme.notify(
                new Notification(message, nzyme.getChannelHopper().getCurrentChannel())
                        .addField(GraylogFieldNames.TRANSMITTER, transmitter)
                        .addField(GraylogFieldNames.DESTINATION, destination)
                        .addField(GraylogFieldNames.BSSID, bssid)
                        .addField(GraylogFieldNames.REASON_CODE, reasonCode)
                        .addField(GraylogFieldNames.REASON_STRING, reasonString)
                        .addField(GraylogFieldNames.SUBTYPE, "deauth"),
                meta
        );

        LOG.debug(message);
    }

    @Override
    public String getName() {
        return "deauth";
    }

}
