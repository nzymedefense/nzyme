/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package horse.wtf.nzyme.dot11;

import horse.wtf.nzyme.channels.Frequencies;
import org.pcap4j.packet.*;

import java.util.ArrayList;

import static horse.wtf.nzyme.util.Tools.calculateSignalQuality;

public class Dot11MetaInformation {

    private final boolean malformed;
    private final int antennaSignal;
    private final int signalQuality;
    private final int frequency;
    private final int channel;
    private final long macTimestamp;
    private final boolean isWep;

    public Dot11MetaInformation(boolean malformed, int antennaSignal, int frequency, int channel, long macTimestamp, boolean isWep) {
        this.malformed = malformed;
        this.antennaSignal = antennaSignal;
        this.signalQuality = calculateSignalQuality(antennaSignal);
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

    public int getSignalQuality() {
        return signalQuality;
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

        boolean antennaRead = false;
        for (RadiotapPacket.RadiotapData f : dataFields) {
            if(f instanceof RadiotapDataAntennaSignal) {
                if (antennaRead) {
                    /*
                     * In some circumstances, RadioTap might report multiple antenna signal fields, with one of them being wildly
                     * off. Only read the first one.
                     *
                     * I assume this might be related to adapters with multiple antennas or simply a faulty driver.
                     *
                     * Thanks to @mathieubrun for reporting this: https://github.com/lennartkoopmann/nzyme/issues/459
                     */

                    continue;
                }

                antennaSignal = ((RadiotapDataAntennaSignal) f).getAntennaSignalAsInt();
                antennaRead = true;
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
