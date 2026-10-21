package app.nzyme.core.ethernet.beacons;

import org.testng.annotations.Test;

import java.util.Random;

import static org.testng.Assert.*;

public class BeaconNameGeneratorTest {

    @Test
    public void testFormat() {
        BeaconNameGenerator generator = new BeaconNameGenerator(new Random(1));

        for (int i = 0; i < 1000; i++) {
            String name = generator.generate();
            assertTrue(name.matches("^[a-z]+-[a-z]+-[0-9]{2}$"), "Unexpected name format: " + name);
        }
    }

    @Test
    public void testDeterministicWithSeed() {
        assertEquals(new BeaconNameGenerator(new Random(5)).generate(), new BeaconNameGenerator(new Random(5)).generate());
    }

    @Test
    public void testNeverContainsTheWordBeacon() {
        // "beacon" in a beacon name is confusing. Exhaust the generator with many samples.
        BeaconNameGenerator generator = new BeaconNameGenerator(new java.util.Random(42));
        for (int i = 0; i < 100000; i++) {
            assertFalse(generator.generate().toLowerCase().contains("beacon"));
        }
    }

}
