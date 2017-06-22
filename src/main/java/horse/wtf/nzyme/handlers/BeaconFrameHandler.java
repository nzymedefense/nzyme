package horse.wtf.nzyme.handlers;

import horse.wtf.nzyme.*;
import horse.wtf.nzyme.dot11.Dot11BeaconPacket;
import horse.wtf.nzyme.graylog.GraylogFieldNames;
import horse.wtf.nzyme.graylog.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.RadiotapPacket;
import org.pcap4j.util.ByteArrays;

import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.concurrent.atomic.AtomicInteger;

public class BeaconFrameHandler extends FrameHandler {

    // kek this should really be handled in the Dot11BeaconPacket at some point
    private static final int SSID_LENGTH_POSITION = 37;
    private static final int SSID_POSITION = 38;

    private final AtomicInteger sampleCount;

    private static final Logger LOG = LogManager.getLogger(BeaconFrameHandler.class);

    public BeaconFrameHandler(Nzyme nzyme) {
        super(nzyme);

        this.sampleCount = new AtomicInteger(0);
    }

    @Override
    public void handle(byte[] payload, RadiotapPacket.RadiotapHeader header) throws IllegalRawDataException {
        tick();
        if(nzyme.getCliArguments().getBeaconSamplingRate() != 0) { // skip this completely if sampling is disabled
            if (sampleCount.getAndIncrement() == nzyme.getCliArguments().getBeaconSamplingRate()) {
                sampleCount.set(0);
            } else {
                return;
            }
        }

        // MAC header: 24 byte
        // Fixed parameters: 12 byte
        // Tagged parameters start at: 36 byte

        Dot11BeaconPacket beacon = Dot11BeaconPacket.newPacket(payload, 0, payload.length);

        // Check bounds for SSID length field.
        try {
            ByteArrays.validateBounds(payload, 0, SSID_LENGTH_POSITION+1);
        } catch(Exception e) {
            malformed();
            LOG.trace("Beacon payload out of bounds. (1) Ignoring.");
            return;
        }

        // SSID length.
        byte ssidLength = payload[SSID_LENGTH_POSITION];

        if(ssidLength < 0) {
            malformed();
            LOG.trace("Negative SSID length. Ignoring.");
            return;
        }

        // Check bounds for SSID field.
        try {
            ByteArrays.validateBounds(payload, SSID_POSITION, ssidLength);
        } catch(Exception e) {
            malformed();
            LOG.trace("Beacon payload out of bounds. (2) Ignoring.");
            return;
        }

        // Extract SSID
        byte[] ssidBytes = ByteArrays.getSubArray(payload, SSID_POSITION, ssidLength);

        // Check if the SSID is valid UTF-8 (might me malformed frame)
        if(!Tools.isValidUTF8(ssidBytes)) {
            malformed();
            LOG.trace("Beacon SSID not valid UTF8. Ignoring.");
            return;
        }

        String ssid = null;
        if(ssidLength >= 0) {
            ssid = Normalizer.normalize(new String(ssidBytes, Charset.forName("UTF-8")), Normalizer.Form.NFD);
        }

        String transmitter = "";
        if(beacon.getHeader().getAddress2() != null) {
            transmitter = Normalizer.normalize(beacon.getHeader().getAddress3().toString(), Normalizer.Form.NFD);
        }

        String message;
        if (ssid != null) {
            message = "Received beacon from " + transmitter + " for SSID " + ssid;
            nzyme.getStatistics().tickBeaconedNetwork(ssid);
        } else {
            // Broadcast beacon.
            message = "Received broadcast beacon from " + transmitter;
        }

        nzyme.getStatistics().tickAccessPoint(transmitter);

        nzyme.getGraylogUplink().notify(
                new Notification(message)
                        .addField(GraylogFieldNames.TRANSMITTER, transmitter)
                        .addField(GraylogFieldNames.SSID, ssid)
                        .addField(GraylogFieldNames.SUBTYPE, "beacon")
        );

        LOG.debug(message);
    }

    // TODO?
    // RECEIVED x BEACON FRAMES FOR x SSIDS FROM x BSSIDS (x BROADCAST BEACONS)

    @Override
    public String getName() {
        return "beacon";
    }

}
