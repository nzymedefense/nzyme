package horse.wtf.nzyme.dot11.deauth;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.alerts.DeauthFloodAlert;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.frames.Dot11DeauthenticationFrame;
import horse.wtf.nzyme.dot11.frames.Dot11DisassociationFrame;
import horse.wtf.nzyme.dot11.parsers.Dot11DeauthenticationFrameParser;
import horse.wtf.nzyme.dot11.parsers.Dot11DisassociationFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import horse.wtf.nzyme.notifications.uplinks.misc.LoopbackUplink;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class DeauthenticationMonitorTest {

    private static final Dot11MetaInformation META_NO_WEP = new Dot11MetaInformation(false, 100, 2400, 1, 0L, false);

    @BeforeMethod
    public void cleanDatabase() {
        NzymeLeader nzyme = new MockNzyme();
        nzyme.getDatabase().useHandle(handle -> handle.execute("DELETE FROM deauth_monitor;"));
    }

    @Test
    public void testRecordFrame() throws MalformedFrameException, IllegalRawDataException, InterruptedException {
        NzymeLeader nzyme = new MockNzyme();
        DeauthenticationMonitor monitor = new DeauthenticationMonitor(nzyme,2);

        assertEquals(monitor.currentCount(), 0L);

        Dot11DeauthenticationFrame frame = new Dot11DeauthenticationFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.DEAUTH_1_PAYLOAD, Frames.DEAUTH_1_HEADER, META_NO_WEP);

        monitor.record(frame);
        assertEquals(monitor.currentCount(), 1L);

        monitor.record(frame);
        assertEquals(monitor.currentCount(), 2L);

        monitor.record(frame);
        assertEquals(monitor.currentCount(), 3L);

        Thread.sleep(3000); // sync kicks in
        assertEquals(monitor.currentCount(), 0L);

        long newCount = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM deauth_monitor")
                        .mapTo(Long.class)
                        .first());

        long frameCount = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT total_frame_count FROM deauth_monitor LIMIT 1")
                        .mapTo(Long.class)
                        .first());

        assertEquals(newCount, 1);
        assertEquals(frameCount, 3);

        monitor.record(frame);
        assertEquals(monitor.currentCount(), 1L);
    }

    @Test
    public void testAlerting() throws MalformedFrameException, IllegalRawDataException, InterruptedException {
        NzymeLeader nzyme = new MockNzyme();
        LoopbackUplink loopback = new LoopbackUplink();
        nzyme.registerUplink(loopback);

        DeauthenticationMonitor monitor = new DeauthenticationMonitor(nzyme,2);

        assertNull(loopback.getLastAlert());

        Dot11DeauthenticationFrame deauth = new Dot11DeauthenticationFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.DEAUTH_1_PAYLOAD, Frames.DEAUTH_1_HEADER, META_NO_WEP);
        Dot11DisassociationFrame disassoc = new Dot11DisassociationFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.DISASSOC_1_PAYLOAD, Frames.DISASSOC_1_HEADER, META_NO_WEP);

        for (int i = 0; i<7; i++) {
            monitor.record(deauth);
            monitor.record(disassoc);
        }

        Thread.sleep(3000); // sync kicks in

        assertNotNull(loopback.getLastAlert());
        assertEquals(loopback.getLastAlert().getClass(), DeauthFloodAlert.class);
    }

}