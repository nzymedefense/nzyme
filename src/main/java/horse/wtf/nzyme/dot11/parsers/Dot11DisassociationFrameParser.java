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
import horse.wtf.nzyme.dot11.Dot11LeavingReason;
import horse.wtf.nzyme.dot11.Dot11ManagementFrame;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.frames.Dot11DisassociationFrame;
import org.pcap4j.packet.IllegalRawDataException;

public class Dot11DisassociationFrameParser extends Dot11FrameParser<Dot11DisassociationFrame> {

    private final Anonymizer anonymizer;

    public Dot11DisassociationFrameParser(MetricRegistry metrics, Anonymizer anonymizer) {
        super(metrics);

        this.anonymizer = anonymizer;
    }

    @Override
    protected Dot11DisassociationFrame doParse(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException {
        Dot11ManagementFrame disassociationRequest = Dot11ManagementFrame.newPacket(payload, 0, payload.length);

        String destination = "";
        if(disassociationRequest.getHeader().getAddress1() != null) {
            destination = disassociationRequest.getHeader().getAddress1().toString();
        }

        String transmitter = "";
        if(disassociationRequest.getHeader().getAddress2() != null) {
            transmitter = disassociationRequest.getHeader().getAddress2().toString();
        }

        // Reason.
        short reasonCode = Dot11LeavingReason.extract(payload, header);
        String reasonString = Dot11LeavingReason.lookup(reasonCode);

        if (anonymizer.isEnabled()) {
            transmitter = anonymizer.anonymizeBSSID(transmitter);
            destination = anonymizer.anonymizeBSSID(destination);
        }

        return Dot11DisassociationFrame.create(destination, transmitter, reasonCode, reasonString, meta);
    }

}
