package horse.wtf.nzyme.handlers;

import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.RadiotapPacket;

public abstract class FrameHandler {

    protected final Nzyme nzyme;

    protected FrameHandler(Nzyme nzyme) {
        this.nzyme = nzyme;
    }

    protected void tick() {
        nzyme.getStatistics().tickType(getName());

    }

    protected void malformed() {
        nzyme.getStatistics().tickMalformedCount(nzyme.getChannelHopper().getCurrentChannel());
    }

    public abstract void handle(byte[] payload, Dot11MetaInformation meta) throws IllegalRawDataException;
    public abstract String getName();

}
