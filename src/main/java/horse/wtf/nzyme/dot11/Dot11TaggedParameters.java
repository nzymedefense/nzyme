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
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
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
    public static final int PROBERESP_TAGGED_PARAMS_POSITION = 36;
    public static final int ASSOCREQ_TAGGED_PARAMS_POSITION = 28;

    public static final int WPA1_UNICAST_CYPHER_SUITE_COUNT_POSITION = 10;
    public static final int WPA2_PAIRWISE_CYPHER_SUITE_COUNT_POSITION = 6;

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

        this.parserTimer = metrics.timer(MetricRegistry.name(MetricNames.TAGGED_PARAMS_PARSE_TIMING));
        this.fingerprintTimer = metrics.timer(MetricRegistry.name(MetricNames.TAGGED_PARAMS_FINGERPRINT_TIMING));

        Timer.Context time = this.parserTimer.time();
        int position = startPosition;
        while (true) {
            try {
                int tag = 0xFF & payload[position];
                int length = 0xFF & payload[position + 1];

                byte[] tagPayload;
                if (length == 0) {
                    tagPayload = new byte[]{};
                } else {
                    tagPayload = ByteArrays.getSubArray(payload, position + 2, length);
                }
                params.put(tag, tagPayload);

                // Handle vendor specific tags.
                if (tag == 221) {
                    try {
                        // Read vendor OUI and type into a string we can reference later.
                        String oui = BaseEncoding.base16()
                                .withSeparator(":", 2)
                                .upperCase()
                                .encode(ByteArrays.getSubArray(tagPayload, 0, 3));
                        int type = 0xFF & tagPayload[3];

                        vendorSpecificParams.put(oui + "-" + type, tagPayload);
                    } catch(ArrayIndexOutOfBoundsException e) {
                        LOG.debug("Tag 221 out of bounds/malformed. Skipping.", e);
                    }
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

    public List<Dot11SecurityConfiguration> getSecurityConfiguration() {
        ImmutableList.Builder<Dot11SecurityConfiguration> configurations = new ImmutableList.Builder<>();
        int found = 0;

        // WPA 1.
        if (vendorSpecificParams.containsKey(ID_VENDOR_SPECIFIC_WPA)) {
            try {
                byte[] wpa1 = vendorSpecificParams.get(ID_VENDOR_SPECIFIC_WPA);
                LOG.trace("WPA1 payload: {}", () -> Tools.byteArrayToHexPrettyPrint(wpa1));

                List<Dot11SecurityConfiguration.ENCRYPTION_MODE> encryptionModes = parseEncryptionModes(WPA1_UNICAST_CYPHER_SUITE_COUNT_POSITION, wpa1);
                List<Dot11SecurityConfiguration.KEY_MGMT_MODE> keyMgmtModes = parseKeyMgmtModes(WPA1_UNICAST_CYPHER_SUITE_COUNT_POSITION + (encryptionModes.size() * 4) + 2, wpa1);

                configurations.add(Dot11SecurityConfiguration.create(
                        Dot11SecurityConfiguration.MODE.WPA1,
                        keyMgmtModes,
                        encryptionModes
                ));
            } catch(Exception e) {
                LOG.error("Could not parse WPA1 information from frame.", e);
            } finally {
                found++;
            }
        }

        // WPA 2.
        if (params.containsKey(ID_RSN)) {
            try {
                byte[] wpa2 = params.get(ID_RSN);
                LOG.trace("WPA2 payload: {}", () -> Tools.byteArrayToHexPrettyPrint(wpa2));

                List<Dot11SecurityConfiguration.ENCRYPTION_MODE> encryptionModes = parseEncryptionModes(WPA2_PAIRWISE_CYPHER_SUITE_COUNT_POSITION, wpa2);
                List<Dot11SecurityConfiguration.KEY_MGMT_MODE> keyMgmtModes = parseKeyMgmtModes(WPA2_PAIRWISE_CYPHER_SUITE_COUNT_POSITION + (encryptionModes.size() * 4) + 2, wpa2);

                configurations.add(Dot11SecurityConfiguration.create(
                        Dot11SecurityConfiguration.MODE.WPA2,
                        keyMgmtModes,
                        encryptionModes
                ));
            } catch(Exception e) {
                LOG.error("Could not parse WPA2 information from frame.", e);
            } finally {
                found++;
            }
        }

        if (found == 0) {
            configurations.add(Dot11SecurityConfiguration.create(
                    Dot11SecurityConfiguration.MODE.NONE,
                    Collections.emptyList(),
                    Collections.emptyList()
            ));
        }

        return configurations.build();
    }

    public boolean isWPA1() {
        return vendorSpecificParams.containsKey(ID_VENDOR_SPECIFIC_WPA);
    }

    public boolean isWPA2() {
        return params.containsKey(ID_RSN);
    }

    public boolean isWPS() {
        return vendorSpecificParams.containsKey(ID_VENDOR_SPECIFIC_WPS);
    }

    public String getFullSecurityString() {
        return Joiner.on(", ").join(getSecurityStrings());
    }

    public List<String> getSecurityStrings() {
        ImmutableList.Builder<String> x = new ImmutableList.Builder<>();
        getSecurityConfiguration().forEach(s -> x.add(s.asString()));

        return x.build();
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

    private List<Byte> parseSuites(int suiteCount, byte[] payload, int offset) {
        ImmutableList.Builder<Byte> result = new ImmutableList.Builder<>();
        for(int i = 0; i < suiteCount; i++) {
            byte[] suite = ByteArrays.getSubArray(payload, offset+2, 4);
            result.add(suite[3]);
            offset += 4;
        }

        return result.build();
    }

    private List<Dot11SecurityConfiguration.ENCRYPTION_MODE> parseEncryptionModes(int startPosition, byte[] payload) {
        ImmutableList.Builder<Dot11SecurityConfiguration.ENCRYPTION_MODE> encryptionModes = new ImmutableList.Builder<>();

        int cypherSuitesCount = payload[startPosition];
        parseSuites(cypherSuitesCount, payload, startPosition).forEach(suite -> {
            switch(suite) {
                case 2:
                    encryptionModes.add(Dot11SecurityConfiguration.ENCRYPTION_MODE.TKIP);
                    break;
                case 4:
                    encryptionModes.add(Dot11SecurityConfiguration.ENCRYPTION_MODE.CCMP);
                    break;
                default:
                    encryptionModes.add(Dot11SecurityConfiguration.ENCRYPTION_MODE.UNKNOWN);
            }
        });

        return encryptionModes.build();
    }

    private List<Dot11SecurityConfiguration.KEY_MGMT_MODE> parseKeyMgmtModes(int startPosition, byte[] payload) {
        ImmutableList.Builder<Dot11SecurityConfiguration.KEY_MGMT_MODE> keyMgmtModes = new ImmutableList.Builder<>();

        int keyMgmtModesCount = payload[startPosition];
        parseSuites(keyMgmtModesCount, payload, startPosition).forEach(suite -> {
            switch(suite) {
                case 1:
                    keyMgmtModes.add(Dot11SecurityConfiguration.KEY_MGMT_MODE.EAM);
                    break;
                case 2:
                    keyMgmtModes.add(Dot11SecurityConfiguration.KEY_MGMT_MODE.PSK);
                    break;
                default:
                    keyMgmtModes.add(Dot11SecurityConfiguration.KEY_MGMT_MODE.UNKNOWN);
            }
        });

        return keyMgmtModes.build();
    }

    public class NoSuchTaggedElementException extends Exception {
    }

}
