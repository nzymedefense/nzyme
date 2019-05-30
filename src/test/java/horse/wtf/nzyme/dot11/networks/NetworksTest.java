package horse.wtf.nzyme.dot11.networks;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.Dot11TaggedParameters;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import horse.wtf.nzyme.dot11.parsers.Dot11BeaconFrameParser;
import horse.wtf.nzyme.dot11.parsers.Dot11ProbeResponseFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import org.joda.time.DateTime;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class NetworksTest {

    protected static final Dot11MetaInformation META_NO_WEP = new Dot11MetaInformation(false, 100, 2400, 1, 0L, false);
    
    @Test
    public void testRegisterFrames() throws MalformedFrameException, IllegalRawDataException {
        Networks n = new Networks(new MockNzyme());

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, new Dot11MetaInformation(
                        false, -59, 2400, 1, 0L, false)
        ));

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                Frames.BEACON_3_PAYLOAD, Frames.BEACON_3_HEADER, META_NO_WEP
        ));

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, new Dot11MetaInformation(
                        false, -54, 2400, 1, 0L, false)
        ));

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, new Dot11MetaInformation(
                        false, -57, 2400, 6, 0L, false)
        ));

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, new Dot11MetaInformation(
                        false, -56, 2400, 6, 0L, false)
        ));

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, new Dot11MetaInformation(
                        false, -52, 2400, 6, 0L, false)
        ));

        n.registerProbeResponseFrame(new Dot11ProbeResponseFrameParser(new MetricRegistry()).parse(
                Frames.PROBE_RESP_2_PAYLOAD, Frames.PROBE_RESP_2_HEADER, new Dot11MetaInformation(
                        false, -72, 2400, 1, 0L, false)
        ));

        n.registerProbeResponseFrame(new Dot11ProbeResponseFrameParser(new MetricRegistry()).parse(
                Frames.PROBE_RESP_2_PAYLOAD, Frames.PROBE_RESP_2_HEADER, new Dot11MetaInformation(
                        false, -51, 2400, 6, 0L, false)
        ));

        // Test that this broadcast frame is ignore.
        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                Frames.BEACON_4_PAYLOAD, Frames.BEACON_4_HEADER, META_NO_WEP
        ));

        assertEquals(n.getSSIDs().size(), 2);
        assertEquals(n.getBSSIDs().size(), 2);

        BSSID n1 = n.getBSSIDs().get("00:c0:ca:95:68:3b");
        BSSID n2 = n.getBSSIDs().get("06:0d:2d:c9:36:23");

        assertNotNull(n1);
        assertEquals(n1.oui(), "unknown");
        assertEquals(n1.bssid(), "00:c0:ca:95:68:3b");
        assertFalse(n2.isWPS());
        assertTrue(n1.getLastSeen().isBefore(new DateTime()));
        assertTrue(n1.getLastSeen().isAfter(new DateTime().minusSeconds(5)));
        assertEquals(n1.ssids().size(), 1);
        assertEquals(n1.ssids().get("WTF").name(), "WTF");
        assertEquals(n1.ssids().get("WTF").channels().size(), 2);
        assertEquals(n1.ssids().get("WTF").channels().get(1).totalFrames().get(), 3L);
        assertEquals(n1.ssids().get("WTF").channels().get(1).signalMin().get(), 56);
        assertEquals(n1.ssids().get("WTF").channels().get(1).signalMax().get(), 92);
        assertEquals(n1.ssids().get("WTF").channels().get(1).fingerprints().size(), 1);
        assertEquals(n1.ssids().get("WTF").channels().get(1).fingerprints().get(0), "dfac3abce0c722f9609343f7dfa208afa51a1c7decbd2eb6f96c78051f0a594b");
        assertEquals(n1.ssids().get("WTF").channels().get(6).totalFrames().get(), 4L);
        assertEquals(n1.ssids().get("WTF").channels().get(6).signalMin().get(), 86);
        assertEquals(n1.ssids().get("WTF").channels().get(6).signalMax().get(), 98);
        assertEquals(n1.ssids().get("WTF").channels().get(6).fingerprints().size(), 1);
        assertEquals(n1.ssids().get("WTF").channels().get(6).fingerprints().get(0), "dfac3abce0c722f9609343f7dfa208afa51a1c7decbd2eb6f96c78051f0a594b");

        assertNotNull(n2);
        assertEquals(n2.oui(), "unknown");
        assertEquals(n2.bssid(), "06:0d:2d:c9:36:23");
        assertFalse(n2.isWPS());
        assertTrue(n1.getLastSeen().isBefore(new DateTime()));
        assertTrue(n1.getLastSeen().isAfter(new DateTime().minusSeconds(5)));
        assertEquals(n2.ssids().size(), 1);
        assertEquals(n2.ssids().get("United_Wi-Fi").name(), "United_Wi-Fi");
        assertEquals(n2.ssids().get("United_Wi-Fi").channels().size(), 1);
        assertEquals(n2.ssids().get("United_Wi-Fi").channels().get(1).totalFrames().get(), 1L);
        assertEquals(n2.ssids().get("United_Wi-Fi").channels().get(1).signalMin().get(), 100);
        assertEquals(n2.ssids().get("United_Wi-Fi").channels().get(1).signalMax().get(), 100);
        assertEquals(n2.ssids().get("United_Wi-Fi").channels().get(1).fingerprints().size(), 1);
        assertEquals(n2.ssids().get("United_Wi-Fi").channels().get(1).fingerprints().get(0), "c9ed4adc12dc3e17208446b6a10070b70a73b9ce3a99215e05426faea6de91c7");
    }

    @Test
    public void testLastSeenUpdates() throws MalformedFrameException, IllegalRawDataException {
        Networks n = new Networks(new MockNzyme());

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
        ));

        BSSID n1 = n.getBSSIDs().get("00:c0:ca:95:68:3b");

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) { fail(); }

        assertTrue(n1.getLastSeen().isBefore(new DateTime()));
        assertTrue(n1.getLastSeen().isAfter(new DateTime().minusSeconds(2)));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) { fail(); }

        assertTrue(n1.getLastSeen().isBefore(new DateTime()));
        assertFalse(n1.getLastSeen().isAfter(new DateTime().minusSeconds(2)));

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
        ));

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) { fail(); }

        assertTrue(n1.getLastSeen().isBefore(new DateTime()));
        assertTrue(n1.getLastSeen().isAfter(new DateTime().minusSeconds(2)));
    }

    @Test
    public void testRetentionCleaning() throws MalformedFrameException, IllegalRawDataException {
        Networks n = new Networks(new MockNzyme());

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
        ));

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                Frames.BEACON_3_PAYLOAD, Frames.BEACON_3_HEADER, META_NO_WEP
        ));

        assertEquals(n.getBSSIDs().size(), 2);
        assertFalse(n.getSSIDs().isEmpty());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) { fail(); }

        n.retentionClean(1);

        assertTrue(n.getBSSIDs().isEmpty());
        assertTrue(n.getSSIDs().isEmpty());
    }

    @Test
    public void testIgnoresBroadcastBeacon() throws MalformedFrameException, IllegalRawDataException {
        Networks n = new Networks(new MockNzyme());

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                Frames.BEACON_4_PAYLOAD, Frames.BEACON_4_HEADER, META_NO_WEP
        ));

        assertNotNull(n.getBSSIDs());
        assertNotNull(n.getSSIDs());
        assertEquals( n.getBSSIDs().size(), 0);
        assertEquals( n.getSSIDs().size(), 0);
    }

    @Test
    public void testIgnoresBroadcastProbeResp() throws MalformedFrameException {
        Networks n = new Networks(new MockNzyme());

        // Don't actually have a NULL/broadcast probe-resp because they are so rare in the wild. Create a frame without parser.
        n.registerProbeResponseFrame(Dot11ProbeResponseFrame.create(
                null,
                "00:c0:ca:95:68:3b",
                "06:0d:2d:c9:36:23",
                "foo",
                new Dot11TaggedParameters(
                        new MetricRegistry(),
                        Dot11TaggedParameters.PROBERESP_TAGGED_PARAMS_POSITION,
                        Frames.PROBE_RESP_1_PAYLOAD),
                META_NO_WEP));

        assertNotNull(n.getBSSIDs());
        assertNotNull(n.getSSIDs());
        assertEquals( n.getBSSIDs().size(), 0);
        assertEquals( n.getSSIDs().size(), 0);
    }

    @Test
    public void testGetBSSIDsEmpty() {
        Networks n = new Networks(new MockNzyme());
        assertNotNull(n.getBSSIDs());
        assertEquals(n.getBSSIDs().size(), 0);
    }

    @Test
    public void testGetSSIDsEmpty() {
        Networks n = new Networks(new MockNzyme());
        assertNotNull(n.getSSIDs());
        assertEquals( n.getSSIDs().size(), 0);
    }

    @Test
    public void testGetSignalDelta() throws Networks.NoSuchNetworkException, Networks.NoSuchChannelException, MalformedFrameException, IllegalRawDataException {
        Networks n = new Networks(new MockNzyme());

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, new Dot11MetaInformation(
                        false, -59, 2400, 1, 0L, false)
        ));

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, new Dot11MetaInformation(
                        false, -57, 2400, 1, 0L, false)
        ));

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, new Dot11MetaInformation(
                        false, -54, 2400, 1, 0L, false)
        ));

        n.registerProbeResponseFrame(new Dot11ProbeResponseFrameParser(new MetricRegistry()).parse(
                Frames.PROBE_RESP_2_PAYLOAD, Frames.PROBE_RESP_2_HEADER, new Dot11MetaInformation(
                        false, -56, 2400, 1, 0L, false)
        ));

        SignalDelta delta = n.getSignalDelta("00:c0:ca:95:68:3b", "WTF", 1);

        assertEquals(delta.lower(), 87);
        assertEquals(delta.upper(), 91);
    }

    @Test(expectedExceptions = Networks.NoSuchNetworkException.class)
    public void testGetSignalDeltaNoSuchNetwork() throws Networks.NoSuchNetworkException, Networks.NoSuchChannelException, MalformedFrameException, IllegalRawDataException {
        Networks n = new Networks(new MockNzyme());

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
        ));

        n.getSignalDelta("00:c0:ca:95:68:3b", "foonet", 1);
    }

    @Test(expectedExceptions = Networks.NoSuchNetworkException.class)
    public void testGetSignalDeltaNoSuchNetworkBSSID() throws Networks.NoSuchNetworkException, Networks.NoSuchChannelException, MalformedFrameException, IllegalRawDataException {
        Networks n = new Networks(new MockNzyme());

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
        ));

        n.getSignalDelta("foo", "WTF", 1);
    }

    @Test(expectedExceptions = Networks.NoSuchChannelException.class)
    public void testGetSignalDeltaNoSuchChannel() throws Networks.NoSuchNetworkException, Networks.NoSuchChannelException, MalformedFrameException, IllegalRawDataException {
        Networks n = new Networks(new MockNzyme());

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry()).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
        ));

        n.getSignalDelta("00:c0:ca:95:68:3b", "WTF", 2);
    }

}