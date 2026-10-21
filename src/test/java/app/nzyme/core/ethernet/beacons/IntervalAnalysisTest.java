package app.nzyme.core.ethernet.beacons;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Random;

import static org.testng.Assert.*;

public class IntervalAnalysisTest {

    private static final DateTime START = new DateTime(2026, 10, 19, 12, 0, 0, DateTimeZone.UTC);
    private static final Duration COLLAPSE = Duration.standardSeconds(5);

    private List<DateTime> regular(int count, int intervalSeconds) {
        List<DateTime> result = Lists.newArrayList();
        for (int i = 0; i < count; i++) {
            result.add(START.plusSeconds(i * intervalSeconds));
        }
        return result;
    }

    @Test
    public void testPerfectlyRegularBeacon() {
        IntervalAnalysis.Result result = IntervalAnalysis.analyze(regular(24, 3600), COLLAPSE, 0.3);

        assertEquals(result.checkinCount(), 24);
        assertEquals(result.intervalMedianSeconds(), 3600.0);
        assertEquals(result.intervalMeanSeconds(), 3600.0);
        assertEquals(result.jitter(), 0.0);
        assertEquals(result.regularity(), 1.0);
        assertEquals(result.firstSeen(), START);
        assertEquals(result.lastSeen(), START.plusSeconds(23 * 3600));

        assertTrue(IntervalAnalysis.isBeacon(result, 6, 30, 0.7));
    }

    @Test
    public void testJitteredBeaconWithinTolerance() {
        // 60 second beacon with up to +-10 seconds of jitter.
        Random random = new Random(42);
        List<DateTime> timestamps = Lists.newArrayList();
        DateTime ts = START;
        for (int i = 0; i < 50; i++) {
            timestamps.add(ts);
            ts = ts.plusSeconds(60 + random.nextInt(21) - 10);
        }

        IntervalAnalysis.Result result = IntervalAnalysis.analyze(timestamps, COLLAPSE, 0.3);

        assertEquals(result.checkinCount(), 50);
        assertTrue(result.intervalMedianSeconds() > 50 && result.intervalMedianSeconds() < 70);
        assertTrue(result.jitter() < 0.2, "Jitter was " + result.jitter());
        assertEquals(result.regularity(), 1.0);
        assertTrue(IntervalAnalysis.isBeacon(result, 6, 30, 0.7));
    }

    @Test
    public void testJitteredBeaconOutsideTolerance() {
        // 60 second beacon with up to +-50 seconds of jitter is not regular at 10% tolerance.
        Random random = new Random(1);
        List<DateTime> timestamps = Lists.newArrayList();
        DateTime ts = START;
        for (int i = 0; i < 50; i++) {
            timestamps.add(ts);
            ts = ts.plusSeconds(60 + random.nextInt(101) - 50);
        }

        IntervalAnalysis.Result result = IntervalAnalysis.analyze(timestamps, COLLAPSE, 0.1);

        assertTrue(result.regularity() < 0.7, "Regularity was " + result.regularity());
        assertFalse(IntervalAnalysis.isBeacon(result, 6, 30, 0.7));
    }

    @Test
    public void testRandomTrafficIsNotABeacon() {
        Random random = new Random(7);
        List<DateTime> timestamps = Lists.newArrayList();
        DateTime ts = START;
        for (int i = 0; i < 100; i++) {
            timestamps.add(ts);
            ts = ts.plusSeconds(10 + random.nextInt(3000));
        }

        IntervalAnalysis.Result result = IntervalAnalysis.analyze(timestamps, COLLAPSE, 0.3);

        assertFalse(IntervalAnalysis.isBeacon(result, 6, 30, 0.7),
                "Regularity was " + result.regularity());
    }

    @Test
    public void testSingleOutlierDoesNotBreakRegularity() {
        // Hourly beacon, with a laptop that slept for eight hours in the middle.
        List<DateTime> timestamps = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            timestamps.add(START.plusHours(i));
        }
        for (int i = 0; i < 10; i++) {
            timestamps.add(START.plusHours(18 + i));
        }

        IntervalAnalysis.Result result = IntervalAnalysis.analyze(timestamps, COLLAPSE, 0.3);

        assertEquals(result.checkinCount(), 20);
        assertEquals(result.intervalMedianSeconds(), 3600.0);
        // 18 regular intervals, 1 outlier.
        assertEquals(result.regularity(), 18.0 / 19.0, 0.0001);
        assertEquals(result.jitter(), 0.0);
        assertTrue(IntervalAnalysis.isBeacon(result, 6, 30, 0.7));
    }

    @Test
    public void testParallelConnectionsAreCollapsed() {
        // Every 5 minutes, a browser opens 6 connections within two seconds.
        List<DateTime> timestamps = Lists.newArrayList();
        for (int i = 0; i < 12; i++) {
            DateTime base = START.plusMinutes(i * 5);
            for (int j = 0; j < 6; j++) {
                timestamps.add(base.plusMillis(j * 300));
            }
        }

        IntervalAnalysis.Result result = IntervalAnalysis.analyze(timestamps, COLLAPSE, 0.3);

        assertEquals(result.checkinCount(), 12);
        assertEquals(result.intervalMedianSeconds(), 300.0);
        assertEquals(result.regularity(), 1.0);
    }

    @Test
    public void testUnsortedInputIsSorted() {
        List<DateTime> timestamps = Lists.newArrayList(
                START.plusMinutes(20), START, START.plusMinutes(10), START.plusMinutes(30)
        );

        IntervalAnalysis.Result result = IntervalAnalysis.analyze(timestamps, COLLAPSE, 0.3);

        assertEquals(result.checkinCount(), 4);
        assertEquals(result.intervalMedianSeconds(), 600.0);
        assertEquals(result.firstSeen(), START);
        assertEquals(result.lastSeen(), START.plusMinutes(30));
    }

    @Test
    public void testTooFewCheckins() {
        IntervalAnalysis.Result result = IntervalAnalysis.analyze(regular(3, 60), COLLAPSE, 0.3);

        assertEquals(result.checkinCount(), 3);
        assertEquals(result.regularity(), 1.0);
        assertFalse(IntervalAnalysis.isBeacon(result, 6, 30, 0.7));
    }

    @Test
    public void testIntervalBelowMinimum() {
        IntervalAnalysis.Result result = IntervalAnalysis.analyze(regular(100, 10), COLLAPSE, 0.3);

        assertEquals(result.intervalMedianSeconds(), 10.0);
        assertFalse(IntervalAnalysis.isBeacon(result, 6, 30, 0.7));
        assertTrue(IntervalAnalysis.isBeacon(result, 6, 5, 0.7));
    }

    @Test
    public void testSingleTimestamp() {
        IntervalAnalysis.Result result = IntervalAnalysis.analyze(List.of(START), COLLAPSE, 0.3);

        assertEquals(result.checkinCount(), 1);
        assertEquals(result.intervalMedianSeconds(), 0.0);
        assertEquals(result.regularity(), 0.0);
        assertFalse(IntervalAnalysis.isBeacon(result, 1, 0, 0.0));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testEmptyInput() {
        IntervalAnalysis.analyze(List.of(), COLLAPSE, 0.3);
    }

    @Test
    public void testMedian() {
        assertEquals(IntervalAnalysis.median(List.of()), 0.0);
        assertEquals(IntervalAnalysis.median(List.of(5.0)), 5.0);
        assertEquals(IntervalAnalysis.median(List.of(3.0, 1.0, 2.0)), 2.0);
        assertEquals(IntervalAnalysis.median(List.of(4.0, 1.0, 3.0, 2.0)), 2.5);
    }

}
