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
import app.nzyme.core.dot11.MalformedFrameException;
import app.nzyme.core.dot11.anonymization.Anonymizer;
import app.nzyme.core.dot11.frames.Dot11AssociationResponseFrame;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.util.ByteArrays;

import java.nio.ByteOrder;

public class Dot11AssociationResponseFrameParser extends Dot11FrameParser<Dot11AssociationResponseFrame> {

    private final Anonymizer anonymizer;

    private static final int STATUS_CODE_POSITION = 26;
    private static final int STATUS_CODE_LENGTH = 2;

    public Dot11AssociationResponseFrameParser(MetricRegistry metrics, Anonymizer anonymizer) {
        super(metrics);

        this.anonymizer = anonymizer;
    }

    @Override
    protected Dot11AssociationResponseFrame doParse(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException, MalformedFrameException {
        Dot11ManagementFrame associationResponse = Dot11ManagementFrame.newPacket(payload, 0, payload.length);

        // Check bounds for response code field.
        try {
            ByteArrays.validateBounds(payload, 0, STATUS_CODE_POSITION+STATUS_CODE_LENGTH-1);
        } catch(Exception e) {
            throw new MalformedFrameException("Payload out of bounds. (1) Ignoring.");
        }

        // Parse the response code. 0 means success any other value means failure.
        short responseCode = ByteArrays.getShort(new byte[]{payload[26], payload[27]}, 0, ByteOrder.LITTLE_ENDIAN);

        if(responseCode < 0) {
            throw new MalformedFrameException("Invalid response code <" + responseCode + ">");
        }

        String response = "refused";
        if (responseCode == 0) {
            response = "success";
        }

        String destination = "";
        if(associationResponse.getHeader().getAddress1() != null) {
            destination = associationResponse.getHeader().getAddress1().toString();
        }

        String transmitter = "";
        if(associationResponse.getHeader().getAddress2() != null) {
            transmitter = associationResponse.getHeader().getAddress2().toString();
        }

        if (anonymizer.isEnabled()) {
            transmitter = anonymizer.anonymizeBSSID(transmitter);
            destination = anonymizer.anonymizeBSSID(destination);
        }

        return Dot11AssociationResponseFrame.create(transmitter, destination, response, responseCode, meta, payload, header);
    }

}
