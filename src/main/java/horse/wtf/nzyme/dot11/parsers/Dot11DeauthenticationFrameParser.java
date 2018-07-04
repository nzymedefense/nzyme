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

import horse.wtf.nzyme.dot11.Dot11LeavingReason;
import horse.wtf.nzyme.dot11.Dot11ManagementFrame;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.frames.Dot11DeauthenticationFrame;
import org.pcap4j.packet.IllegalRawDataException;

public class Dot11DeauthenticationFrameParser extends Dot11FrameParser<Dot11DeauthenticationFrame> {

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

        return Dot11DeauthenticationFrame.create(destination, transmitter, bssid, reasonCode, reasonString, meta);
    }

}
