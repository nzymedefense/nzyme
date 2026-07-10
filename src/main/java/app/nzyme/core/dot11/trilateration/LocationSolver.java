package app.nzyme.core.dot11.trilateration;

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

import java.util.*;

public class LocationSolver {

    private static final Logger LOG = LogManager.getLogger(LocationSolver.class);

    private static final double REFERENCE_RSSI_AT_1M = -25;
    private static final double BOUNDARY_PADDING_METERS = 3.0;
    private static final double OUTSIDE_OF_PLAN_THRESHOLD_PERCENT = 66;

    public TrilaterationResult solve(List<TapBasedSignalStrengthResultHistogramEntry> signals,
                                     List<Tap> floorTaps,
                                     TenantLocationFloorEntry floor)
            throws InvalidTapsException {

        if (floor.planLengthPixels() == null || floor.planWidthPixels() == null
                || floor.planLengthMeters() == null || floor.planWidthMeters() == null) {
            throw new RuntimeException("Cannot run location solver on incomplete floor configuration.");
        }

        int planWidthPixels = floor.planWidthPixels();
        int planLengthPixels = floor.planLengthPixels();
        int planWidthMeters = floor.planWidthMeters();
        int planLengthMeters = floor.planLengthMeters();

        Map<DateTime, List<TapBasedSignalStrengthResultHistogramEntry>> histo = new TreeMap<>();
        for (TapBasedSignalStrengthResultHistogramEntry signal : signals) {
            histo.computeIfAbsent(signal.bucket(), k -> Lists.newArrayList()).add(signal);
        }

        Map<UUID, Tap> taps = Maps.newHashMap();
        for (Tap tap : floorTaps) {
            taps.put(tap.uuid(), tap);
        }

        int totalDataPoints = 0;
        int distancesOutsideOfBoundaries = 0;
        int attemptedSolves = 0;
        int gatedOutOfBounds = 0;

        Map<DateTime, TrilaterationLocation> result = new TreeMap<>();
        Map<Tap, Integer> outsideOfBoundarySignalStrengths = Maps.newHashMap();
        Map<Tap, Integer> outsideOfBoundarySignalCounts = Maps.newHashMap();

        for (Map.Entry<DateTime, List<TapBasedSignalStrengthResultHistogramEntry>> bucketEntry : histo.entrySet()) {
            DateTime bucket = bucketEntry.getKey();
            List<TapBasedSignalStrengthResultHistogramEntry> signal = bucketEntry.getValue();

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

                double distance = calculateDistance(REFERENCE_RSSI_AT_1M, s.signalStrength(), floor.pathLossExponent());

                totalDataPoints++;
                if (isDistanceOutsideOfBoundaries(
                        distance,
                        tap.x(),
                        tap.y(),
                        planWidthMeters,
                        planLengthMeters,
                        planWidthPixels,
                        planLengthPixels)) {
                    outsideOfBoundarySignalStrengths.put(
                            tap, Math.round(outsideOfBoundarySignalStrengths
                                    .getOrDefault(tap, 0) + s.signalStrength())
                    );
                    outsideOfBoundarySignalCounts.put(
                            tap, outsideOfBoundarySignalCounts.getOrDefault(tap, 0) + 1
                    );
                    distancesOutsideOfBoundaries++;
                    continue;
                }

                positions.add(new Double[]{(double) tap.x(), (double) tap.y()});
                distances.add(distance);
            }

            // Need at least three usable reference points to trilaterate.
            if (positions.size() < 3 || positions.size() != distances.size()) {
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

            int x = (int) position[0];
            int y = (int) position[1];

            attemptedSolves++;

            if (x < 0 || y < 0 || x > planWidthPixels || y > planLengthPixels) {
                gatedOutOfBounds++;
                LOG.debug("Discarding trilaterated position [{}, {}] outside floor plan bounds " +
                        "([0-{}] x [0-{}]) for bucket {}.", x, y, planWidthPixels, planLengthPixels, bucket);
                continue;
            }

            result.put(bucket, TrilaterationLocation.create(x, y));
        }

        Map<Integer, Map<String, Integer>> outsideOfPlanBoundariesTapStrengths = Maps.newHashMap();
        for (Map.Entry<Tap, Integer> t : outsideOfBoundarySignalStrengths.entrySet()) {
            HashMap<String, Integer> location = Maps.newHashMap();
            location.put("x", t.getKey().x());
            location.put("y", t.getKey().y());
            outsideOfPlanBoundariesTapStrengths.put(
                    t.getValue() / outsideOfBoundarySignalCounts.get(t.getKey()), location);
        }

        double outsideOfPlanBoundariesPercentage = totalDataPoints == 0
                ? 0.0
                : distancesOutsideOfBoundaries * 100.0 / totalDataPoints;

        double gatedOutOfBoundsPercentage = attemptedSolves == 0
                ? 0.0
                : gatedOutOfBounds * 100.0 / attemptedSolves;

        boolean isOutsideOfFloorPlanBoundaries =
                outsideOfPlanBoundariesPercentage > OUTSIDE_OF_PLAN_THRESHOLD_PERCENT
                        || gatedOutOfBoundsPercentage > OUTSIDE_OF_PLAN_THRESHOLD_PERCENT;

        LOG.debug("Trilateration outside-of-plan signals: pre-filter {}% ({}/{} measurements), " +
                        "gate {}% ({}/{} solvable buckets), verdict isOutside={}.",
                String.format("%.1f", outsideOfPlanBoundariesPercentage),
                distancesOutsideOfBoundaries, totalDataPoints,
                String.format("%.1f", gatedOutOfBoundsPercentage),
                gatedOutOfBounds, attemptedSolves,
                isOutsideOfFloorPlanBoundaries);

        return TrilaterationResult.create(
                result,
                outsideOfPlanBoundariesPercentage,
                isOutsideOfFloorPlanBoundaries,
                outsideOfPlanBoundariesTapStrengths
        );
    }

