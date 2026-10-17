package app.nzyme.core.rest.resources.taps.reports.tables.tls;

import app.nzyme.core.rest.ObjectMapperProvider;
import com.google.common.io.Resources;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.annotations.Test;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

import static org.testng.Assert.*;

public class TlsSessionsReportTest {

    @Test
    public void testDeserializesTapReport() throws Exception {
        String json = Resources.toString(
                Resources.getResource("tap-reports/tls_sessions.json"), StandardCharsets.UTF_8
        );

        ObjectMapper om = new ObjectMapperProvider().getContext(TlsSessionsReport.class);
        TlsSessionsReport report = om.readValue(json, TlsSessionsReport.class);

        assertEquals(report.sessions().size(), 1);
        TlsSessionReport session = report.sessions().get(0);

        assertEquals(session.connectionStatus(), "Inactive");
        assertEquals(session.sourceMac(), "aa:bb:cc:dd:ee:01");
        assertEquals(session.sourceAddress(), "10.0.0.10");
        assertEquals(session.sourcePort(), 51000);
        assertEquals(session.destinationAddress(), "93.184.216.34");
        assertEquals(session.destinationPort(), 443);
        assertEquals(session.bytesCount(), 5500L);
        assertEquals(session.establishedAt().withZone(DateTimeZone.UTC),
                new DateTime(2026, 10, 16, 12, 0, 0, DateTimeZone.UTC));
        assertEquals(session.terminatedAt().withZone(DateTimeZone.UTC),
                new DateTime(2026, 10, 16, 12, 0, 5, DateTimeZone.UTC));
        assertFalse(session.helloRetryRequest());
        assertFalse(session.clientCertificateRequested());
        assertTrue(session.clientChangeCipherSpec());
        assertTrue(session.serverChangeCipherSpec());
        assertEquals(session.clientApplicationDataRecords(), 3L);
        assertEquals(session.serverApplicationDataBytes(), 3900L);

        TlsClientHelloReport clientHello = session.clientHello();
        assertNotNull(clientHello);
        assertEquals(clientHello.legacyVersion(), "TLSv1.2");
        assertEquals(clientHello.supportedVersions(), java.util.List.of("TLSv1.3", "TLSv1.2"));
        assertEquals(clientHello.serverName(), "example.com");
        assertEquals(clientHello.cipherSuites(), java.util.List.of(0x1301, 0x1302, 0x1303));
        assertEquals(clientHello.alpn(), java.util.List.of("h2", "http/1.1"));
        assertTrue(clientHello.sessionIdPresent());

        TlsServerHelloReport serverHello = session.serverHello();
        assertNotNull(serverHello);
        assertEquals(serverHello.version(), "TLSv1.2");
        assertEquals(serverHello.cipherSuite(), 0xC02F);
        assertEquals(serverHello.alpn(), "h2");
        assertFalse(serverHello.sessionResumed());

        assertEquals(session.serverCertificates().size(), 2);
        TlsCertificateReport leaf = session.serverCertificates().get(0);
        assertTrue(leaf.parsed());
        assertEquals(leaf.subject(), "C=US, O=nzyme test, CN=tls-test.nzyme.local");
        assertEquals(leaf.issuer(), "C=US, O=nzyme test, CN=nzyme test CA");
        assertEquals(leaf.fingerprintSha256().length(), 64);
        assertEquals(leaf.subjectAlternativeNames().size(), 3);
        assertNotNull(leaf.notBefore());
        assertNotNull(leaf.notAfter());
        assertFalse(leaf.selfSigned());
        assertFalse(leaf.isCa());
        assertEquals(leaf.publicKeyBits(), 256L);
        assertEquals(leaf.signatureAlgorithm(), "ecdsa-with-SHA256");
        assertTrue(session.serverCertificates().get(1).isCa());
        assertTrue(session.clientCertificates().isEmpty());

        assertEquals(session.alerts().size(), 1);
        TlsAlertReport alert = session.alerts().get(0);
        assertEquals(alert.direction(), "ServerToClient");
        assertEquals(alert.level(), 1);
        assertEquals(alert.description(), 0);
        assertEquals(alert.descriptionName(), "close_notify");
    }

}
