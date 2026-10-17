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

        assertEquals(session.serverChainSha256(), java.util.List.of(
                "60e588b0f2805d5a80f41a738035765dcc14f669afd0c6b705f955387f1e5dcb",
                "cdf99a4bd3a5626bc9c1cd447ce2fea8ac8fd5da764e5f25fbf0cc9faa737f47"));
        assertTrue(session.clientChainSha256().isEmpty());

        assertEquals(report.certificates().size(), 2);
        for (java.util.Map.Entry<String, String> cert : report.certificates().entrySet()) {
            byte[] der = java.util.Base64.getDecoder().decode(cert.getValue());
            app.nzyme.core.ethernet.tls.CertificateDetails details = app.nzyme.core.ethernet.tls.CertificateParser.parse(der);
            assertTrue(details.parsed());
            assertEquals(com.google.common.io.BaseEncoding.base16().lowerCase().encode(details.sha256()), cert.getKey());
        }
        assertEquals(app.nzyme.core.ethernet.tls.CertificateParser.parse(java.util.Base64.getDecoder().decode(
                report.certificates().get(session.serverChainSha256().get(0)))).commonName(), "tls-test.nzyme.local");

        assertEquals(session.alerts().size(), 1);
        TlsAlertReport alert = session.alerts().get(0);
        assertEquals(alert.direction(), "ServerToClient");
        assertEquals(alert.level(), 1);
        assertEquals(alert.description(), 0);
        assertEquals(alert.descriptionName(), "close_notify");
    }

}
