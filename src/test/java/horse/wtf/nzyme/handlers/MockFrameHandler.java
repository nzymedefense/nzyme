package horse.wtf.nzyme.handlers;

import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import org.pcap4j.packet.IllegalRawDataException;

public class MockFrameHandler extends FrameHandler {

    public MockFrameHandler(Nzyme nzyme) {
        super(nzyme);
    }

    @Override
    public void handle(byte[] payload, byte[] header, Dot11MetaInformation meta) throws IllegalRawDataException {
        // noop
    }

    @Override
    public String getName() {
        return "MockFrameHandler";
    }

}