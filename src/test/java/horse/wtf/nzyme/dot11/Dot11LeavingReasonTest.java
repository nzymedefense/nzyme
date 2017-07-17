package horse.wtf.nzyme.dot11;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11LeavingReasonTest {

    @Test
    public void testLookup() throws Exception {
        assertEquals(
                Dot11LeavingReason.lookup(2),
                "Previous authentication no longer valid"
        );

        assertEquals(
                Dot11LeavingReason.lookup(9001),
                "Unknown reason (9001)"
        );
    }

}