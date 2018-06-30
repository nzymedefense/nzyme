/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.dot11;

import horse.wtf.nzyme.util.Tools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.util.ByteArrays;

import java.nio.charset.Charset;

public class Dot11SSID {

    private static final Logger LOG = LogManager.getLogger(Dot11SSID.class);

    public static String extractSSID(int lengthPos, int ssidPos, byte[] payload) throws MalformedFrameException {
        // Check bounds for SSID length field.
        try {
            ByteArrays.validateBounds(payload, 0, lengthPos + 1);
        } catch (Exception e) {
            LOG.trace("Payload out of bounds. (1)");
            throw new MalformedFrameException();
        }

        byte ssidLength = payload[lengthPos];

        if (ssidLength < 0) {
            LOG.trace("Negative SSID length.");
            throw new MalformedFrameException();
        }

        // Check bounds for SSID field.
        try {
            ByteArrays.validateBounds(payload, ssidPos, ssidLength);
        } catch (Exception e) {
            LOG.trace("Payload out of bounds. (2)");
            throw new MalformedFrameException();
        }

        // Extract SSID
        byte[] ssidBytes = ByteArrays.getSubArray(payload, ssidPos, ssidLength);

        // Check if the SSID is valid UTF-8 (might me malformed frame)
        if (!Tools.isValidUTF8(ssidBytes)) {
            LOG.trace("SSID not valid UTF8.");
            throw new MalformedFrameException();
        }

        if (ssidLength >= 0) {
            return new String(ssidBytes, Charset.forName("UTF-8"));
        } else {
            return null;
        }
    }

}
