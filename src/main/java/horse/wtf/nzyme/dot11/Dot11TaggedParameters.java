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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import horse.wtf.nzyme.util.MetricNames;
import horse.wtf.nzyme.util.Tools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.util.ByteArrays;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.TreeMap;

public class Dot11TaggedParameters {

    private static final Logger LOG = LogManager.getLogger(Dot11TaggedParameters.class);

    public static final int BEACON_TAGGED_PARAMS_POSITION = 36;
    public static final int PROBERESP_TAGGED_PARAMS_POSITION = 24;
    public static final int ASSOCREQ_TAGGED_PARAMS_POSITION = 28;

    public static ImmutableList<Integer> FINGERPRINT_IDS = new ImmutableList.Builder<Integer>()
            .add(1)   // Supported Rates
            .add(42)  // ERP
            .add(45)  // HT Capabilities
            .add(48)  // RSN
            .add(50)  // Extended Supported Rates
            .add(127) // Extended Capabilities
            .build();

    private static final int ID_SSID = 0;
    private static final int ID_WPA_2 = 48;

    private final TreeMap<Integer, byte[]> params;

    private final Timer parserTimer;
    private final Timer fingerprintTimer;

    public Dot11TaggedParameters(MetricRegistry metrics, int startPosition, byte[] payload) throws MalformedFrameException {
        this.params = Maps.newTreeMap();

        this.parserTimer = metrics.timer(MetricRegistry.name(MetricNames.TAGGED_PARAMS_PARSE_TIMER));
        this.fingerprintTimer = metrics.timer(MetricRegistry.name(MetricNames.TAGGED_PARAMS_FINGERPRINT_TIMER));

        Timer.Context time = this.parserTimer.time();
        int position = startPosition;
        while (true) {
            try {
                int tag = 0xFF & payload[position];
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

        time.stop();
    }

    public boolean isWPA2() {
        return params.containsKey(ID_WPA_2);
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

    public String fingerprint() {
        Timer.Context time = this.fingerprintTimer.time();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        params.forEach((k,v) -> {
            try {
                if (FINGERPRINT_IDS.contains(k)) {
                    bytes.write(v);
                }
            } catch (IOException e) {
                LOG.error("Could not assemble bytes for fingerprinting.", e);
            }
        });

        String fingerprint = Hashing.sha256().hashBytes(bytes.toByteArray()).toString();

        time.stop();
        return fingerprint;
    }

    public class NoSuchTaggedElementException extends Exception {
    }
}
