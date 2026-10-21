package app.nzyme.core.ethernet.beacons;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntervalAnalysis {

    @AutoValue
    public abstract static class Result {

        // Timestamps after collapsing bursts of parallel connections into one check-in each. Sorted ascending.
        public abstract List<DateTime> checkins();

        public abstract int checkinCount();

        // Median of the intervals between check-ins. Zero if there are less than two check-ins.
        public abstract double intervalMedianSeconds();

        // Mean of the intervals between check-ins. Zero if there are less than two check-ins.
        public abstract double intervalMeanSeconds();

        // Median absolute deviation of the intervals, relative to the median interval. (0.1 = 10% jitter)
        public abstract double jitter();

        // Fraction of intervals that fall within the tolerance band around the median interval. (0.0-1.0)
        public abstract double regularity();

        public abstract DateTime firstSeen();

        public abstract DateTime lastSeen();

        public static Result create(List<DateTime> checkins, int checkinCount, double intervalMedianSeconds, double intervalMeanSeconds, double jitter, double regularity, DateTime firstSeen, DateTime lastSeen) {
            return builder()
                    .checkins(checkins)
                    .checkinCount(checkinCount)
                    .intervalMedianSeconds(intervalMedianSeconds)
                    .intervalMeanSeconds(intervalMeanSeconds)
                    .jitter(jitter)
                    .regularity(regularity)
                    .firstSeen(firstSeen)
                    .lastSeen(lastSeen)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_IntervalAnalysis_Result.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder checkins(List<DateTime> checkins);

            public abstract Builder checkinCount(int checkinCount);

            public abstract Builder intervalMedianSeconds(double intervalMedianSeconds);

            public abstract Builder intervalMeanSeconds(double intervalMeanSeconds);

            public abstract Builder jitter(double jitter);

            public abstract Builder regularity(double regularity);

            public abstract Builder firstSeen(DateTime firstSeen);

            public abstract Builder lastSeen(DateTime lastSeen);

            public abstract Result build();
        }
    }

    /*
     * Collapses timestamps that are closer together than the collapse window into a single check-in. A browser
     * opening six parallel connections to the same host is one check-in, not six.
     */
    public static List<DateTime> collapse(List<DateTime> timestamps, Duration collapseWindow) {
        List<DateTime> sorted = new ArrayList<>(timestamps);
        Collections.sort(sorted);

        List<DateTime> collapsed = Lists.newArrayList();
        DateTime last = null;
        for (DateTime ts : sorted) {
            if (last == null || new Duration(last, ts).isLongerThan(collapseWindow)) {
                collapsed.add(ts);
                last = ts;
            }
        }

        return collapsed;
    }

    public static Result analyze(List<DateTime> timestamps, Duration collapseWindow, double tolerance) {
        if (timestamps.isEmpty()) {
            throw new IllegalArgumentException("Cannot analyze empty list of timestamps.");
        }

        List<DateTime> checkins = collapse(timestamps, collapseWindow);

        if (checkins.size() < 2) {
            return Result.create(checkins, checkins.size(), 0, 0, 0, 0, checkins.get(0), checkins.get(0));
        }

        List<Double> intervals = Lists.newArrayList();
        for (int i = 1; i < checkins.size(); i++) {
            intervals.add(new Duration(checkins.get(i - 1), checkins.get(i)).getMillis() / 1000.0);
        }

        double median = median(intervals);
        double mean = intervals.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        List<Double> deviations = Lists.newArrayList();
        for (double interval : intervals) {
            deviations.add(Math.abs(interval - median));
        }
        double jitter = median == 0 ? 0 : median(deviations) / median;

        double lowerBound = median * (1 - tolerance);
        double upperBound = median * (1 + tolerance);
        long withinBand = intervals.stream().filter(i -> i >= lowerBound && i <= upperBound).count();
        double regularity = (double) withinBand / intervals.size();

        return Result.create(
                checkins,
                checkins.size(),
                median,
                mean,
                jitter,
                regularity,
                checkins.get(0),
                checkins.get(checkins.size() - 1)
        );
    }


    public static boolean isBeacon(Result result,
                                   int minimumCheckins,
                                   double minimumIntervalSeconds,
                                   double minimumRegularity) {
        // At least two check-ins are needed for a single interval, regardless of configuration.
        return result.checkinCount() >= Math.max(2, minimumCheckins)
                && result.intervalMedianSeconds() >= minimumIntervalSeconds
                && result.regularity() >= minimumRegularity;
    }

    public static double median(List<Double> values) {
        if (values.isEmpty()) {
            return 0;
        }

        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);

        int middle = sorted.size() / 2;
        if (sorted.size() % 2 == 0) {
            return (sorted.get(middle - 1) + sorted.get(middle)) / 2.0;
        } else {
            return sorted.get(middle);
        }
    }

}
