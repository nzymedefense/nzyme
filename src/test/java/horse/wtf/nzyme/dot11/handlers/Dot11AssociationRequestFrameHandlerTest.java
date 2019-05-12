package horse.wtf.nzyme.dot11.handlers;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.MockNzyme;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.MalformedFrameException;
import horse.wtf.nzyme.dot11.frames.Dot11AssociationRequestFrame;
import horse.wtf.nzyme.dot11.parsers.Dot11AssociationRequestFrameParser;
import horse.wtf.nzyme.dot11.parsers.Frames;
import horse.wtf.nzyme.dot11.probes.Dot11MockProbe;
import horse.wtf.nzyme.statistics.Statistics;
import org.pcap4j.packet.IllegalRawDataException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Dot11AssociationRequestFrameHandlerTest extends FrameHandlerTest {

    @Test
    public void testDoHandle() throws MalformedFrameException, IllegalRawDataException {
        Nzyme nzyme = new MockNzyme();

        Dot11AssociationRequestFrame frame = new Dot11AssociationRequestFrameParser(new MetricRegistry())
                .parse(Frames.ASSOC_REQ_1_PAYLOAD, Frames.ASSOC_REQ_1_HEADER, META_NO_WEP);

        new Dot11AssociationRequestFrameHandler(new Dot11MockProbe(nzyme, CONFIG_STANDARD, new Statistics()))
                .doHandle(frame);


    }

}