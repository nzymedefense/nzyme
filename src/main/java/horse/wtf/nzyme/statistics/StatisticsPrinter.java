package horse.wtf.nzyme.statistics;

import horse.wtf.nzyme.Nzyme;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class StatisticsPrinter {

    private final DecimalFormat df;

    private final Statistics statistics;

    public StatisticsPrinter(Statistics statistics) {
        this.statistics = statistics;

        this.df = new DecimalFormat();
    }

    public String print() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n+++++ Statistics: +++++");
        sb.append("\n");
        sb.append("Total frames considered: ").append(df.format(statistics.getFrameCount()))
                .append(" (").append(df.format(statistics.getMalformedCount())).append(" malformed)");

        for (Map.Entry<String, AtomicLong> type : statistics.getFrameTypes().entrySet()) {
            sb.append(", ").append(type.getKey()).append(": ").append(df.format(type.getValue().get()));
        }

        sb.append("\n");
        sb.append("Probing devices:         ").append(df.format(statistics.getProbingDevices().size()))
                .append(" (last ").append(Nzyme.STATS_INTERVAL).append(")");
        sb.append("\n");
        sb.append("Access points:           ").append(df.format(statistics.getAccessPoints().size()))
                .append(" (last ").append(Nzyme.STATS_INTERVAL).append(")");
        sb.append("\n");
        sb.append("Beaconing networks:      ").append(df.format(statistics.getBeaconedNetworks().size()))
                .append(" (last ").append(Nzyme.STATS_INTERVAL).append(")");

        return sb.toString();
    }

}
