package horse.wtf.nzyme.dot11;

import horse.wtf.nzyme.Tools;
import horse.wtf.nzyme.handlers.FrameHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.util.ByteArrays;

import java.nio.charset.Charset;
import java.util.Arrays;

public class Dot11SSID {

    private static final Logger LOG = LogManager.getLogger(Dot11SSID.class);

    public static String extractSSID(FrameHandler handler, int lengthPos, int ssidPos, byte[] payload) {
        // Check bounds for SSID length field.
        try {
            ByteArrays.validateBounds(payload, 0, lengthPos + 1);
        } catch (Exception e) {
            handler.malformed();
            LOG.trace("Payload out of bounds. (1) Ignoring.");
            return null;
        }

        byte ssidLength = payload[lengthPos];

        if (ssidLength < 0) {
            handler.malformed();
            LOG.trace("Negative SSID length. Ignoring.");
            return null;
        }

        // Check bounds for SSID field.
        try {
            ByteArrays.validateBounds(payload, ssidPos, ssidLength);
        } catch (Exception e) {
            handler.malformed();
            LOG.trace("Payload out of bounds. (2) Ignoring.");
            return null;
        }

        // Extract SSID
        byte[] ssidBytes = ByteArrays.getSubArray(payload, ssidPos, ssidLength);

        // Check if the SSID is valid UTF-8 (might me malformed frame)
        if (!Tools.isValidUTF8(ssidBytes)) {
            handler.malformed();
            LOG.trace("SSID not valid UTF8. Ignoring.");
            return null;
        }

        if (ssidLength >= 0) {
            return new String(ssidBytes, Charset.forName("UTF-8"));
        } else {
            return null;
        }
    }

}
