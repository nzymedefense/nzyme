package app.nzyme.core.ethernet.tls;

import app.nzyme.core.rest.resources.taps.reports.tables.tls.TlsClientHelloReport;
import com.google.common.hash.Hashing;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * JA4 TLS client fingerprint, implemented from the JA4 specification published by FoxIO under the
 * BSD 3-Clause license. Only the JA4 client fingerprint is implemented here. The other JA4+
 * fingerprints (JA4S, JA4H, JA4X, ...) are licensed differently and are intentionally not part of this.
 */
public class JA4 {

    private static final int EXTENSION_SNI = 0x0000;
    private static final int EXTENSION_ALPN = 0x0010;
    private static final int EXTENSION_SUPPORTED_VERSIONS = 0x002b;

    private static final String EMPTY_HASH = "000000000000";

    // Highest version first.
    private static final Map<String, String> VERSION_CODES = Map.of(
            "TLSv1.3", "13",
            "TLSv1.2", "12",
            "TLSv1.1", "11",
            "TLSv1.0", "10",
            "SSLv3", "s3",
            "SSLv2", "s2"
    );
    private static final List<String> VERSION_ORDER = List.of(
            "TLSv1.3", "TLSv1.2", "TLSv1.1", "TLSv1.0", "SSLv3", "SSLv2"
    );

    @Nullable
    public static String calculate(@Nullable TlsClientHelloReport ch) {
        if (ch == null) {
            return null;
        }

        List<Integer> ciphers = withoutGrease(ch.cipherSuites());
        List<Integer> extensions = withoutGrease(ch.extensions());

        // Sessions over TCP only. QUIC ("q") and DTLS ("d") are not captured by the taps.
        return "t" + version(ch, extensions) + sni(ch, extensions)
                + count(ciphers) + count(extensions) + alpn(ch.alpn())
                + "_" + cipherHash(ciphers)
                + "_" + extensionHash(extensions, ch.signatureAlgorithms());
    }

    private static String version(TlsClientHelloReport ch, List<Integer> extensions) {
        /*
         * The supported_versions extension wins over the legacy version field. A hello that advertises
         * the extension but only unknown values (a tap-side naming gap) falls through to "00".
         */
        if (extensions.contains(EXTENSION_SUPPORTED_VERSIONS) && !ch.supportedVersions().isEmpty()) {
            for (String candidate : VERSION_ORDER) {
                if (ch.supportedVersions().contains(candidate)) {
                    return VERSION_CODES.get(candidate);
                }
            }
            return "00";
        }

        return VERSION_CODES.getOrDefault(ch.legacyVersion(), "00");
    }

    private static String sni(TlsClientHelloReport ch, List<Integer> extensions) {
        boolean hasSni = extensions.contains(EXTENSION_SNI)
                || (ch.serverName() != null && !ch.serverName().isEmpty());
        return hasSni ? "d" : "i";
    }

    private static String count(List<Integer> values) {
        return String.format(Locale.ROOT, "%02d", Math.min(values.size(), 99));
    }

    private static String alpn(List<String> alpn) {
        if (alpn == null || alpn.isEmpty() || alpn.get(0) == null || alpn.get(0).isEmpty()) {
            return "00";
        }

        byte[] first = alpn.get(0).getBytes(StandardCharsets.ISO_8859_1);
        byte head = first[0];
        byte tail = first[first.length - 1];

        if (isAlphanumeric(head) && isAlphanumeric(tail)) {
            return "" + (char) head + (char) tail;
        }

        String hex = hex(first);
        return "" + hex.charAt(0) + hex.charAt(hex.length() - 1);
    }

    private static String cipherHash(List<Integer> ciphers) {
        if (ciphers.isEmpty()) {
            return EMPTY_HASH;
        }

        return hash(ciphers.stream().sorted().map(JA4::hex4).collect(Collectors.joining(",")));
    }

    private static String extensionHash(List<Integer> extensions, List<Integer> signatureAlgorithms) {
        List<Integer> hashed = extensions.stream()
                .filter(e -> e != EXTENSION_SNI && e != EXTENSION_ALPN)
                .sorted()
                .toList();

        if (hashed.isEmpty()) {
            return EMPTY_HASH;
        }

        String raw = hashed.stream().map(JA4::hex4).collect(Collectors.joining(","));

        if (signatureAlgorithms != null && !signatureAlgorithms.isEmpty()) {
            raw += "_" + signatureAlgorithms.stream().map(JA4::hex4).collect(Collectors.joining(","));
        }

        return hash(raw);
    }

    private static List<Integer> withoutGrease(List<Integer> values) {
        if (values == null) {
            return List.of();
        }

        return values.stream().filter(v -> !isGrease(v)).toList();
    }

    static boolean isGrease(int value) {
        return (value & 0x0f0f) == 0x0a0a && ((value >> 8) & 0xff) == (value & 0xff);
    }

    private static boolean isAlphanumeric(byte b) {
        return (b >= '0' && b <= '9') || (b >= 'A' && b <= 'Z') || (b >= 'a' && b <= 'z');
    }

    private static String hash(String input) {
        return Hashing.sha256().hashString(input, StandardCharsets.UTF_8).toString().substring(0, 12);
    }

    private static String hex4(int value) {
        return String.format(Locale.ROOT, "%04x", value & 0xffff);
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format(Locale.ROOT, "%02x", b & 0xff));
        }
        return sb.toString();
    }

}
