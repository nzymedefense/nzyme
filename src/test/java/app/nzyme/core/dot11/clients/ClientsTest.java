package app.nzyme.core.dot11.clients;

import com.codahale.metrics.MetricRegistry;
import app.nzyme.core.MockNzyme;
import app.nzyme.core.dot11.Dot11MetaInformation;
import app.nzyme.core.dot11.MalformedFrameException;
import app.nzyme.core.dot11.anonymization.Anonymizer;
import app.nzyme.core.dot11.parsers.Dot11AssociationRequestFrameParser;
import app.nzyme.core.dot11.parsers.Dot11ProbeRequestFrameParser;
import app.nzyme.core.dot11.parsers.Frames;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ClientsTest {

    protected static final Dot11MetaInformation META_NO_WEP = new Dot11MetaInformation(false, 100, 2400, 1, 0L, false);

    @Test
    public void testRegisterProbeRequestFrame() throws MalformedFrameException, IllegalRawDataException {
        Clients c = new Clients(new MockNzyme());

        c.registerProbeRequestFrame(new Dot11ProbeRequestFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.PROBE_REQ_1_PAYLOAD, Frames.PROBE_REQ_1_HEADER, META_NO_WEP
        ));

        assertEquals(c.getClients().size(), 1);
        assertNotNull(c.getClients().get("3c:8d:20:25:20:e9"));

        Client c1 = c.getClients().get("3c:8d:20:25:20:e9");
        assertEquals(c1.mac(), "3c:8d:20:25:20:e9");
    }

    @Test
    public void testRegisterProbeRequestFrameMultiple() throws MalformedFrameException, IllegalRawDataException {
        Clients c = new Clients(new MockNzyme());

        c.registerProbeRequestFrame(new Dot11ProbeRequestFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.PROBE_REQ_1_PAYLOAD, Frames.PROBE_REQ_1_HEADER, META_NO_WEP
        ));

        c.registerProbeRequestFrame(new Dot11ProbeRequestFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.PROBE_REQ_2_PAYLOAD, Frames.PROBE_REQ_2_HEADER, META_NO_WEP
        ));

        assertEquals(c.getClients().size(), 2);
    }

    @Test
    public void testRegisterAssociationRequestFrame() throws MalformedFrameException, IllegalRawDataException {
        Clients c = new Clients(new MockNzyme());

        c.registerAssociationRequestFrame(new Dot11AssociationRequestFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.ASSOC_REQ_1_PAYLOAD, Frames.ASSOC_REQ_1_HEADER, META_NO_WEP
        ));

        assertEquals(c.getClients().size(), 1);
        assertNotNull(c.getClients().get("ac:81:12:d2:26:7e"));

        Client c1 = c.getClients().get("ac:81:12:d2:26:7e");
        assertEquals(c1.mac(), "ac:81:12:d2:26:7e");
    }

    @Test
    public void testRegisterMutlipleTypesAndUpdates() throws MalformedFrameException, IllegalRawDataException {
        Clients c = new Clients(new MockNzyme());

        c.registerAssociationRequestFrame(new Dot11AssociationRequestFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.ASSOC_REQ_1_PAYLOAD, Frames.ASSOC_REQ_1_HEADER, META_NO_WEP
        ));

        c.registerProbeRequestFrame(new Dot11ProbeRequestFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.PROBE_REQ_1_PAYLOAD, Frames.PROBE_REQ_1_HEADER, META_NO_WEP
        ));

        c.registerProbeRequestFrame(new Dot11ProbeRequestFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.PROBE_REQ_2_PAYLOAD, Frames.PROBE_REQ_2_HEADER, META_NO_WEP
        ));

        c.registerProbeRequestFrame(new Dot11ProbeRequestFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.PROBE_REQ_1_PAYLOAD, Frames.PROBE_REQ_1_HEADER, META_NO_WEP
        ));

        assertEquals(c.getClients().size(), 3);

        assertNotNull(c.getClients().get("ac:81:12:d2:26:7e"));
        assertNotNull(c.getClients().get("3c:8d:20:25:20:e9"));
        assertNotNull(c.getClients().get("a8:51:5b:7f:1b:2d"));
    }

    @Test
    public void testGetClientsWithEmptyList() {
        Clients c = new Clients(new MockNzyme());

        assertNotNull(c.getClients());
        assertEquals(c.getClients().size(), 0);
    }

    @Test
    public void testRetentionClean() throws MalformedFrameException, IllegalRawDataException {
        Clients c = new Clients(new MockNzyme());

        c.registerProbeRequestFrame(new Dot11ProbeRequestFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.PROBE_REQ_1_PAYLOAD, Frames.PROBE_REQ_1_HEADER, META_NO_WEP
        ));

        c.registerProbeRequestFrame(new Dot11ProbeRequestFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.PROBE_REQ_2_PAYLOAD, Frames.PROBE_REQ_2_HEADER, META_NO_WEP
        ));

        assertEquals(2, c.getClients().size());
        assertNotNull(c.getClients().get("a8:51:5b:7f:1b:2d"));
        assertNotNull(c.getClients().get("3c:8d:20:25:20:e9"));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) { fail("interrupted"); }

        c.registerAssociationRequestFrame(new Dot11AssociationRequestFrameParser(new MetricRegistry(), new Anonymizer(false, "")).parse(
                Frames.ASSOC_REQ_1_PAYLOAD, Frames.ASSOC_REQ_1_HEADER, META_NO_WEP
        ));

        assertEquals(3, c.getClients().size());
        assertNotNull(c.getClients().get("a8:51:5b:7f:1b:2d"));
        assertNotNull(c.getClients().get("3c:8d:20:25:20:e9"));
        assertNotNull(c.getClients().get("ac:81:12:d2:26:7e"));

        c.retentionClean(1);

        assertEquals(1, c.getClients().size());
        assertNull(c.getClients().get("a8:51:5b:7f:1b:2d"));
        assertNull(c.getClients().get("3c:8d:20:25:20:e9"));
        assertNotNull(c.getClients().get("ac:81:12:d2:26:7e"));
    }

}