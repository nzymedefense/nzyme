package app.nzyme.core.dot11.trilateration;

import app.nzyme.core.NzymeNode;
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
import org.joda.time.Duration;

import java.util.*;

public class LocationSolver {

    private static final Logger LOG = LogManager.getLogger(LocationSolver.class);

    public static int WIDTH = 14;
    public static int LENGTH = 13;

    private final NzymeNode nzyme;

    public LocationSolver(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public TrilaterationResult solve(List<TapBasedSignalStrengthResultHistogramEntry> signals, float pathLossExponent)
            throws InvalidTapsException {
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

                    double distance = calculateDistance(-15, s.signalStrength(), pathLossExponent);

                    totalDataPoints++;
                    if(isDistanceOutsideOfBoundaries(tap.name(), distance, tap.x(), tap.y(), WIDTH, LENGTH, 1178, 1232)) {
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

    private boolean isDistanceOutsideOfBoundaries(String tapName, double distance, int tapXPixel, int tapYPixel, int floorplanWidthMeters, int floorplanLengthMeters, int floorplanWidthPixels, int floorplanHeightPixels) {
        // Calculate scale factors
        double floorplanWidth = (double) floorplanWidthMeters / floorplanWidthPixels;
        double floorplanLength = (double) floorplanLengthMeters / floorplanHeightPixels;

        // Convert tap positions from pixels to meters
        double tapX = tapXPixel * floorplanWidth;
        double tapY = tapYPixel * floorplanLength;

        ////////////////////
        // Example values
        double pointX = tapXPixel; // The X coordinate of your point
        double pointY = tapYPixel; // The Y coordinate of your point
        double planWidthPixels = floorplanWidthPixels; // Width of the floor plan in pixels
        double planHeightPixels = floorplanHeightPixels; // Height of the floor plan in pixels
        double planWidthMeters = floorplanWidthMeters; // Width of the floor plan in meters
        double planHeightMeters = floorplanLengthMeters; // Height of the floor plan in meters

        // Calculate scale factors
        double scaleX = planWidthMeters / planWidthPixels;
        double scaleY = planHeightMeters / planHeightPixels;

        // Calculate distances in pixels to each corner
        double distanceToTopLeft = calculateFloorPlanDistance(pointX, pointY, 0, 0);
        double distanceToTopRight = calculateFloorPlanDistance(pointX, pointY, planWidthPixels, 0);
        double distanceToBottomLeft = calculateFloorPlanDistance(pointX, pointY, 0, planHeightPixels);
        double distanceToBottomRight = calculateFloorPlanDistance(pointX, pointY, planWidthPixels, planHeightPixels);

        // Convert pixel distances to meters
        double[] distancesToCorners = new double[4];
        distancesToCorners[0] = distanceToTopLeft * scaleX;
        distancesToCorners[1] = distanceToTopRight * Math.sqrt(scaleX * scaleX + scaleY * scaleY);
        distancesToCorners[2] = distanceToBottomLeft * Math.sqrt(scaleX * scaleX + scaleY * scaleY);
        distancesToCorners[3] = distanceToBottomRight * scaleX;

        // Find the maximum distance to any corner
        double maxDistanceToCorner = 0;
        for (double dist : distancesToCorners) {
            if (dist > maxDistanceToCorner) {
                maxDistanceToCorner = dist;
            }
        }

        // If the estimated distance to the signal source is greater than the maximum distance to any corner, it's likely outside
        return distance > maxDistanceToCorner;
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
