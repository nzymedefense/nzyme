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

package horse.wtf.nzyme.statistics;

import horse.wtf.nzyme.Nzyme;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class StatisticsPrinter {

    private final DecimalFormat df;
    private final DecimalFormat percentDf;

    private final Statistics statistics;
    private final Nzyme nzyme;

    public StatisticsPrinter(Nzyme nzyme) {
        this.statistics = nzyme.getStatistics();
        this.nzyme = nzyme;

        this.df = new DecimalFormat();
        this.percentDf = new DecimalFormat("#,##0.00'%'");
    }

    public String print() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n+++++ Statistics: +++++");
        sb.append("\n");
        sb.append("Total frames considered:           ").append(df.format(statistics.getFrameCount()))
                .append(" (").append(df.format(statistics.getMalformedCount())).append(" malformed)");

        for (Map.Entry<String, AtomicLong> type : statistics.getFrameTypes().entrySet()) {
            sb.append(", ").append(type.getKey()).append(": ").append(df.format(type.getValue().get()));
        }

        sb.append("\n");
        sb.append("Frames per channel:                ");
        sb.append(printChannelStatistics(statistics.getChannelCounts()));

        sb.append("\n");
        sb.append("Malformed Frames per channel:      ");
        sb.append(printMalformedStatistics(statistics.getChannelCounts(), statistics.getChannelMalformedCounts()));

        sb.append("\n");
        sb.append("Probing devices:                   ").append(df.format(statistics.getProbingDevices().size()))
                .append(" (last ").append(nzyme.getStatsInterval()).append("s)");
        sb.append("\n");
        sb.append("Access points:                     ").append(df.format(statistics.getAccessPoints().size()))
                .append(" (last ").append(nzyme.getStatsInterval()).append("s)");
        sb.append("\n");
        sb.append("Beaconing networks:                ").append(df.format(statistics.getBeaconedNetworks().size()))
                .append(" (last ").append(nzyme.getStatsInterval()).append("s)");



        return sb.toString();
    }

    private String printChannelStatistics(Map<Integer, AtomicLong> channels) {
        StringBuilder sb = new StringBuilder();

        int i = 0;
        for (Map.Entry<Integer, AtomicLong> channel : channels.entrySet()) {
            sb.append(channel.getKey()).append(": ").append(df.format(channel.getValue()));

            if(i+1 != channels.size()) {
                sb.append(", ");
            }

            i++;
        }

        return sb.toString();
    }

    private String printMalformedStatistics(Map<Integer, AtomicLong> channels, Map<Integer, AtomicLong> malformed) {
        StringBuilder sb = new StringBuilder();

        int i = 0;
        for (Map.Entry<Integer, AtomicLong> channel : malformed.entrySet()) {
            double malformedPercentage;

            // Calculate percentage of malformed frames on this channel.
            if(channels.containsKey(channel.getKey())) {
                long totalCount = channels.get(channel.getKey()).get();
                if(totalCount == 0) {
                    malformedPercentage = -1.0;
                } else {
                    malformedPercentage = (channel.getValue().get() / ((double) totalCount)) * 100;
                }
            } else {
                malformedPercentage = -1.0;
            }

            sb.append(channel.getKey()).append(": ").append(percentDf.format(malformedPercentage))
                    .append(" (").append(df.format(channel.getValue())).append(")");

            if(i+1 != channels.size()) {
                sb.append(", ");
            }

            i++;
        }

        return sb.toString();
    }

}
