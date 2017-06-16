package horse.wtf.nzyme.dot11;

import org.pcap4j.packet.Dot11ManagementPacket;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.util.ByteArrays;

public class Dot11BeaconPacket extends Dot11ManagementPacket {

    private final Dot11BeaconHeader header;

    public static Dot11BeaconPacket newPacket(byte[] rawData, int offset, int length) throws IllegalRawDataException {
        ByteArrays.validateBounds(rawData, offset, length);
        Dot11BeaconPacket.Dot11BeaconHeader h = new Dot11BeaconPacket.Dot11BeaconHeader(rawData, offset, length);
        return new Dot11BeaconPacket(rawData, offset, length, h);
    }

    private Dot11BeaconPacket(byte[] rawData, int offset, int length, Dot11BeaconPacket.Dot11BeaconHeader header) {
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

    public static final class Dot11BeaconHeader extends Dot11ManagementHeader {

        // TODO get deauth reason here
        private Dot11BeaconHeader(byte[] rawData, int offset, int length) throws IllegalRawDataException {
            super(rawData, offset, length);
        }

        @Override
        protected String getHeaderName() {
            return "IEEE802.11 Beacon header";
        }
    }

}
