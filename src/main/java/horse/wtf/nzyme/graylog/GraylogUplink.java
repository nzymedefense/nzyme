package horse.wtf.nzyme.graylog;

import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;

public class GraylogUplink {

    private static final String SOURCE = "nzyme";

    private final String nzymeId;

    private final GelfTransport gelfTransport;

    public GraylogUplink(String hostname, int port, String nzymeId) {
        this.nzymeId = nzymeId;

        this.gelfTransport = GelfTransports.create(new GelfConfiguration(new InetSocketAddress(hostname, port))
                .transport(GelfTransports.TCP)
                .queueSize(512)
                .connectTimeout(5000)
                .reconnectDelay(1000)
                .tcpNoDelay(true)
                .sendBufferSize(32768));
    }

    public void notify(Notification notification, @Nullable Dot11MetaInformation meta) {
        // Add signal strength and frequency to message.
        StringBuilder sb = new StringBuilder(notification.getMessage());

        if(meta != null) {
            sb.append(" ").append("(").append(meta.getFrequency()).append("MHz @")
                    .append(" ").append(meta.getAntennaSignal()).append("dBm)")
                    .toString();
        }

        GelfMessage gelf = new GelfMessage(sb.toString(), SOURCE);
        gelf.addAdditionalFields(notification.getAdditionalFields());
        gelf.addAdditionalField("nzyme_sensor_id", nzymeId);

        // Meta information.
        if(meta != null) {
            gelf.addAdditionalField("signal_strength", meta.getAntennaSignal());
            gelf.addAdditionalField("frequency", meta.getFrequency());

            gelf.addAdditionalField("signal_quality", calculateSignalQuality(meta.getAntennaSignal()));
        }

        this.gelfTransport.trySend(gelf);
    }

    private int calculateSignalQuality(int antennaSignal) {
        if(antennaSignal >= -50) {
             return 100;
        }

        if(antennaSignal <= -100) {
            return 0;
        }

        return 2*(antennaSignal+100);
    }

}
