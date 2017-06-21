package horse.wtf.nzyme;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class StatisticsPrinter {

    private final Statistics statistics;

    public StatisticsPrinter(Statistics statistics) {
        this.statistics = statistics;
    }

    // TODO print info about malformed/rejected frames
    public String print() {
        StringBuilder sb = new StringBuilder();

        sb.append("Frames: ").append(statistics.getFrameCount())
                .append(" (").append(statistics.getMalformedCount()).append(" malformed)");

        for (Map.Entry<String, AtomicLong> type : statistics.getTypes().entrySet()) {
            sb.append(", ").append(type.getKey()).append(": ").append(type.getValue().get());
        }

        return sb.toString();
    }

}
