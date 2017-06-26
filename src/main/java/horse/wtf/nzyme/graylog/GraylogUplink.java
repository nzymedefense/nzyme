package horse.wtf.nzyme.graylog;

import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;

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

    public void notify(Notification notification, Dot11MetaInformation meta) {
        // Add signal strength and frequency to message.
        String message = new StringBuilder(notification.getMessage()).append(" ")
                .append("(").append(meta.getFrequency()).append("MHz @")
                .append(" ").append(meta.getAntennaSignal()).append("dBm)")
                .toString();

        GelfMessage gelf = new GelfMessage(message, SOURCE);
        gelf.addAdditionalFields(notification.getAdditionalFields());
        gelf.addAdditionalField("nzyme_sensor_id", nzymeId);

        // Meta information.
        gelf.addAdditionalField("signal_strength", meta.getAntennaSignal());
        gelf.addAdditionalField("frequency", meta.getFrequency());

        this.gelfTransport.trySend(gelf);
    }

}
