package horse.wtf.nzyme;

import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;

import java.net.InetSocketAddress;

public class Graylog {

    private static final String SOURCE = "nzyme";

    private final String nzymeId;

    private final GelfTransport gelfTransport;

    public Graylog(String hostname, int port, String nzymeId) {
        this.nzymeId = nzymeId;

        this.gelfTransport = GelfTransports.create(new GelfConfiguration(new InetSocketAddress(hostname, port))
                .transport(GelfTransports.TCP)
                .queueSize(512)
                .connectTimeout(5000)
                .reconnectDelay(1000)
                .tcpNoDelay(true)
                .sendBufferSize(32768));
    }

    public void notify(Notification notification) {
        GelfMessage gelf = new GelfMessage(notification.getMessage(), SOURCE);
        gelf.addAdditionalFields(notification.getAdditionalFields());

        this.gelfTransport.trySend(gelf);
    }

}
