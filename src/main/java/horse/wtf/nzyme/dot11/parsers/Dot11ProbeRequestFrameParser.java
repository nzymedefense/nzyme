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
import com.google.common.base.Strings;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeRequestFrame;
import horse.wtf.nzyme.util.Tools;
import org.pcap4j.packet.Dot11ProbeRequestPacket;
import org.pcap4j.packet.IllegalRawDataException;

import java.text.Normalizer;

public class Dot11ProbeRequestFrameParser extends Dot11FrameParser<Dot11ProbeRequestFrame> {

    private final Anonymizer anonymizer;

    public Dot11ProbeRequestFrameParser(MetricRegistry metrics, Anonymizer anonymizer) {
        super(metrics);

        this.anonymizer = anonymizer;
    }

    @Override
    protected Dot11ProbeRequestFrame doParse(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException, MalformedFrameException {
        Dot11ProbeRequestPacket probeRequest = Dot11ProbeRequestPacket.newPacket(payload, 0, payload.length);

        if (probeRequest.getHeader() == null) {
            throw new MalformedFrameException("Malformed header in probe request packet. Skipping.");
        }

        String ssid;
        boolean broadcastProbe = false;
        if (probeRequest.getHeader().getSsid() != null) {
            ssid = probeRequest.getHeader().getSsid().getSsid();

            if (Strings.isNullOrEmpty(ssid)) {
                ssid = null;
                broadcastProbe = true;
            }
        } else {
            ssid = null;
            broadcastProbe = true;
        }

        String requester;
        if (probeRequest.getHeader().getAddress2() != null) {
            requester = probeRequest.getHeader().getAddress2().toString();
        } else {
            throw new MalformedFrameException("Missing requester address.");
        }

        if (anonymizer.isEnabled()) {
            ssid = anonymizer.anonymizeSSID(ssid);
            requester = anonymizer.anonymizeBSSID(requester);
        }

        return Dot11ProbeRequestFrame.create(requester, ssid, broadcastProbe, meta);
    }

}
