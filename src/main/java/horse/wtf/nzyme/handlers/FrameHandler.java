package horse.wtf.nzyme.handlers;

import horse.wtf.nzyme.Graylog;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.RadiotapPacket;

public abstract class FrameHandler {

    protected final Graylog graylog;

    protected FrameHandler(Graylog graylog) {
        this.graylog = graylog;
    }

    public abstract void handle(byte[] payload, RadiotapPacket.RadiotapHeader header) throws IllegalRawDataException;
    public abstract String getName();

}
