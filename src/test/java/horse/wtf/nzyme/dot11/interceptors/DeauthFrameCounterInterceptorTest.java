package horse.wtf.nzyme.dot11.interceptors;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.deauth.DeauthenticationMonitor;
import horse.wtf.nzyme.dot11.frames.Dot11DeauthenticationFrame;
import horse.wtf.nzyme.dot11.parsers.Dot11DeauthenticationFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class DeauthFrameCounterInterceptorTest {

    private static final Dot11MetaInformation META_NO_WEP = new Dot11MetaInformation(false, 100, 2400, 1, 0L, false);

    @Test
    public void testIntercept() throws MalformedFrameException, IllegalRawDataException {
        NzymeLeader nzyme = new MockNzyme();
        DeauthenticationMonitor monitor = new DeauthenticationMonitor(nzyme);

        Dot11DeauthenticationFrame frame = new Dot11DeauthenticationFrameParser(new MetricRegistry(), new Anonymizer(false, ""))
                .parse(Frames.DEAUTH_1_PAYLOAD, Frames.DEAUTH_1_HEADER, META_NO_WEP);

        DeauthFrameCounterInterceptor interceptor = new DeauthFrameCounterInterceptor(monitor);

        assertEquals(monitor.currentCount(), 0);
        interceptor.intercept(frame);
        assertEquals(monitor.currentCount(), 1);
        interceptor.intercept(frame);
        assertEquals(monitor.currentCount(), 2);
    }

}