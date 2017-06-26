package horse.wtf.nzyme.dot11;

import org.pcap4j.packet.*;

import java.util.ArrayList;

public class Dot11MetaInformation {

    private final boolean malformed;
    private final int antennaSignal;
    private final int frequency;

    private Dot11MetaInformation(boolean malformed, int antennaSignal, int frequency) {
        this.malformed = malformed;
        this.antennaSignal = antennaSignal;
        this.frequency = frequency;
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

    public static Dot11MetaInformation parse(ArrayList<RadiotapPacket.RadiotapData> dataFields) {
        int antennaSignal = 0;
        int frequency = 0;

        boolean delimiterCrcError = false;
        boolean badPlcpCrc = false;
        boolean badFcs = false;

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
            }
        }

        return new Dot11MetaInformation( delimiterCrcError || badPlcpCrc || badFcs, antennaSignal, frequency);
    }
}
