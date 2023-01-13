package app.nzyme.core.distributed;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class NodeInformationTest {

    @Test
    public void testCollect() {
        NodeInformation ni = new NodeInformation();
        NodeInformation.Info i = ni.collect();

        // Just testing that there are no exceptions because information differs wildly based on the underlying system.
    }

}