package app.nzyme.core.dot11.trilateration;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.TapBasedSignalStrengthResult;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class LocationSolver {

    private static final Logger LOG = LogManager.getLogger(LocationSolver.class);

    private final NzymeNode nzyme;

    public LocationSolver(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public TrilaterationResult solve(List<TapBasedSignalStrengthResult> taps) throws InvalidTapsException {
        List<Double[]> positions = Lists.newArrayList();
        List<Double> distances = Lists.newArrayList();

        Map<UUID, Double> debugTapDistances = Maps.newHashMap();

        for (TapBasedSignalStrengthResult s : taps) {
            Optional<Tap> tapResult = nzyme.getTapManager().findTap(s.tapUuid());

            if (tapResult.isEmpty()) {
                throw new InvalidTapsException("Tap [" + s.tapUuid() + "] not found.");
            }

            Tap tap = tapResult.get();

            if (tap.x() == null || tap.y() == null) {
                throw new InvalidTapsException("Tap [" + tap.name() + "/" + tap.uuid() + "] not placed on any " +
                        "location/floor.");
            }

            positions.add(new Double[]{(double) tap.x(), (double) tap.y()});

            double distance = Math.pow(10, (double)(-40 - (s.signalStrength())) / (10 * 3));
            distances.add(distance);
            debugTapDistances.put(tap.uuid(), distance);
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

        return TrilaterationResult.create((int) position[0], (int) position[1], debugTapDistances);
    }

    @AutoValue
    public abstract static class TrilaterationResult {

        public abstract int x();
        public abstract int y();

        public abstract Map<UUID, Double> tapDistances();

        public static TrilaterationResult create(int x, int y, Map<UUID, Double> tapDistances) {
            return builder()
                    .x(x)
                    .y(y)
                    .tapDistances(tapDistances)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_LocationSolver_TrilaterationResult.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder x(int x);

            public abstract Builder y(int y);

            public abstract Builder tapDistances(Map<UUID, Double> tapDistances);

            public abstract TrilaterationResult build();
        }
    }

    public static class InvalidTapsException extends Throwable {

        public InvalidTapsException(String msg) {
            super(msg);
        }

    }

}
