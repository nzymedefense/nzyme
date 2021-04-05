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

package horse.wtf.nzyme.dot11.parsers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.dot11.*;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import org.pcap4j.packet.IllegalRawDataException;


public class Dot11BeaconFrameParser extends Dot11FrameParser<Dot11BeaconFrame> {

    private final Anonymizer anonymizer;

    public Dot11BeaconFrameParser(MetricRegistry metrics, Anonymizer anonymizer) {
        super(metrics);

        this.anonymizer = anonymizer;
    }

    @Override
    protected Dot11BeaconFrame doParse(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException, MalformedFrameException {
        Dot11ManagementFrame beacon = Dot11ManagementFrame.newPacket(payload, 0, payload.length);
        Dot11TaggedParameters taggedParameters = new Dot11TaggedParameters(metrics, Dot11TaggedParameters.BEACON_TAGGED_PARAMS_POSITION, payload);

        String transmitter = "";
        if(beacon.getHeader().getAddress2() != null) {
            transmitter = beacon.getHeader().getAddress2().toString();
        }

        String ssid;
        try {
            ssid = taggedParameters.getSSID();
        } catch(Dot11TaggedParameters.NoSuchTaggedElementException e) {
            // Broadcast/Wildcard beacon.
            ssid = null;
        }

        if (anonymizer.isEnabled()) {
            ssid = anonymizer.anonymizeSSID(ssid);
            transmitter = anonymizer.anonymizeBSSID(transmitter);
        }

        return Dot11BeaconFrame.create(ssid, transmitter, taggedParameters.fingerprint(), taggedParameters, meta, payload, header);
    }

}
