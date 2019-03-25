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
import horse.wtf.nzyme.dot11.*;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import org.pcap4j.packet.IllegalRawDataException;


public class Dot11BeaconFrameParser extends Dot11FrameParser<Dot11BeaconFrame> {

    public Dot11BeaconFrameParser(MetricRegistry metrics) {
        super(metrics);
    }

    @Override
    protected Dot11BeaconFrame doParse(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException, MalformedFrameException {
        Dot11ManagementFrame beacon = Dot11ManagementFrame.newPacket(payload, 0, payload.length);
        Dot11TaggedParameters taggedParameters = new Dot11TaggedParameters(Dot11TaggedParameters.BEACON_TAGGED_PARAMS_POSITION, payload);

        String transmitter = "";
        if(beacon.getHeader().getAddress2() != null) {
            transmitter = beacon.getHeader().getAddress2().toString();
        }

        String ssid;
        try {
            ssid = taggedParameters.getSSID();
        } catch(Dot11TaggedParameters.NoSuchTaggedElementException e) {
            throw new IllegalRawDataException("No SSID in beacon frame. Not even empty SSID. This is a malformed frame.");
        }

        return Dot11BeaconFrame.create(ssid, transmitter, meta);
    }

}
