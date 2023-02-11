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
import app.nzyme.core.dot11.Dot11LeavingReason;
import app.nzyme.core.dot11.Dot11ManagementFrame;
import app.nzyme.core.dot11.Dot11MetaInformation;
import app.nzyme.core.dot11.anonymization.Anonymizer;
import app.nzyme.core.dot11.frames.Dot11DeauthenticationFrame;
import org.pcap4j.packet.IllegalRawDataException;

public class Dot11DeauthenticationFrameParser extends Dot11FrameParser<Dot11DeauthenticationFrame> {

    private final Anonymizer anonymizer;

    public Dot11DeauthenticationFrameParser(MetricRegistry metrics, Anonymizer anonymizer) {
        super(metrics);

        this.anonymizer = anonymizer;
    }

    @Override
    protected Dot11DeauthenticationFrame doParse(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException {
        Dot11ManagementFrame deauth = Dot11ManagementFrame.newPacket(payload, 0, payload.length);

        String destination = "";
        if (deauth.getHeader().getAddress1() != null) {
            destination = deauth.getHeader().getAddress1().toString();
        }

        String transmitter = "";
        if (deauth.getHeader().getAddress2() != null) {
            transmitter = deauth.getHeader().getAddress2().toString();
        }

        String bssid = "";
        if (deauth.getHeader().getAddress3() != null) {
            bssid = deauth.getHeader().getAddress3().toString();
        }

        // Reason.
        short reasonCode = Dot11LeavingReason.extract(payload, header);
        String reasonString = Dot11LeavingReason.lookup(reasonCode);

        if (anonymizer.isEnabled()) {
            transmitter = anonymizer.anonymizeBSSID(transmitter);
            destination = anonymizer.anonymizeBSSID(destination);
        }

        return Dot11DeauthenticationFrame.create(destination, transmitter, bssid, reasonCode, reasonString, meta, payload, header);
    }

}
