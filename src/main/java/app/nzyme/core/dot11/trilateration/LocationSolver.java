package app.nzyme.core.dot11.trilateration;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.TapBasedSignalStrengthResultHistogramEntry;
import app.nzyme.core.floorplans.db.TenantLocationFloorEntry;
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
import org.joda.time.Duration;

import java.util.*;

public class LocationSolver {

    private static final Logger LOG = LogManager.getLogger(LocationSolver.class);

    private final NzymeNode nzyme;

    public LocationSolver(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public TrilaterationResult solve(List<TapBasedSignalStrengthResultHistogramEntry> signals,
                                     TenantLocationFloorEntry floor)
            throws InvalidTapsException {

        if (floor.planLengthPixels() == null || floor.planWidthPixels() == null
                || floor.planLengthMeters() == null || floor.planWidthMeters() == null) {
            throw new RuntimeException("Cannot run location solver on incomplete floor configuration.");
        }

        // Sort the signal data into a queryable histogram.
        Map<DateTime, List<TapBasedSignalStrengthResultHistogramEntry>> histo = Maps.newHashMap();
        DateTime earliest = DateTime.now();
        DateTime latest = DateTime.now();
        for (TapBasedSignalStrengthResultHistogramEntry signal : signals) {
            List<TapBasedSignalStrengthResultHistogramEntry> entry = histo.get(signal.bucket());

            if (entry != null) {
                entry.add(signal);
            } else {
                histo.put(signal.bucket(), new ArrayList<>(){{ add(signal); }});
            }

            if (signal.bucket().isAfter(latest)) {
                latest = signal.bucket();
            }

            if (signal.bucket().isBefore(earliest)) {
                earliest = signal.bucket();
            }
        }

        // Cache taps.
        Map<UUID, Tap> taps = Maps.newHashMap();
        for (Tap tap : nzyme.getTapManager().findAllTapsOfAllUsers()) {
            taps.put(tap.uuid(), tap);
        }

        int totalDataPoints = 0;
        int distancesOutsideOfBoundaries = 0;

        Duration duration = new Duration(earliest, latest);
        Map<DateTime, TrilaterationLocation> result = new TreeMap<>();
        for (int x = (int) duration.getStandardMinutes(); x != 0; x--) {
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

                    double distance = calculateDistance(-15, s.signalStrength(), floor.pathLossExponent());

                    totalDataPoints++;
                    if(isDistanceOutsideOfBoundaries(
                            distance,
                            tap.x(),
                            tap.y(),
                            floor.planWidthMeters(),
                            floor.planLengthMeters(),
                            floor.planWidthPixels(),
                            floor.planLengthPixels())) {
                        distancesOutsideOfBoundaries++;
                        continue;
                    }

                    positions.add(new Double[]{(double) tap.x(), (double) tap.y()});
                    distances.add(distance);
                }

                if (positions.size() < 3 || positions.size() != distances.size()) {
                    // All signals outside of floor plan boundaries.
                    if (distancesOutsideOfBoundaries == signal.size()) {
                        continue;
                    }

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

        LOG.info("{}/{} - {}%", distancesOutsideOfBoundaries, totalDataPoints, distancesOutsideOfBoundaries*100.0/totalDataPoints);

        return TrilaterationResult.create(result);
    }

    private boolean isDistanceOutsideOfBoundaries(double distanceMeters, int tapXPixel, int tapYPixel, int floorPlanWidthMeters, int floorPlanLengthMeters, int floorPlanWidthPixels, int floorPlanLengthPixels) {
        // Calculate scale factors.
        double scaleX = (double) floorPlanWidthMeters / floorPlanWidthPixels;
        double scaleY = (double) floorPlanLengthMeters / floorPlanLengthPixels;

        // Calculate distances in pixels to each corner.
        double distanceToTopLeft = calculateFloorPlanDistance(tapXPixel, tapYPixel, 0, 0);
        double distanceToTopRight = calculateFloorPlanDistance(tapXPixel, tapYPixel, floorPlanWidthPixels, 0);
        double distanceToBottomLeft = calculateFloorPlanDistance(tapXPixel, tapYPixel, 0, floorPlanLengthPixels);
        double distanceToBottomRight = calculateFloorPlanDistance(tapXPixel, tapYPixel, floorPlanWidthPixels, floorPlanLengthPixels);

        // Convert pixel distances to meters.
        double[] distancesToCorners = new double[4];
        distancesToCorners[0] = distanceToTopLeft * scaleX;
        distancesToCorners[1] = distanceToTopRight * Math.sqrt(scaleX * scaleX + scaleY * scaleY);
        distancesToCorners[2] = distanceToBottomLeft * Math.sqrt(scaleX * scaleX + scaleY * scaleY);
        distancesToCorners[3] = distanceToBottomRight * scaleX;

        // Find the maximum distance to any corner.
        double maxDistanceToCorner = 0;
        for (double dist : distancesToCorners) {
            if (dist > maxDistanceToCorner) {
                maxDistanceToCorner = dist;
            }
        }

        // If the estimated distance to the signal source is greater than the maximum distance to any corner, it's likely outside
        return distanceMeters > maxDistanceToCorner;
    }

    private static double calculateFloorPlanDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private double calculateDistance(double RSSI0, double RSSI, double n) {
        return Math.pow(10.0, (RSSI0 - RSSI) / (10.0 * n));
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