    private boolean isDistanceOutsideOfBoundaries(double distanceMeters,
                                                  int tapXPixel, int tapYPixel,
                                                  int floorPlanWidthMeters, int floorPlanLengthMeters,
                                                  int floorPlanWidthPixels, int floorPlanLengthPixels) {
        // Meters-per-pixel on each axis.
        double scaleX = (double) floorPlanWidthMeters / floorPlanWidthPixels;
        double scaleY = (double) floorPlanLengthMeters / floorPlanLengthPixels;

        double[] distancesToCorners = new double[]{
                cornerDistanceMeters(tapXPixel, tapYPixel, 0, 0, scaleX, scaleY),
                cornerDistanceMeters(tapXPixel, tapYPixel, floorPlanWidthPixels, 0, scaleX, scaleY),
                cornerDistanceMeters(tapXPixel, tapYPixel, 0, floorPlanLengthPixels, scaleX, scaleY),
                cornerDistanceMeters(tapXPixel, tapYPixel, floorPlanWidthPixels, floorPlanLengthPixels, scaleX, scaleY)
        };

        double maxDistanceToCorner = 0;
        for (double dist : distancesToCorners) {
            if (dist > maxDistanceToCorner) {
                maxDistanceToCorner = dist;
            }
        }

        return distanceMeters > maxDistanceToCorner + BOUNDARY_PADDING_METERS;
    }

    private static double cornerDistanceMeters(int tapXPixel, int tapYPixel,
                                               int cornerXPixel, int cornerYPixel,
                                               double scaleX, double scaleY) {
        double dxMeters = (cornerXPixel - tapXPixel) * scaleX;
        double dyMeters = (cornerYPixel - tapYPixel) * scaleY;
        return Math.sqrt(dxMeters * dxMeters + dyMeters * dyMeters);
    }

    private double calculateDistance(double RSSI0, double RSSI, double n) {
        return Math.pow(10.0, (RSSI0 - RSSI) / (10.0 * n));
    }

    @AutoValue
    public abstract static class TrilaterationResult {

        public abstract Map<DateTime, TrilaterationLocation> locations();
        public abstract double outsideOfPlanBoundariesPercentage();
        public abstract boolean isOutsideOfFloorPlanBoundaries();
        public abstract Map<Integer, Map<String, Integer>> outsideOfPlanBoundariesTapStrengths();

        public static TrilaterationResult create(Map<DateTime, TrilaterationLocation> locations, double outsideOfPlanBoundariesPercentage, boolean isOutsideOfFloorPlanBoundaries, Map<Integer, Map<String, Integer>> outsideOfPlanBoundariesTapStrengths) {
            return builder()
                    .locations(locations)
                    .outsideOfPlanBoundariesPercentage(outsideOfPlanBoundariesPercentage)
                    .isOutsideOfFloorPlanBoundaries(isOutsideOfFloorPlanBoundaries)
                    .outsideOfPlanBoundariesTapStrengths(outsideOfPlanBoundariesTapStrengths)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_LocationSolver_TrilaterationResult.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder locations(Map<DateTime, TrilaterationLocation> locations);

            public abstract Builder outsideOfPlanBoundariesPercentage(double outsideOfPlanBoundariesPercentage);

            public abstract Builder isOutsideOfFloorPlanBoundaries(boolean isOutsideOfFloorPlanBoundaries);

            public abstract Builder outsideOfPlanBoundariesTapStrengths(Map<Integer, Map<String, Integer>> outsideOfPlanBoundariesTapStrengths);

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