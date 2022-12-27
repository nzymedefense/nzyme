/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package app.nzyme.core.dot11.misc;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.nzyme.core.dot11.frames.Dot11BeaconFrame;
import app.nzyme.core.dot11.interceptors.misc.PwnagotchiAdvertisement;
import app.nzyme.core.util.Tools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pcap4j.util.ByteArrays;

import java.io.IOException;
import java.util.Optional;

public class PwnagotchiAdvertisementExtractor {

    private static final Logger LOG = LogManager.getLogger(PwnagotchiAdvertisementExtractor.class);

    private final ObjectMapper om;

    public PwnagotchiAdvertisementExtractor() {
        this.om = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Optional<PwnagotchiAdvertisement> extract(Dot11BeaconFrame frame) {
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
                LOG.debug("Could not parse 802.11 tagged parameters for pwnagotchi advertisement detection.", e);
                return Optional.empty();
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
                    return Optional.empty();
                }

                return Optional.of(parsed);
            } catch (IOException e) {
                LOG.warn("Failed to parse what looked like a pwnagotchi advertisement payload. Please report this exception " +
                        "to the nzyme team, including the following payload: {} {}", e, Tools.byteArrayToHexPrettyPrint(payload));
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

}
