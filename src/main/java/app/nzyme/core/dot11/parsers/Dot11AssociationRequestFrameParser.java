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

package app.nzyme.core.dot11.parsers;

import com.codahale.metrics.MetricRegistry;
import app.nzyme.core.dot11.Dot11ManagementFrame;
import app.nzyme.core.dot11.Dot11MetaInformation;
import app.nzyme.core.dot11.Dot11TaggedParameters;
import app.nzyme.core.dot11.MalformedFrameException;
import app.nzyme.core.dot11.anonymization.Anonymizer;
import app.nzyme.core.dot11.frames.Dot11AssociationRequestFrame;
import org.pcap4j.packet.IllegalRawDataException;

public class Dot11AssociationRequestFrameParser extends Dot11FrameParser<Dot11AssociationRequestFrame> {

    private final Anonymizer anonymizer;

    public Dot11AssociationRequestFrameParser(MetricRegistry metrics, Anonymizer anonymizer) {
        super(metrics);

        this.anonymizer = anonymizer;
    }

    @Override
    protected Dot11AssociationRequestFrame doParse(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException, MalformedFrameException {
        Dot11ManagementFrame associationRequest = Dot11ManagementFrame.newPacket(payload, 0, payload.length);
        Dot11TaggedParameters taggedParameters = new Dot11TaggedParameters(metrics, Dot11TaggedParameters.ASSOCREQ_TAGGED_PARAMS_POSITION, payload);

        String destination = "";
        if(associationRequest.getHeader().getAddress1() != null) {
            destination = associationRequest.getHeader().getAddress1().toString();
        }

        String transmitter = "";
        if(associationRequest.getHeader().getAddress2() != null) {
            transmitter = associationRequest.getHeader().getAddress2().toString();
        }

        String ssid;
        try {
            ssid = taggedParameters.getSSID();
        } catch(Dot11TaggedParameters.NoSuchTaggedElementException e) {
            throw new IllegalRawDataException("No SSID in assoc-req frame. Not even empty SSID. This is a malformed frame.");
        }

        if (anonymizer.isEnabled()) {
            ssid = anonymizer.anonymizeSSID(ssid);
            transmitter = anonymizer.anonymizeBSSID(transmitter);
            destination = anonymizer.anonymizeBSSID(destination);
        }

        return Dot11AssociationRequestFrame.create(ssid, destination, transmitter, meta, payload, header);
    }

}
