package app.nzyme.core.crypto.tls;

import app.nzyme.core.rest.resources.system.CryptoResource;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.joda.time.DateTime;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TLSUtils {

    private static final Pattern CERT_BASE64_BLOCKS = Pattern.compile(
            "-+BEGIN\\s+.*CERTIFICATE[^-]*-+(?:\\s|\\r|\\n)+" +
                    "([a-z0-9+/=\\r\\n]+)" +
                    "-+END\\s+.*CERTIFICATE[^-]*-+",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern KEY_BASE64_BLOCK = Pattern.compile(
            "-+BEGIN\\s+.*PRIVATE\\s+KEY[^-]*-+(?:\\s|\\r|\\n)+" +
                    "([a-z0-9+/=\\r\\n]+)" +
                    "-+END\\s+.*PRIVATE\\s+KEY[^-]*-+",
            Pattern.CASE_INSENSITIVE);

    public static List<X509Certificate> readCertificateChainFromPEM(String pem) throws PEMParserException {
        final Matcher m = CERT_BASE64_BLOCKS.matcher(pem);

        final List<X509Certificate> certs = Lists.newArrayList();
        int pos = 0;

        while (m.find(pos)) {
            byte[] bytes = BaseEncoding.base64().decode(
                    CharMatcher
                            .breakingWhitespace()
                            .removeFrom(m.group(1))
            );

            try {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                certs.add((X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(bytes)));
            } catch(Exception e) {
                throw new PEMParserException("Could not construct certificate.", e);
            }

            pos = m.end();
        }

        if (certs.isEmpty()) {
            throw new PEMParserException("Could not find any certificates in certificate file.");
        }

        return certs;
    }

    public static PrivateKey readKeyFromPEM(String pem) throws PEMParserException {
        final Matcher m = KEY_BASE64_BLOCK.matcher(pem);

        if (!m.find()) {
            throw new PEMParserException("No key found in data.");
        }

        byte[] bytes = BaseEncoding.base64().decode(
                CharMatcher
                        .breakingWhitespace()
                        .removeFrom(m.group(1))
        );

        try {
            try {
                return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(bytes));
            } catch (InvalidKeySpecException ignore) {
                try {
                    return KeyFactory.getInstance("ECDSA").generatePrivate(new PKCS8EncodedKeySpec(bytes));
                } catch (InvalidKeySpecException e) {
                    throw new PEMParserException("Could not construct private key. Must be RSA or ECDSA.", e);
                }
            }
        } catch(Exception e) {
            throw new PEMParserException("Could not construct private key.", e);
        }
    }

    public static PrivateKey deserializeKey(String base64) throws PEMParserException {
        byte[] bytes = BaseEncoding.base64().decode(base64);

        try {
            try {
                return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(bytes));
            } catch (InvalidKeySpecException ignore) {
                try {
                    return KeyFactory.getInstance("ECDSA").generatePrivate(new PKCS8EncodedKeySpec(bytes));
                } catch (InvalidKeySpecException e) {
                    throw new PEMParserException("Could not construct private key. Must be RSA or ECDSA.", e);
                }
            }
        } catch(Exception e) {
            throw new PEMParserException("Could not construct private key.", e);
        }
    }

    public static String serializeCertificateChain(List<X509Certificate> certificates) throws CertificateEncodingException {
        List<String> strings = Lists.newArrayList();
        for (Certificate certificate : certificates) {
            strings.add(BaseEncoding.base64().encode(certificate.getEncoded()));
        }

        return Joiner.on(",").join(strings);
    }

    public static List<X509Certificate> deSerializeCertificateChain(String serialized)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {
        List<X509Certificate> certificates = Lists.newArrayList();

        for (String s : Splitter.on(",").split(serialized)) {
            byte[] certBytes = BaseEncoding.base64().decode(s);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            certificates.add((X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes)));
        }

        return certificates;
    }

    public static String calculateTLSCertificateFingerprint(X509Certificate certificate) throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(certificate.getEncoded());
        return DatatypeConverter.printHexBinary(md.digest()).toLowerCase();
    }

    public static TLSKeyAndCertificate readTLSKeyAndCertificateFromInputStreams(UUID nodeId,
                                                                                TLSSourceType sourceType,
                                                                                InputStream certificate,
                                                                                InputStream privateKey) throws TLSCertificateCreationException {
        String certificateInput, keyInput;
        try {
            certificateInput = new String(certificate.readAllBytes());
            keyInput = new String(privateKey.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException("Could not read provided TLS certificate form data.", e);
        }

        List<X509Certificate> certificates;
        PrivateKey key;
        try {
            certificates = TLSUtils.readCertificateChainFromPEM(certificateInput);
            key = TLSUtils.readKeyFromPEM(keyInput);
        } catch(Exception e) {
            throw new TLSCertificateCreationException("Could not build key/certificate from provided data.", e);
        }

        // We have a valid certificate and key from here on. Serialize to Base64.
        X509Certificate firstCert = certificates.get(0);
        String fingerprint;

        try {
            fingerprint = TLSUtils.calculateTLSCertificateFingerprint(firstCert);
        } catch (NoSuchAlgorithmException | CertificateEncodingException e) {
            throw new TLSCertificateCreationException("Could not build certificate fingerprint.", e);
        }

        return TLSKeyAndCertificate.create(
                nodeId,
                sourceType,
                certificates,
                key,
                fingerprint,
                new DateTime(firstCert.getNotBefore()),
                new DateTime(firstCert.getNotAfter())
        );
    }

    public static TLSWildcardKeyAndCertificate readTLSWildcardKeyAndCertificateFromInputStreams(String nodeMatcher,
                                                                                                TLSSourceType sourceType,
                                                                                                InputStream certificate,
                                                                                                InputStream privateKey)
            throws TLSCertificateCreationException {
        String certificateInput, keyInput;
        try {
            certificateInput = new String(certificate.readAllBytes());
            keyInput = new String(privateKey.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException("Could not read provided TLS certificate form data.", e);
        }

        List<X509Certificate> certificates;
        PrivateKey key;
        try {
            certificates = TLSUtils.readCertificateChainFromPEM(certificateInput);
            key = TLSUtils.readKeyFromPEM(keyInput);
        } catch(Exception e) {
            throw new TLSCertificateCreationException("Could not build key/certificate from provided data.", e);
        }

        // We have a valid certificate and key from here on. Serialize to Base64.
        X509Certificate firstCert = certificates.get(0);
        String fingerprint;

        try {
            fingerprint = TLSUtils.calculateTLSCertificateFingerprint(firstCert);
        } catch (NoSuchAlgorithmException | CertificateEncodingException e) {
            throw new TLSCertificateCreationException("Could not build certificate fingerprint.", e);
        }

        return TLSWildcardKeyAndCertificate.create(
                null,
                nodeMatcher,
                sourceType,
                certificates,
                key,
                fingerprint,
                new DateTime(firstCert.getNotBefore()),
                new DateTime(firstCert.getNotAfter())
        );
    }


    public static final class PEMParserException extends Exception {

        public PEMParserException(String msg) {
            super(msg);
        }

        public PEMParserException(String msg, Throwable t) {
            super(msg, t);
        }

    }

    public static final class TLSCertificateCreationException extends Exception {

        public TLSCertificateCreationException(String msg, Throwable t) {
            super(msg, t);
        }

    }

}
