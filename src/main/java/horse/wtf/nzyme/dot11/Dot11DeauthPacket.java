package horse.wtf.nzyme.dot11;

import org.pcap4j.packet.*;
import org.pcap4j.util.ByteArrays;

public class Dot11DeauthPacket extends Dot11ManagementPacket {

    private final Dot11DeauthHeader header;

    public static Dot11DeauthPacket newPacket(byte[] rawData, int offset, int length) throws IllegalRawDataException {
        ByteArrays.validateBounds(rawData, offset, length);
        Dot11DeauthPacket.Dot11DeauthHeader h = new Dot11DeauthPacket.Dot11DeauthHeader(rawData, offset, length);
        return new Dot11DeauthPacket(rawData, offset, length, h);
    }

    private Dot11DeauthPacket(byte[] rawData, int offset, int length, Dot11DeauthPacket.Dot11DeauthHeader header) {
        super(rawData, offset, length, header.length());
        this.header = header;
    }

    @Override
    public Dot11ManagementHeader getHeader() {
        return header;
    }

    @Override
    public Builder getBuilder() {
        return null;
    }

    public static final class Dot11DeauthHeader extends Dot11ManagementHeader {

        // TODO get deauth reason here
        private Dot11DeauthHeader(byte[] rawData, int offset, int length) throws IllegalRawDataException {
            super(rawData, offset, length);
        }

        @Override
        protected String getHeaderName() {
            return "IEEE802.11 Deauthentication header";
        }
    }

}
