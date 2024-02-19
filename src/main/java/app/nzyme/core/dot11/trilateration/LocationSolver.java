package app.nzyme.core.dot11.trilateration;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.TapBasedSignalStrengthResult;
import app.nzyme.core.dot11.db.TapBasedSignalStrengthResultHistogramEntry;
import app.nzyme.core.taps.Tap;
import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.*;

public class LocationSolver {

    private final NzymeNode nzyme;

    public LocationSolver(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public TrilaterationResult solve(List<TapBasedSignalStrengthResultHistogramEntry> signals, int minutes) throws InvalidTapsException {
        // Sort the signal data into a queryable histogram.
        Map<DateTime, List<TapBasedSignalStrengthResultHistogramEntry>> histo = Maps.newHashMap();
        for (TapBasedSignalStrengthResultHistogramEntry signal : signals) {
            List<TapBasedSignalStrengthResultHistogramEntry> entry = histo.get(signal.bucket());

            if (entry != null) {
                entry.add(signal);
            } else {
                histo.put(signal.bucket(), new ArrayList<>(){{ add(signal); }});
            }
        }

        // Cache taps.
        Map<UUID, Tap> taps = Maps.newHashMap();
        for (Tap tap : nzyme.getTapManager().findAllTapsOfAllUsers()) {
            taps.put(tap.uuid(), tap);
        }

        Map<DateTime, TrilaterationLocation> result = new TreeMap<>();
        for (int x = minutes; x != 0; x--) {
            DateTime bucket = DateTime.now().withSecondOfMinute(0).withMillisOfSecond(0).minusMinutes(x);

            List<TapBasedSignalStrengthResultHistogramEntry> signal = histo.get(bucket);

            if (signal != null) {
                List<Double[]> positions = Lists.newArrayList();
                List<Double> distances = Lists.newArrayList();

                for (TapBasedSignalStrengthResultHistogramEntry s : signal) {
                    Tap tap = taps.get(s.tapUuid());

                    if (tap == null) {
                        continue;
                    }

                    if (tap.x() == null || tap.y() == null) {
                        continue;
                    }

                    positions.add(new Double[]{(double) tap.x(), (double) tap.y()});

                    double distance = Math.pow(10, (double)(-40 - (s.signalStrength())) / (10 * 3));
                    distances.add(distance);
                }

                if (positions.size() < 3 || positions.size() != distances.size()) {
                    // The signal loop above didn't find all taps or didn't have all signals. Skip this minute.
                    continue;
                }

                double[][] positionsArr = new double[positions.size()][];
                int i = 0;
                for (Double[] value : positions) {
                    positionsArr[i] = new double[]{value[0], value[1]};
                    i++;
                }

                NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(
                        new TrilaterationFunction(positionsArr, Doubles.toArray(distances)),
                        new LevenbergMarquardtOptimizer()
                );

                LeastSquaresOptimizer.Optimum optimum = solver.solve();
                double[] position = optimum.getPoint().toArray();

                result.put(bucket, TrilaterationLocation.create((int) position[0], (int) position[1]));
            }
        }

        return TrilaterationResult.create(result);
    }

    @AutoValue
    public abstract static class TrilaterationResult {

        public abstract Map<DateTime, TrilaterationLocation> locations();

        public static TrilaterationResult create(Map<DateTime, TrilaterationLocation> locations) {
            return builder()
                    .locations(locations)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_LocationSolver_TrilaterationResult.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder locations(Map<DateTime, TrilaterationLocation> locations);

            public abstract TrilaterationResult build();
        }
    }

    @AutoValue
    public abstract static class TrilaterationLocation {

        public abstract int x();
        public abstract int y();

        public static TrilaterationLocation create(int x, int y) {
            return builder()
                    .x(x)
                    .y(y)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_LocationSolver_TrilaterationLocation.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder x(int x);

            public abstract Builder y(int y);

            public abstract TrilaterationLocation build();
        }
    }

    public static class InvalidTapsException extends Throwable {

        public InvalidTapsException(String msg) {
            super(msg);
        }

    }

}
