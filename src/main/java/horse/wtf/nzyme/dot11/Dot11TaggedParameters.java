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

import com.google.common.collect.Maps;
import horse.wtf.nzyme.util.Tools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.util.ByteArrays;

import java.nio.charset.Charset;
import java.util.Map;

public class Dot11TaggedParameters {

    private static final Logger LOG = LogManager.getLogger(Dot11TaggedParameters.class);

    public static final int BEACON_TAGGED_PARAMS_POSITION = 36;
    public static final int PROBERESP_TAGGED_PARAMS_POSITION = 24;
    public static final int ASSOCREQ_TAGGED_PARAMS_POSITION = 28;

    private static final Byte ID_SSID = 0;

    private final Map<Byte, byte[]> params;

    public Dot11TaggedParameters(int startPosition, byte[] payload) throws MalformedFrameException {
        this.params = Maps.newHashMap();

        int position = startPosition;
        while (true) {
            try {
                byte tag = payload[position];
                byte length = payload[position + 1];

                if (length == 0) {
                    params.put(tag, new byte[]{});
                } else {
                    params.put(tag, ByteArrays.getSubArray(payload, position + 2, length));
                }

                position = position + length + 2; // 2 = tag+length offset

                if(position >= payload.length) {
                    // fin
                    break;
                }
            } catch (Exception e) {
                throw new MalformedFrameException("Could not parse 802.11 tagged parameters.", e);
            }
        }

    }

    public boolean hasSSID() {
        return params.containsKey(ID_SSID);
    }

    public String getSSID() throws MalformedFrameException, NoSuchTaggedElementException {
        if (!params.containsKey(ID_SSID)) {
            throw new NoSuchTaggedElementException();
        } else {
            byte[] bytes = params.get(ID_SSID);

            if(bytes.length == 0) {
                // Broadcast SSID.
                return null;
            }

            // Check if the SSID is valid UTF-8 (might me malformed frame)
            if (!Tools.isValidUTF8(bytes)) {
                LOG.trace("SSID not valid UTF8.");
                throw new MalformedFrameException();
            }

            return new String(bytes, Charset.forName("UTF-8"));
        }
    }

    public class NoSuchTaggedElementException extends Exception {
    }
}
