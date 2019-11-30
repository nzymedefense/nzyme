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

package horse.wtf.nzyme.dot11.interceptors;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.alerts.PwnagotchiAdvertisementAlert;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.interceptors.misc.PwnagotchiAdvertisement;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.util.Tools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.util.ByteArrays;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PwnagotchiAdvertisementInterceptor implements Dot11FrameInterceptor<Dot11BeaconFrame> {

    private static final Logger LOG = LogManager.getLogger(PwnagotchiAdvertisementInterceptor.class);

    private final Dot11Probe probe;
    private final ObjectMapper om;

    public PwnagotchiAdvertisementInterceptor(Dot11Probe probe) {
        this.probe = probe;

        this.om = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void intercept(Dot11BeaconFrame frame) throws IllegalRawDataException {
        final byte[] payload = frame.payload();

        int position = 36;
        StringBuilder advertisement = new StringBuilder();
        while (true) {
            try {
                int tag = 0xFF & payload[position];
                int length = 0xFF & payload[position + 1];

                if (tag == 222) {
                    // This is the tag ID used by pwnagotchis.
                    byte[] tagPayload;
                    if (length == 0) {
                        tagPayload = new byte[]{};
                    } else {
                        tagPayload = ByteArrays.getSubArray(payload, position + 2, length);
                    }

                    // Maximum tag length is usually exceeded and pwnagotchi sends multiple ID 222 payloads that we'll have to concatenate.
                    advertisement.append(new String(tagPayload));
                }

                position = position + length + 2; // 2 = tag+length offset

                if(position >= payload.length) {
                    // fin
                    break;
                }
            } catch (Exception e) {
                LOG.debug("Could not parse 802.11 tagged parameters for pwnagotchi avertisement detection.", e);
            }
        }

        // Check if whatever was the payload of those type 222 tags looks like JSON. (the advertisement is JSON)
        String fullAdvertisement = advertisement.toString();
        if (fullAdvertisement.contains("{") && fullAdvertisement.contains("}") && fullAdvertisement.contains("identity")) {
            PwnagotchiAdvertisement parsed = null;
            try {
                parsed = this.om.readValue(fullAdvertisement, PwnagotchiAdvertisement.class);

                /* The alert is able to deal with NULL values but we still want a warning because it means that the
                 * pwnagotchi developers changed the advertisement protocol.
                 */
                if (parsed.identity() == null || parsed.name() == null || parsed.version() == null ||
                        parsed.uptime() == null || parsed.pwndThisRun() == null || parsed.pwndTotal() == null) {
                    LOG.warn("Unexpected pwnagotchi advertisement payload. This could mean that the pwnagotchi developers changed " +
                            "the advertisement format and it would be great if you could report this to the nzyme developers, including the " +
                            "following payload: {} {}", parsed, Tools.byteArrayToHexPrettyPrint(payload));
                }

                probe.raiseAlert(PwnagotchiAdvertisementAlert.create(parsed, frame.meta(), probe));
            } catch (IOException e) {
                LOG.warn("Failed to parse what looked like a pwnagotchi advertisement payload. Please report this exception " +
                        "to the nzyme team, including the following payload: {} {}", e, Tools.byteArrayToHexPrettyPrint(payload));
            }
        }
    }

    @Override
    public byte forSubtype() {
        return Dot11FrameSubtype.BEACON;
    }

    @Override
    public List<Class<? extends Alert>> raisesAlerts() {
        return new ArrayList<Class<? extends Alert>>(){{
            add(PwnagotchiAdvertisementAlert.class);
        }};
    }

}
