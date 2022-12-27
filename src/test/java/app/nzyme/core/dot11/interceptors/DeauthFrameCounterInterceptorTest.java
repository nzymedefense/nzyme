package app.nzyme.core.dot11.interceptors;

import com.codahale.metrics.MetricRegistry;
import app.nzyme.core.MockNzyme;
import app.nzyme.core.NzymeLeader;
import app.nzyme.core.dot11.Dot11MetaInformation;
import app.nzyme.core.dot11.MalformedFrameException;
import app.nzyme.core.dot11.anonymization.Anonymizer;
import app.nzyme.core.dot11.deauth.DeauthenticationMonitor;
import app.nzyme.core.dot11.frames.Dot11DeauthenticationFrame;
import app.nzyme.core.dot11.frames.Dot11DisassociationFrame;
import app.nzyme.core.dot11.parsers.Dot11DeauthenticationFrameParser;
import app.nzyme.core.dot11.parsers.Dot11DisassociationFrameParser;
import app.nzyme.core.dot11.parsers.Frames;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class DeauthFrameCounterInterceptorTest {

    private static final Dot11MetaInformation META_NO_WEP = new Dot11MetaInformation(false, 100, 2400, 1, 0L, false);

    @Test
    public void testIntercept() throws MalformedFrameException, IllegalRawDataException {
        NzymeLeader nzyme = new MockNzyme();
        DeauthenticationMonitor monitor = new DeauthenticationMonitor(nzyme);

        Dot11DeauthenticationFrame deauth = new Dot11DeauthenticationFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.DEAUTH_1_PAYLOAD, Frames.DEAUTH_1_HEADER, META_NO_WEP);


        Dot11DisassociationFrame disassoc = new Dot11DisassociationFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.DISASSOC_1_PAYLOAD, Frames.DISASSOC_1_HEADER, META_NO_WEP);

        DeauthFrameCounterInterceptorSet interceptors = new DeauthFrameCounterInterceptorSet(monitor);

        assertEquals(monitor.currentCount(), 0);

        interceptors.getInterceptors().get(0).intercept(deauth);
        assertEquals(monitor.currentCount(), 1);
        interceptors.getInterceptors().get(0).intercept(deauth);
        assertEquals(monitor.currentCount(), 2);
        interceptors.getInterceptors().get(1).intercept(disassoc);
        assertEquals(monitor.currentCount(), 3);
    }

}