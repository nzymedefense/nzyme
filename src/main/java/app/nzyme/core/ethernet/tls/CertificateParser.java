package app.nzyme.core.ethernet.tls;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CertificateParser {

    private static final String OID_SUBJECT_KEY_IDENTIFIER = "2.5.29.14";
    private static final String OID_AUTHORITY_KEY_IDENTIFIER = "2.5.29.35";

    private static final Map<String, String> EXTENDED_KEY_USAGE_NAMES = Map.of(
            "1.3.6.1.5.5.7.3.1", "serverAuth",
            "1.3.6.1.5.5.7.3.2", "clientAuth",
            "1.3.6.1.5.5.7.3.3", "codeSigning",
            "1.3.6.1.5.5.7.3.4", "emailProtection",
            "1.3.6.1.5.5.7.3.8", "timeStamping",
            "1.3.6.1.5.5.7.3.9", "ocspSigning",
            "2.5.29.37.0", "anyExtendedKeyUsage"
    );

    public static CertificateDetails parse(byte[] der) {
        CertificateDetails.Builder details = CertificateDetails.builder()
                .der(der)
                .sha256(digest("SHA-256", der))
                .sha1(digest("SHA-1", der))
                .subjectAlternativeNames(List.of())
                .dnsNames(List.of())
                .extendedKeyUsage(List.of());

        X509Certificate cert;
        try {
            cert = (X509Certificate) CertificateFactory.getInstance("X.509")
                    .generateCertificate(new ByteArrayInputStream(der));
        } catch (Exception e) {
            return details.parseError(e.getClass().getSimpleName() + ": " + e.getMessage()).build();
        }

        details.certificate(cert)
                .version(cert.getVersion())
                .serial(cert.getSerialNumber().toString(16))
                .subject(cert.getSubjectX500Principal().getName(X500Principal.RFC2253))
                .issuer(cert.getIssuerX500Principal().getName(X500Principal.RFC2253))
                .commonName(commonName(cert.getSubjectX500Principal()))
                .notBefore(new DateTime(cert.getNotBefore(), DateTimeZone.UTC))
                .notAfter(new DateTime(cert.getNotAfter(), DateTimeZone.UTC))
                .isCa(cert.getBasicConstraints() >= 0)
                .selfSigned(cert.getSubjectX500Principal().equals(cert.getIssuerX500Principal()))
                .signatureAlgorithm(cert.getSigAlgName())
                .subjectKeyIdentifier(keyIdentifier(cert.getExtensionValue(OID_SUBJECT_KEY_IDENTIFIER)))
                .authorityKeyIdentifier(keyIdentifier(cert.getExtensionValue(OID_AUTHORITY_KEY_IDENTIFIER)));

        PublicKey key = cert.getPublicKey();
        details.publicKeyAlgorithm(key.getAlgorithm());
        if (key instanceof RSAPublicKey rsa) {
            details.publicKeyBits(rsa.getModulus().bitLength());
        } else if (key instanceof ECPublicKey ec) {
            details.publicKeyBits(ec.getParams().getCurve().getField().getFieldSize());
        }

        List<String> sans = Lists.newArrayList();
        List<String> dnsNames = Lists.newArrayList();
        try {
            Collection<List<?>> altNames = cert.getSubjectAlternativeNames();
            if (altNames != null) {
                for (List<?> altName : altNames) {
                    int type = (Integer) altName.get(0);
                    Object value = altName.get(1);
                    switch (type) {
                        case 1 -> sans.add("email:" + value);
                        case 2 -> {
                            sans.add("DNS:" + value);
                            dnsNames.add(value.toString().toLowerCase());
                        }
                        case 6 -> sans.add("URI:" + value);
                        case 7 -> sans.add("IP:" + value);
                        default -> { /* Not useful for analysts. */ }
                    }
                }
            }
        } catch (CertificateParsingException ignored) {
            // Malformed SAN extension. Keep the rest.
        }
        details.subjectAlternativeNames(sans).dnsNames(dnsNames);

        try {
            List<String> eku = cert.getExtendedKeyUsage();
            if (eku != null) {
                details.extendedKeyUsage(eku.stream()
                        .map(oid -> EXTENDED_KEY_USAGE_NAMES.getOrDefault(oid, oid))
                        .toList());
            }
        } catch (CertificateParsingException ignored) {
            // Malformed EKU extension. Keep the rest.
        }

        return details.build();
    }

    private static String commonName(X500Principal principal) {
        // RFC 2253 output is comma-separated with CN= attributes. Good enough without an LDAP parser.
        for (String part : principal.getName(X500Principal.RFC2253).split(",(?=(?:[^\\\\]|\\\\.)*$)")) {
            String trimmed = part.trim();
            if (trimmed.startsWith("CN=")) {
                return trimmed.substring(3);
            }
        }

        return null;
    }

    private static byte[] keyIdentifier(byte[] extensionValue) {
        if (extensionValue == null) {
            return null;
        }

        try {
            byte[] inner = derContents(extensionValue, (byte) 0x04); // Outer OCTET STRING.
            if (inner == null) {
                return null;
            }

            if (inner[0] == 0x04) {
                return derContents(inner, (byte) 0x04); // SKI.
            }

            if (inner[0] == 0x30) {
                byte[] sequence = derContents(inner, (byte) 0x30); // AKI.
                if (sequence != null && sequence.length > 0 && sequence[0] == (byte) 0x80) {
                    return derContents(sequence, (byte) 0x80);
                }
            }
        } catch (RuntimeException ignored) {
            // Unexpected encoding.
        }

        return null;
    }

    private static byte[] derContents(byte[] der, byte expectedTag) {
        if (der.length < 2 || der[0] != expectedTag) {
            return null;
        }

        int length = der[1] & 0xFF;
        int offset = 2;
        if (length > 0x80) {
            int lengthBytes = length & 0x7F;
            length = 0;
            for (int i = 0; i < lengthBytes; i++) {
                length = (length << 8) | (der[offset + i] & 0xFF);
            }
            offset += lengthBytes;
        }

        if (offset + length > der.length) {
            return null;
        }

        return Arrays.copyOfRange(der, offset, offset + length);
    }

    private static byte[] digest(String algorithm, byte[] data) {
        try {
            return MessageDigest.getInstance(algorithm).digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
