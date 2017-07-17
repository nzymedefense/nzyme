/*
 *  This file is part of Nzyme.
 *
 *  Nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.dot11;

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
