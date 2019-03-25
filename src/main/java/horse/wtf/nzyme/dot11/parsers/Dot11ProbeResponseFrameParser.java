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
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import org.pcap4j.packet.IllegalRawDataException;

public class Dot11ProbeResponseFrameParser extends Dot11FrameParser<Dot11ProbeResponseFrame> {

    public Dot11ProbeResponseFrameParser(MetricRegistry metrics) {
        super(metrics);
    }

    @Override
    protected Dot11ProbeResponseFrame doParse(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException, MalformedFrameException {
        Dot11ManagementFrame probeReponse = Dot11ManagementFrame.newPacket(payload, 0, payload.length);
        Dot11TaggedParameters taggedParameters = new Dot11TaggedParameters(Dot11TaggedParameters.PROBERESP_TAGGED_PARAMS_POSITION, payload);

        String ssid;
        try {
            ssid = taggedParameters.getSSID();
        } catch(Dot11TaggedParameters.NoSuchTaggedElementException e) {
            throw new IllegalRawDataException("No SSID in probe-resp frame. Not even empty SSID. This is a malformed frame.");
        }

        String destination = "";
        if (probeReponse.getHeader().getAddress1() != null) {
            destination = probeReponse.getHeader().getAddress1().toString();
        }

        String transmitter = "";
        if (probeReponse.getHeader().getAddress2() != null) {
            transmitter = probeReponse.getHeader().getAddress2().toString();
        }

        return Dot11ProbeResponseFrame.create(ssid, destination, transmitter, taggedParameters, meta);
    }

}
