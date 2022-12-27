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

package app.nzyme.core.dot11.frames;

import com.codahale.metrics.MetricRegistry;
import app.nzyme.core.dot11.Dot11FrameSubtype;
import app.nzyme.core.dot11.Dot11MetaInformation;
import app.nzyme.core.dot11.MalformedFrameException;
import app.nzyme.core.dot11.anonymization.Anonymizer;
import app.nzyme.core.dot11.parsers.*;
import app.nzyme.core.remote.protobuf.NzymeMessage;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.namednumber.Dot11FrameType;

public class Dot11FrameFactory {

    private final Dot11BeaconFrameParser beaconParser;
    private final Dot11AssociationRequestFrameParser associationRequestParser;
    private final Dot11AssociationResponseFrameParser associationResponseParser;
    private final Dot11ProbeRequestFrameParser probeRequestParser;
    private final Dot11ProbeResponseFrameParser probeResponseFrameParser;
    private final Dot11DisassociationFrameParser disassociationParser;
    private final Dot11AuthenticationFrameParser authenticationFrameParser;
    private final Dot11DeauthenticationFrameParser deauthenticationFrameParser;

    public Dot11FrameFactory(MetricRegistry metrics, Anonymizer anonymizer) {
        beaconParser = new Dot11BeaconFrameParser(metrics, anonymizer);
        associationRequestParser = new Dot11AssociationRequestFrameParser(metrics, anonymizer);
        associationResponseParser = new Dot11AssociationResponseFrameParser(metrics, anonymizer);
        probeRequestParser = new Dot11ProbeRequestFrameParser(metrics, anonymizer);
        probeResponseFrameParser = new Dot11ProbeResponseFrameParser(metrics, anonymizer);
        disassociationParser = new Dot11DisassociationFrameParser(metrics, anonymizer);
        authenticationFrameParser = new Dot11AuthenticationFrameParser(metrics, anonymizer);
        deauthenticationFrameParser = new Dot11DeauthenticationFrameParser(metrics, anonymizer);
    }

    public Dot11Frame build(Dot11FrameType type, byte[] payload, byte[] header, Dot11MetaInformation meta) throws MalformedFrameException, IllegalRawDataException {
        switch(type.value()) {
            case Dot11FrameSubtype.ASSOCIATION_REQUEST:
                return associationRequestParser.parse(payload, header, meta);
            case Dot11FrameSubtype.ASSOCIATION_RESPONSE:
                return associationResponseParser.parse(payload, header, meta);
            case Dot11FrameSubtype.PROBE_REQUEST:
                return probeRequestParser.parse(payload, header, meta);
            case Dot11FrameSubtype.PROBE_RESPONSE:
                return probeResponseFrameParser.parse(payload, header, meta);
            case Dot11FrameSubtype.BEACON:
                return beaconParser.parse(payload, header, meta);
            case Dot11FrameSubtype.DISASSOCIATION:
                return disassociationParser.parse(payload, header, meta);
            case Dot11FrameSubtype.AUTHENTICATION:
                return authenticationFrameParser.parse(payload, header, meta);
            case Dot11FrameSubtype.DEAUTHENTICATION:
                return deauthenticationFrameParser.parse(payload, header, meta);
            default:
                throw new IllegalStateException("Unexpected value: " + type.value());
        }
    }

    public Dot11Frame fromRemote(NzymeMessage.Dot11Frame frame) throws MalformedFrameException, IllegalRawDataException {
        Dot11FrameType type;
        switch (frame.getFrameType()) {
            case "horse.wtf.nzyme.dot11.frames.AutoValue_Dot11AssociationRequestFrame":
                type = Dot11FrameType.ASSOCIATION_REQUEST;
                break;
            case "horse.wtf.nzyme.dot11.frames.AutoValue_Dot11AssociationResponseFrame":
                type = Dot11FrameType.ASSOCIATION_RESPONSE;
                break;
            case "horse.wtf.nzyme.dot11.frames.AutoValue_Dot11AuthenticationFrame":
                type = Dot11FrameType.AUTHENTICATION;
                break;
            case "horse.wtf.nzyme.dot11.frames.AutoValue_Dot11BeaconFrame":
                type = Dot11FrameType.BEACON;
                break;
            case "horse.wtf.nzyme.dot11.frames.AutoValue_Dot11DeauthenticationFrame":
                type = Dot11FrameType.DEAUTHENTICATION;
                break;
            case "horse.wtf.nzyme.dot11.frames.AutoValue_Dot11DisassociationFrame":
                type = Dot11FrameType.DISASSOCIATION;
                break;
            case "horse.wtf.nzyme.dot11.frames.AutoValue_Dot11ProbeRequestFrame":
                type = Dot11FrameType.PROBE_REQUEST;
                break;
            case "horse.wtf.nzyme.dot11.frames.AutoValue_Dot11ProbeResponseFrame":
                type = Dot11FrameType.PROBE_RESPONSE;
                break;
            default:
                throw new RuntimeException("Unknown frame type [" + frame.getFrameType() + "].");
        }

        NzymeMessage.FrameMeta meta = frame.getFrameMeta();

        return build(
                type,
                frame.getFramePayload().toByteArray(),
                frame.getFrameHeader().toByteArray(),
                new Dot11MetaInformation(
                        meta.getIsMalformed(),
                        meta.getAntennaSignal(),
                        meta.getFrequency(),
                        meta.getChannel(),
                        meta.getMacTimestamp(),
                        meta.getIsWEP()
                )
        );
    }
}
