package app.nzyme.core.integrations.tenant.cot.transports;

import app.nzyme.core.integrations.tenant.cot.protocol.CotEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.net.*;

public class CotPlaintextUdpTransport implements CotTransport {

    private final String address;
    private final int port;

    private final XmlMapper xmlMapper = new XmlMapper();

    public CotPlaintextUdpTransport(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    public CotProcessingResult sendEvent(CotEvent event) throws CotTransportException {
        String payload;
        try {
            payload = xmlMapper.writeValueAsString(event);
        } catch(JsonProcessingException e) {
            throw new CotTransportException("Could not serialize CoT event.", e);
        }

        InetAddress serverAddress;
        try {
            serverAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            throw new CotTransportException("Could not prepare CoT socket.", e);
        }

        byte[] buffer = payload.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, port);

        DatagramSocket socket;

        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            throw new CotTransportException("Could not create CoT socket.", e);
        }

        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new CotTransportException("Could not send CoT event.", e);
        } finally {
            socket.close();
        }

        return CotProcessingResult.create(payload.length(), 1);
    }

}
