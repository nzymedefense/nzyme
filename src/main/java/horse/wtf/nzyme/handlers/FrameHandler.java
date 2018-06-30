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

package horse.wtf.nzyme.handlers;

import horse.wtf.nzyme.probes.dot11.Dot11Probe;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import org.pcap4j.packet.IllegalRawDataException;

public abstract class FrameHandler {

    protected final Dot11Probe probe;

    protected FrameHandler(Dot11Probe probe) {
        this.probe = probe;
    }

    protected void tick() {
        probe.getStatistics().tickType(getName());
    }

    public void malformed(Dot11MetaInformation meta) {
        probe.getStatistics().tickMalformedCountAndNotify(probe, meta);
    }

    public abstract void handle(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException;
    public abstract String getName();

}
