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
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.sun.xml.internal.bind.v2.util.CollisionCheckStack;
import horse.wtf.nzyme.util.MetricNames;
import horse.wtf.nzyme.util.Tools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.util.ByteArrays;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

public class Dot11TaggedParameters {

    private static final Logger LOG = LogManager.getLogger(Dot11TaggedParameters.class);

    public static final int BEACON_TAGGED_PARAMS_POSITION = 36;
    public static final int PROBERESP_TAGGED_PARAMS_POSITION = 24;
    public static final int ASSOCREQ_TAGGED_PARAMS_POSITION = 28;

    public static ImmutableList<Integer> FINGERPRINT_IDS = new ImmutableList.Builder<Integer>()
            .add(1)   // Supported Rates
            .add(7)   // Country Information
            .add(45)  // HT Capabilities
            .add(48)  // RSN
            .add(50)  // Extended Supported Rates
            .add(127) // Extended Capabilities
            .build();

    private static final int ID_SSID = 0;
    private static final int ID_RSN = 48;

    private static final String ID_VENDOR_SPECIFIC_WPS = "00:50:F2-4";
    private static final String ID_VENDOR_SPECIFIC_WPA = "00:50:F2-1";

    private final TreeMap<Integer, byte[]> params;
    private final TreeMap<String, byte[]> vendorSpecificParams;

    private final Timer parserTimer;
    private final Timer fingerprintTimer;

    public Dot11TaggedParameters(MetricRegistry metrics, int startPosition, byte[] payload) throws MalformedFrameException {
        this.params = Maps.newTreeMap();
        this.vendorSpecificParams = Maps.newTreeMap();

        this.parserTimer = metrics.timer(MetricRegistry.name(MetricNames.TAGGED_PARAMS_PARSE_TIMER));
        this.fingerprintTimer = metrics.timer(MetricRegistry.name(MetricNames.TAGGED_PARAMS_FINGERPRINT_TIMER));

        Timer.Context time = this.parserTimer.time();
        int position = startPosition;
        while (true) {
            try {
                int tag = 0xFF & payload[position];
                byte length = payload[position + 1];

                byte[] tagPayload;
                if (length == 0) {
                    tagPayload = new byte[]{};
                } else {
                    tagPayload = ByteArrays.getSubArray(payload, position + 2, length);
                }
                params.put(tag, tagPayload);

                // Handle vendor specific tags.
                if (tag == 221) {
                    // Read vendor OUI and type into a string we can reference later.
                    String oui = BaseEncoding.base16()
                            .withSeparator(":", 2)
                            .upperCase()
                            .encode(ByteArrays.getSubArray(tagPayload, 0, 3));
                    int type = 0xFF & tagPayload[3];

                    vendorSpecificParams.put(oui + "-" + type, tagPayload);
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
        return params.containsKey(ID_RSN);
    }

    public List<Dot11SecurityConfiguration> getSecurityConfiguration() {
        ImmutableList.Builder<Dot11SecurityConfiguration> configurations = new ImmutableList.Builder<>();

        // WPA.
        if (vendorSpecificParams.containsKey(ID_VENDOR_SPECIFIC_WPA)) {
            configurations.add(Dot11SecurityConfiguration.create(
                    Dot11SecurityConfiguration.MODE.WPA_1,
                    Collections.emptyList(),
                    Collections.emptyList()
            ));

            // parse wpa 1 key mgmt modes

            // parse wpa 1 encryption modes
        }

        // WPA 2.
        if (params.containsKey(ID_RSN)) {
            configurations.add(Dot11SecurityConfiguration.create(
                    Dot11SecurityConfiguration.MODE.WPA_2,
                    Collections.emptyList(),
                    Collections.emptyList()
            ));

            // parse wpa 2 key mgmt modes

            // parse wpa 2 encryption modes
        }

        // TODO figure out WEP and other msising info

        return configurations.build();
    }

    public boolean isWPS() {
        return vendorSpecificParams.containsKey(ID_VENDOR_SPECIFIC_WPS);
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
                throw new MalformedFrameException();
            }

            return new String(bytes, Charsets.UTF_8);
        }
    }

    public String fingerprint() {
        Timer.Context time = this.fingerprintTimer.time();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        // Add all payloads of default tags.
        params.forEach((k,v) -> {
            try {
                if (FINGERPRINT_IDS.contains(k)) {
                    bytes.write(v);
                }
            } catch (IOException e) {
                LOG.error("Could not assemble bytes for fingerprinting.", e);
            }
        });

        // Add sequence of vendor specific tags.
        vendorSpecificParams.forEach((k,v) -> {
            try {
                bytes.write(k.getBytes());
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
