package horse.wtf.nzyme.dot11.networks;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.Dot11SecurityConfiguration;
import horse.wtf.nzyme.dot11.Dot11TaggedParameters;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;
import horse.wtf.nzyme.dot11.parsers.Dot11BeaconFrameParser;
import horse.wtf.nzyme.dot11.parsers.Dot11ProbeResponseFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import org.joda.time.DateTime;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.testng.Assert.*;

public class NetworksTest {

    protected static final Dot11MetaInformation META_NO_WEP = new Dot11MetaInformation(false, 100, 2400, 1, 0L, false);
    
    @Test
    public void testRegisterFrames() throws MalformedFrameException, IllegalRawDataException {
        Networks n = new Networks(new MockNzyme());

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, new Dot11MetaInformation(
                        false, -59, 2400, 1, 0L, false)
        ));

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.BEACON_3_PAYLOAD, Frames.BEACON_3_HEADER, META_NO_WEP
        ));

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, new Dot11MetaInformation(
                        false, -54, 2400, 1, 0L, false)
        ));

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, new Dot11MetaInformation(
                        false, -57, 2400, 6, 0L, false)
        ));

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, new Dot11MetaInformation(
                        false, -56, 2400, 6, 0L, false)
        ));

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, new Dot11MetaInformation(
                        false, -52, 2400, 6, 0L, false)
        ));

        n.registerProbeResponseFrame(new Dot11ProbeResponseFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.PROBE_RESP_2_PAYLOAD, Frames.PROBE_RESP_2_HEADER, new Dot11MetaInformation(
                        false, -72, 2400, 1, 0L, false)
        ));

        n.registerProbeResponseFrame(new Dot11ProbeResponseFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.PROBE_RESP_2_PAYLOAD, Frames.PROBE_RESP_2_HEADER, new Dot11MetaInformation(
                        false, -51, 2400, 6, 0L, false)
        ));

        // Test that this broadcast frame is ignore.
        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.BEACON_4_PAYLOAD, Frames.BEACON_4_HEADER, META_NO_WEP
        ));

        // Required for the getLastSeen() comparisons later.
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) { fail(); }

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
        assertEquals(n1.ssids().get("WTF").getSecurity().size(), 2);
        assertEquals(n1.ssids().get("WTF").getSecurity().get(0),  Dot11SecurityConfiguration.create(
                Dot11SecurityConfiguration.MODE.WPA1,
                new ArrayList<Dot11SecurityConfiguration.KEY_MGMT_MODE>(){{
                    add(Dot11SecurityConfiguration.KEY_MGMT_MODE.EAM);
                    add(Dot11SecurityConfiguration.KEY_MGMT_MODE.PSK);
                }},
                new ArrayList<Dot11SecurityConfiguration.ENCRYPTION_MODE>(){{
                    add(Dot11SecurityConfiguration.ENCRYPTION_MODE.CCMP);
                    add(Dot11SecurityConfiguration.ENCRYPTION_MODE.TKIP);
                }}
        ));
        assertEquals(n1.ssids().get("WTF").getSecurity().get(1),  Dot11SecurityConfiguration.create(
                Dot11SecurityConfiguration.MODE.WPA2,
                new ArrayList<Dot11SecurityConfiguration.KEY_MGMT_MODE>(){{
                    add(Dot11SecurityConfiguration.KEY_MGMT_MODE.EAM);
                    add(Dot11SecurityConfiguration.KEY_MGMT_MODE.PSK);
                }},
                new ArrayList<Dot11SecurityConfiguration.ENCRYPTION_MODE>(){{
                    add(Dot11SecurityConfiguration.ENCRYPTION_MODE.CCMP);
                    add(Dot11SecurityConfiguration.ENCRYPTION_MODE.TKIP);
                }}
        ));
        assertEquals(n1.ssids().get("WTF").channels().size(), 2);
        assertEquals(n1.ssids().get("WTF").channels().get(1).totalFrames().get(), 3L);
        assertEquals(n1.ssids().get("WTF").channels().get(1).fingerprints().size(), 2);
        assertEquals(n1.ssids().get("WTF").channels().get(1).fingerprints().get(0), "dfac3abce0c722f9609343f7dfa208afa51a1c7decbd2eb6f96c78051f0a594b");
        assertEquals(n1.ssids().get("WTF").channels().get(6).totalFrames().get(), 4L);
        assertEquals(n1.ssids().get("WTF").channels().get(6).fingerprints().size(), 2);
        assertEquals(n1.ssids().get("WTF").channels().get(6).fingerprints().get(0), "dfac3abce0c722f9609343f7dfa208afa51a1c7decbd2eb6f96c78051f0a594b");

        assertNotNull(n2);
        assertEquals(n2.oui(), "unknown");
        assertEquals(n2.bssid(), "06:0d:2d:c9:36:23");
        assertFalse(n2.isWPS());
        assertTrue(n1.getLastSeen().isBefore(new DateTime()));
        assertTrue(n1.getLastSeen().isAfter(new DateTime().minusSeconds(5)));
        assertEquals(n2.ssids().size(), 1);
        assertEquals(n2.ssids().get("United_Wi-Fi").name(), "United_Wi-Fi");
        assertEquals(n2.ssids().get("United_Wi-Fi").getSecurity().size(), 1);
        assertEquals(n2.ssids().get("United_Wi-Fi").getSecurity().get(0),  Dot11SecurityConfiguration.create(
                Dot11SecurityConfiguration.MODE.NONE,
                Collections.emptyList(),
                Collections.emptyList()
        ));
        assertEquals(n2.ssids().get("United_Wi-Fi").channels().size(), 1);
        assertEquals(n2.ssids().get("United_Wi-Fi").channels().get(1).totalFrames().get(), 1L);
        assertEquals(n2.ssids().get("United_Wi-Fi").channels().get(1).fingerprints().size(), 1);
        assertEquals(n2.ssids().get("United_Wi-Fi").channels().get(1).fingerprints().get(0), "c9ed4adc12dc3e17208446b6a10070b70a73b9ce3a99215e05426faea6de91c7");
    }

    @Test
    public void testLastSeenUpdates() throws MalformedFrameException, IllegalRawDataException {
        Networks n = new Networks(new MockNzyme());

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
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

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
        ));

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) { fail(); }

        assertTrue(n1.getLastSeen().isBefore(new DateTime()));
        assertTrue(n1.getLastSeen().isAfter(new DateTime().minusSeconds(2)));
    }

    @Test
    public void testSecurityChangeIsRecorded() throws MalformedFrameException, IllegalRawDataException {
        Networks n = new Networks(new MockNzyme());

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
        ));

        BSSID n1 = n.getBSSIDs().get("00:c0:ca:95:68:3b");
        assertEquals(n1.ssids().get("WTF").getSecurity().size(), 2);
        assertEquals(n1.ssids().get("WTF").getSecurity().get(0),  Dot11SecurityConfiguration.create(
                Dot11SecurityConfiguration.MODE.WPA1,
                new ArrayList<Dot11SecurityConfiguration.KEY_MGMT_MODE>(){{
                    add(Dot11SecurityConfiguration.KEY_MGMT_MODE.EAM);
                    add(Dot11SecurityConfiguration.KEY_MGMT_MODE.PSK);
                }},
                new ArrayList<Dot11SecurityConfiguration.ENCRYPTION_MODE>(){{
                    add(Dot11SecurityConfiguration.ENCRYPTION_MODE.CCMP);
                }}
        ));
        assertEquals(n1.ssids().get("WTF").getSecurity().get(1),  Dot11SecurityConfiguration.create(
                Dot11SecurityConfiguration.MODE.WPA2,
                new ArrayList<Dot11SecurityConfiguration.KEY_MGMT_MODE>(){{
                    add(Dot11SecurityConfiguration.KEY_MGMT_MODE.EAM);
                    add(Dot11SecurityConfiguration.KEY_MGMT_MODE.PSK);
                }},
                new ArrayList<Dot11SecurityConfiguration.ENCRYPTION_MODE>(){{
                    add(Dot11SecurityConfiguration.ENCRYPTION_MODE.CCMP);
                }}
        ));

        // Same network, but advertising TKIP, too.
        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.BEACON_2_PAYLOAD, Frames.BEACON_2_HEADER, META_NO_WEP
        ));

        assertEquals(n1.ssids().get("WTF").getSecurity().size(), 2);
        assertEquals(n1.ssids().get("WTF").getSecurity().get(0),  Dot11SecurityConfiguration.create(
                Dot11SecurityConfiguration.MODE.WPA1,
                new ArrayList<Dot11SecurityConfiguration.KEY_MGMT_MODE>(){{
                    add(Dot11SecurityConfiguration.KEY_MGMT_MODE.EAM);
                    add(Dot11SecurityConfiguration.KEY_MGMT_MODE.PSK);
                }},
                new ArrayList<Dot11SecurityConfiguration.ENCRYPTION_MODE>(){{
                    add(Dot11SecurityConfiguration.ENCRYPTION_MODE.CCMP);
                    add(Dot11SecurityConfiguration.ENCRYPTION_MODE.TKIP);
                }}
        ));
        assertEquals(n1.ssids().get("WTF").getSecurity().get(1),  Dot11SecurityConfiguration.create(
                Dot11SecurityConfiguration.MODE.WPA2,
                new ArrayList<Dot11SecurityConfiguration.KEY_MGMT_MODE>(){{
                    add(Dot11SecurityConfiguration.KEY_MGMT_MODE.EAM);
                    add(Dot11SecurityConfiguration.KEY_MGMT_MODE.PSK);
                }},
                new ArrayList<Dot11SecurityConfiguration.ENCRYPTION_MODE>(){{
                    add(Dot11SecurityConfiguration.ENCRYPTION_MODE.CCMP);
                    add(Dot11SecurityConfiguration.ENCRYPTION_MODE.TKIP);
                }}
        ));
    }

    @Test
    public void testRetentionCleaning() throws MalformedFrameException, IllegalRawDataException {
        Networks n = new Networks(new MockNzyme());

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.BEACON_1_PAYLOAD, Frames.BEACON_1_HEADER, META_NO_WEP
        ));

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
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

        n.registerBeaconFrame(new Dot11BeaconFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
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

}