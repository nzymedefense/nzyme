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

package horse.wtf.nzyme.dot11;

import horse.wtf.nzyme.channels.Frequencies;
import org.pcap4j.packet.*;

import java.util.ArrayList;

public class Dot11MetaInformation {

    private final boolean malformed;
    private final int antennaSignal;
    private final int frequency;
    private final int channel;
    private final long macTimestamp;
    private final boolean isWep;

    private Dot11MetaInformation(boolean malformed, int antennaSignal, int frequency, int channel, long macTimestamp, boolean isWep) {
        this.malformed = malformed;
        this.antennaSignal = antennaSignal;
        this.channel = channel;
        this.frequency = frequency;
        this.macTimestamp = macTimestamp;
        this.isWep = isWep;
    }

    public boolean isMalformed() {
        return malformed;
    }

    public int getAntennaSignal() {
        return antennaSignal;
    }

    public int getFrequency() {
        return frequency;
    }

    public long getMacTimestamp() {
        return macTimestamp;
    }

    public int getChannel() {
        return channel;
    }

    public boolean isWep() {
        return isWep;
    }

    public static Dot11MetaInformation parse(ArrayList<RadiotapPacket.RadiotapData> dataFields) {
        int antennaSignal = 0;
        int frequency = 0;
        int channel = -1;

        boolean delimiterCrcError = false;
        boolean badPlcpCrc = false;
        boolean badFcs = false;
        boolean isWep = false;
        long macTimestamp = -1;

        for (RadiotapPacket.RadiotapData f : dataFields) {
            if(f instanceof RadiotapDataAntennaSignal) {
                antennaSignal = ((RadiotapDataAntennaSignal) f).getAntennaSignalAsInt();
            } else if (f instanceof RadiotapDataChannel) {
                frequency = ((RadiotapDataChannel) f).getFrequencyAsInt();
                channel =  Frequencies.frequencyToChannel(frequency);
            } else if (f instanceof RadiotapDataAMpduStatus) {
                delimiterCrcError = ((RadiotapDataAMpduStatus) f).isDelimiterCrcError();
            } else if (f instanceof RadiotapDataRxFlags) {
                badPlcpCrc = ((RadiotapDataRxFlags) f).isBadPlcpCrc();
            } else if (f instanceof RadiotapDataFlags) {
                badFcs = ((RadiotapDataFlags) f).isBadFcs();
                isWep = ((RadiotapDataFlags) f).isWepEncrypted();
            } else if (f instanceof RadiotapDataTsft) {
                macTimestamp = ((RadiotapDataTsft) f).getMacTimestamp().longValue();
            }
        }

        return new Dot11MetaInformation( delimiterCrcError || badPlcpCrc || badFcs, antennaSignal, frequency, channel, macTimestamp, isWep);
    }

}
