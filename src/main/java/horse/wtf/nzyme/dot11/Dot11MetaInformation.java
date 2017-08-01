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

import org.pcap4j.packet.*;

import java.math.BigInteger;
import java.util.ArrayList;

public class Dot11MetaInformation {

    private final boolean malformed;
    private final int antennaSignal;
    private final int frequency;
    private final long macTimestamp;

    private Dot11MetaInformation(boolean malformed, int antennaSignal, int frequency, long macTimestamp) {
        this.malformed = malformed;
        this.antennaSignal = antennaSignal;
        this.frequency = frequency;
        this.macTimestamp = macTimestamp;
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

    public static Dot11MetaInformation parse(ArrayList<RadiotapPacket.RadiotapData> dataFields) {
        int antennaSignal = 0;
        int frequency = 0;

        boolean delimiterCrcError = false;
        boolean badPlcpCrc = false;
        boolean badFcs = false;
        long macTimestamp = -1;

        int found = 0;
        for (RadiotapPacket.RadiotapData f : dataFields) {
            if(found == 5) {
                break;
            }

            if(f instanceof RadiotapDataAntennaSignal) {
                antennaSignal = ((RadiotapDataAntennaSignal) f).getAntennaSignalAsInt();
                found++;
            } else if (f instanceof RadiotapDataChannel) {
                frequency = ((RadiotapDataChannel) f).getFrequencyAsInt();
                found++;
            } else if (f instanceof RadiotapDataAMpduStatus) {
                delimiterCrcError = ((RadiotapDataAMpduStatus) f).isDelimiterCrcError();
                found++;
            } else if (f instanceof RadiotapDataRxFlags) {
                badPlcpCrc = ((RadiotapDataRxFlags) f).isBadPlcpCrc();
                found++;
            } else if (f instanceof RadiotapDataFlags) {
                badFcs = ((RadiotapDataFlags) f).isBadFcs();
                found++;
            } else if (f instanceof RadiotapDataTsft) {
                macTimestamp = ((RadiotapDataTsft) f).getMacTimestamp().longValue();
            }
        }

        return new Dot11MetaInformation( delimiterCrcError || badPlcpCrc || badFcs, antennaSignal, frequency, macTimestamp);
    }
}
