/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.dot11.parsers;

import horse.wtf.nzyme.dot11.Dot11ManagementFrame;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.Dot11SSID;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import org.pcap4j.packet.IllegalRawDataException;

public class Dot11ProbeResponseFrameParser extends Dot11FrameParser<Dot11ProbeResponseFrame> {

    public static final int SSID_LENGTH_POSITION = 37;
    public static final int SSID_POSITION = 38;

    @Override
    protected Dot11ProbeResponseFrame doParse(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException, MalformedFrameException {
        Dot11ManagementFrame probeReponse = Dot11ManagementFrame.newPacket(payload, 0, payload.length);

        String ssid;
        try {
            ssid = Dot11SSID.extractSSID(SSID_LENGTH_POSITION, SSID_POSITION, payload);
        } catch (MalformedFrameException e) {
            throw new MalformedFrameException("Skipping malformed probe-resp frame.");
        }

        if (ssid == null) {
            ssid = "[no SSID]";
        }

        String destination = "";
        if (probeReponse.getHeader().getAddress1() != null) {
            destination = probeReponse.getHeader().getAddress1().toString();
        }

        String transmitter = "";
        if (probeReponse.getHeader().getAddress2() != null) {
            transmitter = probeReponse.getHeader().getAddress2().toString();
        }

        return Dot11ProbeResponseFrame.create(ssid, destination, transmitter, meta);
    }

}
