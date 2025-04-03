package app.nzyme.core.integrations.tenant.cot.transports;

import app.nzyme.core.integrations.tenant.cot.protocol.CotEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.annotation.Nullable;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

public class CotTlsTcpTransport implements CotTransport {

    private final String address;
    private final int port;
    private final byte[] certificate;
    @Nullable
    private final char[] certificatePassphrase;

    private final XmlMapper xmlMapper = new XmlMapper();

    public CotTlsTcpTransport(String address, int port, byte[] certificate, @Nullable String certificatePassphrase) {
        if (certificate == null || certificate.length == 0) {
            throw new IllegalArgumentException("Certificate cannot be null or empty");
        }

        this.address = address;
        this.port = port;
        this.certificate = certificate;

        if (certificatePassphrase != null) {
            this.certificatePassphrase = certificatePassphrase.toCharArray();
        } else {
            this.certificatePassphrase = null;
        }
    }

    @Override
    public CotProcessingResult sendEvent(CotEvent event) throws CotTransportException {
        if (certificate == null) {
            throw new CotTransportException("CoT transport requires certificate to be uploaded.");
        }

        String payload;
        try {
            payload = xmlMapper.writeValueAsString(event);
        } catch(JsonProcessingException e) {
            throw new CotTransportException("Could not serialize CoT event.", e);
        }

        // Prepare TLS.
        SSLContext sslContext;
        try {
            // Load PKCS#12 file (contains cert, key, CA chain)
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream is = new ByteArrayInputStream(certificate)) {
                keyStore.load(is, certificatePassphrase);
            }

            // Key material for client auth.
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, certificatePassphrase == null ? new char[0] : certificatePassphrase);

            // Trust material to validate the server.
            String keyAlias = null;
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (keyStore.isKeyEntry(alias)) {
                    keyAlias = alias;
                    break;
                }
            }

            if (keyAlias == null) {
                throw new IllegalStateException("No key entry found in PKCS#12 file.");
            }

            Certificate[] chain = keyStore.getCertificateChain(keyAlias);
            // Find the root certificate in the chain
            X509Certificate caCert = (X509Certificate) chain[chain.length - 1];

            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(null);
            trustStore.setCertificateEntry("cacert", caCert);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(trustStore);

            // Build SSLContext.
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        } catch (Exception e) {
            throw new CotTransportException("Could not prepare TLS for CoT event.", e);
        }

        // Create secure socket
        SSLSocketFactory factory = sslContext.getSocketFactory();
        try (SSLSocket socket = (SSLSocket) factory.createSocket(address, port)) {
            socket.startHandshake();

            OutputStream out = socket.getOutputStream();
            out.write(payload.getBytes());
            out.flush();
        } catch (Exception e) {
            throw new CotTransportException("Could not send CoT event.", e);
        }

        return CotProcessingResult.create(payload.length(), 1);
    }

}
