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

package app.nzyme.core.dot11;

import org.pcap4j.packet.Dot11ManagementPacket;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.util.ByteArrays;

public class Dot11ManagementFrame extends Dot11ManagementPacket {

    private final Dot11ManagementFrameHeader header;

    public static Dot11ManagementFrame newPacket(byte[] rawData, int offset, int length) throws IllegalRawDataException {
        ByteArrays.validateBounds(rawData, offset, length);
        Dot11ManagementFrame.Dot11ManagementFrameHeader h = new Dot11ManagementFrame.Dot11ManagementFrameHeader(rawData, offset, length);
        return new Dot11ManagementFrame(rawData, offset, length, h);
    }

    private Dot11ManagementFrame(byte[] rawData, int offset, int length, Dot11ManagementFrame.Dot11ManagementFrameHeader header) {
        super(rawData, offset, length, header.length());
        this.header = header;
    }

    @Override
    public Dot11ManagementFrame.Dot11ManagementFrameHeader getHeader() {
        return header;
    }

    @Override
    public Dot11ManagementFrame.Builder getBuilder() {
        return null;
    }

    public static final class Dot11ManagementFrameHeader extends Dot11ManagementPacket.Dot11ManagementHeader {

        private Dot11ManagementFrameHeader(byte[] rawData, int offset, int length) throws IllegalRawDataException {
            super(rawData, offset, length);
        }

        @Override
        protected String getHeaderName() {
            return "IEEE802.11 management header";
        }
    }

}
