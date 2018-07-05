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

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.dot11.Dot11ManagementFrame;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.Dot11SSID;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.frames.Dot11AssociationRequestFrame;
import org.pcap4j.packet.IllegalRawDataException;

public class Dot11AssociationRequestFrameParser extends Dot11FrameParser<Dot11AssociationRequestFrame> {

    private static final int SSID_LENGTH_POSITION = 29;
    private static final int SSID_POSITION = 30;

    public Dot11AssociationRequestFrameParser(MetricRegistry metrics) {
        super(metrics);
    }

    @Override
    protected Dot11AssociationRequestFrame doParse(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException, MalformedFrameException {
        Dot11ManagementFrame associationRequest = Dot11ManagementFrame.newPacket(payload, 0, payload.length);

        String destination = "";
        if(associationRequest.getHeader().getAddress1() != null) {
            destination = associationRequest.getHeader().getAddress1().toString();
        }

        String transmitter = "";
        if(associationRequest.getHeader().getAddress2() != null) {
            transmitter = associationRequest.getHeader().getAddress2().toString();
        }

        String ssid = Dot11SSID.extractSSID(SSID_LENGTH_POSITION, SSID_POSITION, payload);

        if (ssid == null) {
            ssid = "[no SSID]";
        }

        return Dot11AssociationRequestFrame.create(ssid, destination, transmitter, meta);
    }

}
