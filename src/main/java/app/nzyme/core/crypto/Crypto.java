package app.nzyme.core.crypto;

import app.nzyme.core.crypto.database.TLSKeyAndCertificateEntry;
import app.nzyme.core.crypto.tls.TLSKeyAndCertificate;
import app.nzyme.core.crypto.tls.TLSSourceType;
import app.nzyme.core.crypto.tls.TLSUtils;
import app.nzyme.core.crypto.database.TLSWildcardKeyAndCertificateEntry;
import app.nzyme.core.crypto.tls.TLSWildcardKeyAndCertificate;
import app.nzyme.core.distributed.Node;
import app.nzyme.plugin.Database;
import com.codahale.metrics.Timer;
import app.nzyme.core.NzymeNode;
import app.nzyme.core.util.MetricNames;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.BaseEncoding;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.*;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.Streams;
import org.joda.time.DateTime;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/*
 * The readPublicKey and findPrivateKey methods are Copyright (c) 2000-2021 The Legion of the Bouncy Castle Inc.
 * (https://www.bouncycastle.org) and were copied under the terms of the MIT license:
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * /////
 *
 * Certain other code in this file is derived from similar Bouncy Castle example code, originally published
 * under the same license.
 *
 * https://github.com/bcgit/bc-java/tree/master/pg/src/main/java/org/bouncycastle/openpgp/examples
 *
 */

public class Crypto {

    private static final Logger LOG = LogManager.getLogger(Crypto.class);

    private enum KeyType {
        PGP
    }

    public static final String DEFAULT_TLS_SUBJECT_DN = "CN=nzyme";

    public static final String PGP_PRIVATE_KEY_FILE_NAME = "pgp_private.pgp";
    public static final String PGP_PUBLIC_KEY_FILE_NAME = "pgp_public.pgp";

    public static final String TLS_CERTIFICATE_FILE_NAME = "tls.cert";
    public static final String TLS_KEY_FILE_NAME = "tls.key";

    private final File cryptoDirectoryConfig;
    private final Database database;
    private final String nodeName;
    private final UUID nodeId;

    private final Timer encryptionTimer;
    private final Timer decryptionTimer;

    private final NzymeNode nzyme;

    private final BouncyCastleProvider bcProvider;

    public Crypto(NzymeNode nzyme) {
        this.nzyme = nzyme;

        this.cryptoDirectoryConfig = new File(nzyme.getConfiguration().cryptoDirectory());
        this.database = nzyme.getDatabase();
        this.nodeName = nzyme.getNodeInformation().name();
        this.nodeId = nzyme.getNodeInformation().id();

        this.encryptionTimer = nzyme.getMetrics().timer(MetricNames.PGP_ENCRYPTION_TIMING);
        this.decryptionTimer = nzyme.getMetrics().timer(MetricNames.PGP_DECRYPTION_TIMING);

        this.bcProvider = new BouncyCastleProvider();
        Security.addProvider(this.bcProvider);
    }

    public void initialize() throws CryptoInitializationException {
        this.initialize(true);
    }

    public void initialize(boolean withRetentionCleaning) throws CryptoInitializationException {
        File privateKeyLocation = Paths.get(cryptoDirectoryConfig.toString(), PGP_PRIVATE_KEY_FILE_NAME).toFile();
        File publicKeyLocation = Paths.get(cryptoDirectoryConfig.toString(), PGP_PUBLIC_KEY_FILE_NAME).toFile();

        if (!privateKeyLocation.exists() || !publicKeyLocation.exists()) {
            LOG.warn("PGP private or public key missing. Re-generating pair. This will make existing encrypted registry " +
                    "values unreadable. Please consult the nzyme documentation.");

            try (FileOutputStream privateOut = new FileOutputStream(privateKeyLocation);
                 FileOutputStream publicOut = new FileOutputStream(publicKeyLocation)) {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
                keyPairGenerator.initialize(4096);
                KeyPair pair = keyPairGenerator.generateKeyPair();

                PGPDigestCalculator shaCalc = new JcaPGPDigestCalculatorProviderBuilder()
                        .build()
                        .get(HashAlgorithmTags.SHA1);
                PGPKeyPair keyPair = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, pair, new Date());
                PGPSecretKey privateKey = new PGPSecretKey(
                        PGPSignature.DEFAULT_CERTIFICATION,
                        keyPair,
                        "nzyme-pgp",
                        shaCalc,
                        null,
                        null,
                        new JcaPGPContentSignerBuilder(
                                keyPair.getPublicKey().getAlgorithm(),
                                HashAlgorithmTags.SHA256),
                        new JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256, shaCalc)
                                .setProvider("BC")
                                .build("nzyme".toCharArray())
                );

                // Write private key.
                privateKey.encode(privateOut);

                // Write public key.
                PGPPublicKey key = privateKey.getPublicKey();
                key.encode(publicOut);
            } catch (NoSuchAlgorithmException | NoSuchProviderException | PGPException e) {
                throw new CryptoInitializationException("Unexpected crypto provider exception when trying " +
                        "to create key.", e);
            } catch (IOException e) {
                throw new CryptoInitializationException("Could not write key file.", e);
            }
        }

        // Load Keys. Build fingerprint.
        String keySignature;
        DateTime keyDate;
        try {
            PGPPublicKey pk = readPublicKey(publicKeyLocation);
            keySignature = String.format("%016X", pk.getKeyID());
            keyDate = new DateTime(pk.getCreationTime());
        } catch (IOException e) {
            throw new CryptoInitializationException("Could not read key file.", e);
        } catch (PGPException e) {
            throw new CryptoInitializationException("Unexpected crypto provider exception when trying " +
                    "to read existing key.", e);
        }

        // Does this node have keys in the database?
        List<String> signatures = database.withHandle(handle ->
                handle.createQuery("SELECT key_signature FROM crypto_keys " +
                        "WHERE node_id = :node_id AND key_type = :key_type")
                        .bind("node_id", nodeId)
                        .bind("key_type", KeyType.PGP)
                        .mapTo(String.class)
                        .list()
        );

        if (signatures.size() == 1) {
            /*
             * This node already has a key in the database. Check if it's another key than the one on disk.
             * If so, someone re-generated it, and we have to update it. If it's the same key, we can leave it
             * alone because nothing changed.
             */
            if (!signatures.contains(keySignature)) {
                database.useHandle(handle ->
                        handle.createUpdate("UPDATE crypto_keys SET key_signature = :key_signature, " +
                                        "created_at = :created_at WHERE key_type = :key_type AND node_id = :node_id")
                                .bind("node_id", nodeId)
                                .bind("key_type", KeyType.PGP)
                                .bind("key_signature", keySignature)
                                .bind("created_at", keyDate)
                                .execute()
                );
            }
        } else if(signatures.size() == 0) {
            database.useHandle(handle ->
                    handle.createUpdate("INSERT INTO crypto_keys(node_id, node_name, key_type, key_signature, created_at) " +
                                    "VALUES(:node_id, :node_name, :key_type, :key_signature, :created_at)")
                            .bind("node_id", nodeId)
                            .bind("node_name", nodeName)
                            .bind("key_type", KeyType.PGP)
                            .bind("key_signature", keySignature)
                            .bind("created_at", keyDate)
                            .execute()
            );
        } else {
            throw new CryptoInitializationException("Unexpected number of PGP keys for this node in database. Cannot continue.");
        }

        // Generate TLS certificate and key if none exist.
        if (findTLSKeyAndCertificateOfNode(nodeId).isEmpty()) {
            try {
                LOG.info("No TLS certificate found. Generating self-signed certificate.");
                TLSKeyAndCertificate tls = generateTLSCertificate(DEFAULT_TLS_SUBJECT_DN, 12);
                setTLSKeyAndCertificateOfNode(nodeId, tls);
            } catch (CryptoOperationException e) {
                throw new CryptoInitializationException("Could not generate TLS certificate.", e);
            }
        }

        if (withRetentionCleaning) {
            Executors.newSingleThreadScheduledExecutor(
                    new ThreadFactoryBuilder()
                            .setNameFormat("crypto-retentionclean-%d")
                            .setDaemon(true)
                            .build()
            ).scheduleAtFixedRate(this::retentionCleanKeys, 0, 1, TimeUnit.MINUTES);
        }
    }

    public TLSKeyAndCertificate generateTLSCertificate(String subjectDN, int validityMonths) throws CryptoOperationException {
        SecureRandom random = new SecureRandom();

        DateTime now = new DateTime();
        Date startDate = now.toDate();
        Date endDate = now.plusMonths(validityMonths).toDate();

        KeyPair keyPair;
        try {
            KeyPairGenerator keypairGen = KeyPairGenerator.getInstance("RSA", "BC");
            keypairGen.initialize(2048, random);
            keyPair = keypairGen.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new CryptoOperationException("Could not initialize key pair generator.", e);
        }

        X500Name dnName = new X500Name(subjectDN);
        BigInteger certSerialNumber = new BigInteger(Long.toString(now.getMillis()));

        String signatureAlgorithm = "SHA256WithRSA";
        ContentSigner contentSigner;
        try {
            contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).build(keyPair.getPrivate());
        } catch (OperatorCreationException e) {
            throw new CryptoOperationException("Could not initialize content signer.", e);
        }
        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                dnName,
                certSerialNumber,
                startDate,
                endDate,
                dnName,
                keyPair.getPublic()
        );

        try {
            certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, new BasicConstraints(true));
        } catch (CertIOException e) {
            throw new CryptoOperationException("Could not add extension.", e);
        }

        try {
            X509Certificate certificate = new JcaX509CertificateConverter().setProvider(this.bcProvider)
                    .getCertificate(certBuilder.build(contentSigner));

            // Create a list of only one cert. We don't have a chain for self-signed certs.
            ArrayList<X509Certificate> certificates = Lists.newArrayList();
            certificates.add(certificate);

            return TLSKeyAndCertificate.create(
                    nzyme.getNodeManager().getLocalNodeId(),
                    TLSSourceType.GENERATED_SELF_SIGNED,
                    certificates,
                    keyPair.getPrivate(),
                    TLSUtils.calculateTLSCertificateFingerprint(certificate),
                    new DateTime(certificate.getNotBefore()),
                    new DateTime(certificate.getNotAfter())
            );
        } catch (CertificateException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] encrypt(byte[] value) throws CryptoOperationException {
        try(ByteArrayOutputStream out = new ByteArrayOutputStream(); ByteArrayOutputStream literalData = new ByteArrayOutputStream();){
            Timer.Context timer = encryptionTimer.time();
            File publicKeyLocation = Paths.get(cryptoDirectoryConfig.toString(), PGP_PUBLIC_KEY_FILE_NAME).toFile();
            PGPPublicKey publicKey = readPublicKey(publicKeyLocation);

            // Write header and literal data.
            PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
            OutputStream literalOut = literalDataGenerator.open(literalData, PGPLiteralData.BINARY, "nzymepgp", value.length, DateTime.now().toDate());
            literalOut.write(value);
            byte[] bytes = literalData.toByteArray();
            literalDataGenerator.close();

            PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
                    new JcePGPDataEncryptorBuilder(PGPEncryptedData.AES_256)
                            .setWithIntegrityPacket(true)
                            .setSecureRandom(new SecureRandom())
                            .setProvider("BC")
            );

            encGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(publicKey).setProvider("BC"));

            OutputStream enc = encGen.open(out, bytes.length);
            enc.write(bytes);
            enc.close();

            timer.stop();
            return out.toByteArray();
        } catch (PGPException | IOException e) {
            throw new CryptoOperationException("Cannot encrypt value.", e);
        }
    }

    public byte[] decrypt(byte[] value) throws CryptoOperationException {
        File privateKeyLocation = Paths.get(cryptoDirectoryConfig.toString(), PGP_PRIVATE_KEY_FILE_NAME).toFile();

        try(InputStream dataIn = PGPUtil.getDecoderStream(new ByteArrayInputStream(value)); InputStream keyIn = new FileInputStream(privateKeyLocation)) {
            Timer.Context timer = decryptionTimer.time();

            JcaPGPObjectFactory pgpF = new JcaPGPObjectFactory(dataIn);
            PGPEncryptedDataList enc;

            // The first object might be a PGP marker packet.
            Object o = pgpF.nextObject();
            if (o instanceof PGPEncryptedDataList) {
                enc = (PGPEncryptedDataList) o;
            } else {
                enc = (PGPEncryptedDataList) pgpF.nextObject();
            }

            // Find the secret key.
            Iterator<PGPEncryptedData> it = enc.getEncryptedDataObjects();
            PGPPrivateKey sKey = null;
            PGPPublicKeyEncryptedData pbe = null;
            PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(
                    PGPUtil.getDecoderStream(keyIn), new JcaKeyFingerprintCalculator()
            );

            while (sKey == null && it.hasNext()) {
                pbe = (PGPPublicKeyEncryptedData) it.next();
                sKey = findSecretKey(pgpSec, pbe.getKeyID());
            }

            if (sKey == null) {
                throw new IllegalArgumentException("Secret key for message not found.");
            }

            InputStream clear = pbe.getDataStream(
                    new JcePublicKeyDataDecryptorFactoryBuilder()
                            .setProvider("BC")
                            .build(sKey)
            );
            JcaPGPObjectFactory plainFact = new JcaPGPObjectFactory(clear);
            Object message = plainFact.nextObject();
            clear.close();

            if (message instanceof PGPCompressedData) {
                PGPCompressedData cData = (PGPCompressedData) message;
                JcaPGPObjectFactory pgpFact = new JcaPGPObjectFactory(cData.getDataStream());
                message = pgpFact.nextObject();
            }

            ByteArrayOutputStream data = new ByteArrayOutputStream();
            if (message instanceof PGPLiteralData) {
                PGPLiteralData ld = (PGPLiteralData) message;
                InputStream unc = ld.getInputStream();
                Streams.pipeAll(unc, data, 8192);
                unc.close();
            } else {
                throw new PGPException("Data type unknown: " + message.getClass().getCanonicalName());
            }

            if (pbe.isIntegrityProtected() && !pbe.verify()) {
                throw new CryptoOperationException("PGP data integrity check failed.");
            }

            data.close();
            timer.stop();

            return data.toByteArray();
        } catch(IOException | PGPException | IllegalArgumentException e) {
            throw new CryptoOperationException("Cannot decrypt value.", e);
        }
    }

    public List<PGPKeyFingerprint> getPGPKeysByNode() {
        return database.withHandle(handle ->
                handle.createQuery("SELECT node_name, node_id, key_signature, created_at " +
                        "FROM crypto_keys WHERE key_type = :key_type ORDER BY node_name")
                        .bind("key_type", KeyType.PGP)
                        .mapTo(PGPKeyFingerprint.class)
                        .list()
        );
    }

    public String getLocalPGPKeyFingerprint() {
        return database.withHandle(handle ->
                handle.createQuery("SELECT key_signature FROM crypto_keys " +
                                "WHERE key_type = :key_type AND node_id = :node_id")
                        .bind("key_type", KeyType.PGP)
                        .bind("node_id", nodeId)
                        .mapTo(String.class)
                        .one()
        );
    }

    public boolean allPGPKeysEqualAcrossCluster() {
        Set<String> uniqueFingerprints = Sets.newHashSet();
        for (PGPKeyFingerprint fp : getPGPKeysByNode()) {
            uniqueFingerprints.add(fp.fingerprint());
        }

        return uniqueFingerprints.size() == 1;
    }

    public List<TLSKeyAndCertificate> getTLSCertificateByNode() {
        List<TLSKeyAndCertificateEntry> entries = database.withHandle(handle ->
                handle.createQuery("SELECT node_id, certificate, source_type, key, valid_from, expires_at " +
                                "FROM crypto_tls_certificates ORDER BY node_id DESC")
                        .mapTo(TLSKeyAndCertificateEntry.class)
                        .list()
        );

        List<TLSKeyAndCertificate> result = Lists.newArrayList();
        for (TLSKeyAndCertificateEntry entry : entries) {
            try {
                result.add(tlsKeyAndCertificateEntryToObject(entry));
            } catch (CertificateException | NoSuchAlgorithmException | InvalidKeySpecException | TLSUtils.PEMParserException e) {
                LOG.error("Could not build TLS certificate from database. Skipping.", e);
            }
        }

        return result;
    }

    public Optional<TLSKeyAndCertificate> getTLSCertificateOfNode(UUID nodeId) {
        Optional<TLSKeyAndCertificateEntry> entry = database.withHandle(handle ->
                handle.createQuery("SELECT node_id, certificate, source_type, key, valid_from, expires_at " +
                                "FROM crypto_tls_certificates WHERE node_id = :node_id")
                        .bind("node_id", nodeId)
                        .mapTo(TLSKeyAndCertificateEntry.class)
                        .findOne()
        );

        if (entry.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(tlsKeyAndCertificateEntryToObject(entry.get()));
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeySpecException | TLSUtils.PEMParserException e) {
            throw new RuntimeException("Could not build TLS certificate from database.", e);
        }
    }

    private List<TLSWildcardKeyAndCertificateEntry> getTLSWildcardCertificateEntries() {
        return database.withHandle(handle ->
                handle.createQuery("SELECT id, node_matcher, certificate, key, valid_from, expires_at, source_type " +
                                "FROM crypto_tls_certificates_wildcard ORDER BY node_matcher DESC")
                        .mapTo(TLSWildcardKeyAndCertificateEntry.class)
                        .list()
        );
    }

    public List<TLSWildcardKeyAndCertificate> getTLSWildcardCertificates() {
        List<TLSWildcardKeyAndCertificateEntry> entries = getTLSWildcardCertificateEntries();
        List<TLSWildcardKeyAndCertificate> result = Lists.newArrayList();

        for (TLSWildcardKeyAndCertificateEntry entry : entries) {
            try {
                result.add(tlsWildcardKeyAndCertificateEntryToObject(entry));
            } catch (TLSUtils.PEMParserException | CertificateException | NoSuchAlgorithmException |
                     InvalidKeySpecException e) {
                throw new RuntimeException("Could not build TLS wildcard certificate from database.");
            }
        }

        return result;
    }

    public Optional<TLSWildcardKeyAndCertificate> getTLSWildcardCertificate(long id) {
        Optional<TLSWildcardKeyAndCertificateEntry> entry = database.withHandle(handle ->
                handle.createQuery("SELECT id, node_matcher, certificate, key, valid_from, expires_at, source_type " +
                        "FROM crypto_tls_certificates_wildcard WHERE id = :id")
                        .bind("id", id)
                        .mapTo(TLSWildcardKeyAndCertificateEntry.class)
                        .findOne()
        );

        if (entry.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(tlsWildcardKeyAndCertificateEntryToObject(entry.get()));
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeySpecException |
                 TLSUtils.PEMParserException e) {
            throw new RuntimeException("Could not build TLS wildcard certificate from database.");
        }
    }

    public Map<UUID, TLSKeyAndCertificate> getTLSWildcardCertificatesForMatchingNodes() {
        List<TLSWildcardKeyAndCertificateEntry> entries = getTLSWildcardCertificateEntries();

        Map<UUID, TLSKeyAndCertificate> result = new TreeMap<>();
        List<Node> nodes = nzyme.getNodeManager().getNodes();

        for (TLSWildcardKeyAndCertificateEntry entry : entries) {
            // Find all matching nodes for this wildcard entry.
            for (Node node : nodes) {
                if (node.name().matches(entry.nodeMatcher())) {
                    try {
                        result.put(node.uuid(), tlsWildcardKeyAndCertificateEntryToNodeCertificate(entry, node));
                    } catch (TLSUtils.PEMParserException | NoSuchAlgorithmException | CertificateException |
                             InvalidKeySpecException e) {
                        throw new RuntimeException("Could not build TLS wildcard node certificate from database.");
                    }
                }
            }

        }

        return result;
    }

    public KeyStore getTLSKeyStore() {
        try {
            Optional<TLSKeyAndCertificate> diskCert = loadTLSCertificateFromDisk();

            TLSKeyAndCertificate tls;
            if (diskCert.isPresent()) {
                Optional<TLSKeyAndCertificate> existingCert = findTLSKeyAndCertificateOfNode(nodeId);

                LOG.info("Installing TLS certificate from disk");
                tls = diskCert.get();

                if (existingCert.isPresent()) {
                    updateTLSCertificateOfNode(nodeId, tls);
                } else {
                    setTLSKeyAndCertificateOfNode(nodeId, tls);
                }
            } else {
                LOG.info("Reading TLS certificate from database.");

                Optional<TLSKeyAndCertificate> tlsData = findTLSKeyAndCertificateOfNode(nodeId);
                if (tlsData.isEmpty()) {
                    throw new RuntimeException("No TLS certificate data of this node found in database.");
                } else {
                    tls = tlsData.get();
                }
            }

            List<Certificate> certChain = Lists.newArrayList();
            certChain.addAll(tls.certificates());

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setKeyEntry("key", tls.key(), "".toCharArray(), certChain.toArray(new Certificate[certChain.size()]));

            return keyStore;
        } catch(Exception e) {
            throw new RuntimeException("Could not build TLS key store.", e);
        }
    }

    private Optional<TLSKeyAndCertificate> loadTLSCertificateFromDisk() {
        File certFile = Paths.get(cryptoDirectoryConfig.toString(), TLS_CERTIFICATE_FILE_NAME).toFile();
        File keyFile = Paths.get(cryptoDirectoryConfig.toString(), TLS_KEY_FILE_NAME).toFile();

        if (certFile.exists() && keyFile.exists()) {
            try (FileInputStream certFileS = new FileInputStream(certFile);
                 FileInputStream keyFileS = new FileInputStream(keyFile)) {
                return Optional.of(
                        TLSUtils.readTLSKeyAndCertificateFromInputStreams(nodeId, TLSSourceType.FILE_LOADED, certFileS, keyFileS)
                );
            } catch (Exception e) {
                throw new RuntimeException("Could not read TLS certificate from disk.", e);
            }
        } else {
            return Optional.empty();
        }
    }

    public byte[] getTLSKeyStoreBytes() throws GeneralSecurityException, IOException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        getTLSKeyStore().store(stream, "".toCharArray());

        return stream.toByteArray();
    }

    public void updateTLSCertificateOfNode(UUID nodeId, TLSKeyAndCertificate tls) {
        String certificate;
        String key;
        try {
            certificate = TLSUtils.serializeCertificateChain(tls.certificates());
            key = BaseEncoding.base64().encode(tls.key().getEncoded());
        } catch(Exception e) {
            throw new RuntimeException("Could not encode TLS data.", e);
        }

        // We are double-encoding some things here to avoid confusion later on. It's base64, encrypted, base64 again for storage.
        String encryptedCertificate, encryptedKey;
        try {
            encryptedCertificate = BaseEncoding.base64().encode(encrypt(certificate.getBytes()));
            encryptedKey = BaseEncoding.base64().encode(encrypt(key.getBytes()));
        } catch(CryptoOperationException e) {
            throw new RuntimeException("Could not encrypt TLS certificate/key for database storage.", e);
        }

        nzyme.getDatabase().useHandle(handle ->
            handle.createUpdate("UPDATE crypto_tls_certificates SET certificate = :certificate, key = :key, " +
                            "source_type = :source_type, valid_from = :valid_from, expires_at = :expires_at " +
                            "WHERE node_id = :node_id")
                    .bind("certificate", encryptedCertificate)
                    .bind("key", encryptedKey)
                    .bind("source_type", tls.sourceType().name())
                    .bind("valid_from", tls.validFrom())
                    .bind("expires_at", tls.expiresAt())
                    .bind("node_id", nodeId)
                    .execute()
        );
    }

    public void writeTLSWildcardCertificate(TLSWildcardKeyAndCertificate tls) {
        String certificate;
        String key;
        try {
            certificate = TLSUtils.serializeCertificateChain(tls.certificates());
            key = BaseEncoding.base64().encode(tls.key().getEncoded());
        } catch(Exception e) {
            throw new RuntimeException("Could not encode TLS data.", e);
        }

        if (tls.nodeMatcher().trim().isEmpty()) {
            throw new RuntimeException("Node matcher is empty.");
        }

        // We are double-encoding some things here to avoid confusion later on. It's base64, encrypted, base64 again for storage.
        String encryptedCertificate, encryptedKey;
        try {
            encryptedCertificate = BaseEncoding.base64().encode(encrypt(certificate.getBytes()));
            encryptedKey = BaseEncoding.base64().encode(encrypt(key.getBytes()));
        } catch(CryptoOperationException e) {
            throw new RuntimeException("Could not encrypt TLS certificate/key for database storage.", e);
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO crypto_tls_certificates_wildcard(node_matcher, certificate, key, " +
                                "valid_from, expires_at, source_type) VALUES(:node_matcher, :certificate, :key, :valid_from, " +
                                ":expires_at, :source_type)")
                        .bind("node_matcher", tls.nodeMatcher())
                        .bind("certificate", encryptedCertificate)
                        .bind("key", encryptedKey)
                        .bind("source_type", tls.sourceType().name())
                        .bind("valid_from", tls.validFrom())
                        .bind("expires_at", tls.expiresAt())
                        .execute()
        );
    }

    public void replaceTLSWildcardCertificate(long certificateId, TLSWildcardKeyAndCertificate newCert) {
        String certificate;
        String key;
        try {
            certificate = TLSUtils.serializeCertificateChain(newCert.certificates());
            key = BaseEncoding.base64().encode(newCert.key().getEncoded());
        } catch(Exception e) {
            throw new RuntimeException("Could not encode TLS data.", e);
        }

        if (newCert.nodeMatcher().trim().isEmpty()) {
            throw new RuntimeException("Node matcher is empty.");
        }

        // We are double-encoding some things here to avoid confusion later on. It's base64, encrypted, base64 again for storage.
        String encryptedCertificate, encryptedKey;
        try {
            encryptedCertificate = BaseEncoding.base64().encode(encrypt(certificate.getBytes()));
            encryptedKey = BaseEncoding.base64().encode(encrypt(key.getBytes()));
        } catch(CryptoOperationException e) {
            throw new RuntimeException("Could not encrypt TLS certificate/key for database storage.", e);
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE crypto_tls_certificates_wildcard SET node_matcher = :node_matcher, " +
                        "certificate = :certificate, key = :key, valid_from = :valid_from, expires_at = :expires_at, " +
                        "source_type = :source_type WHERE id = :certificate_id")
                        .bind("node_matcher", newCert.nodeMatcher())
                        .bind("certificate", encryptedCertificate)
                        .bind("key", encryptedKey)
                        .bind("source_type", newCert.sourceType().name())
                        .bind("valid_from", newCert.validFrom())
                        .bind("expires_at", newCert.expiresAt())
                        .bind("certificate_id", certificateId)
                        .execute()
        );
    }

    public void updateTLSWildcardCertificateNodeMatcher(long certificateId, String nodeMatcher) {
        if (nodeMatcher.trim().isEmpty()) {
            throw new RuntimeException("Node matcher is empty.");
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("UPDATE crypto_tls_certificates_wildcard SET node_matcher = :node_matcher " +
                                "WHERE id = :certificate_id")
                        .bind("node_matcher", nodeMatcher)
                        .bind("certificate_id", certificateId)
                        .execute()
        );
    }

    public void deleteTLSWildcardCertificate(long certificateId) {
        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("DELETE FROM crypto_tls_certificates_wildcard WHERE id = :certificate_id")
                        .bind("certificate_id", certificateId)
                        .execute()
        );
    }

    private PGPPublicKey readPublicKey(File file) throws IOException, PGPException {
        try(InputStream keyIn = new BufferedInputStream(new FileInputStream(file))) {
            return readPublicKey(keyIn);
        }
    }

    private PGPPublicKey readPublicKey(InputStream input) throws IOException, PGPException {
        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(input), new JcaKeyFingerprintCalculator());

        Iterator keyRingIter = pgpPub.getKeyRings();
        while (keyRingIter.hasNext()) {
            PGPPublicKeyRing keyRing = (PGPPublicKeyRing)keyRingIter.next();
            Iterator keyIter = keyRing.getPublicKeys();
            while (keyIter.hasNext()) {
                PGPPublicKey key = (PGPPublicKey)keyIter.next();
                if (key.isEncryptionKey()) {
                    return key;
                }
            }
        }

        throw new IllegalArgumentException("Can't find encryption key in key ring.");
    }

    private PGPPrivateKey findSecretKey(PGPSecretKeyRingCollection pgpSec, long keyID) throws PGPException {
        PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);

        if (pgpSecKey == null) {
            return null;
        }

        return pgpSecKey.extractPrivateKey(
                new JcePBESecretKeyDecryptorBuilder()
                        .setProvider("BC")
                        .build("nzyme".toCharArray())
        );
    }

    private Optional<TLSKeyAndCertificate> findTLSKeyAndCertificateOfNode(UUID nodeId) {
        Optional<TLSKeyAndCertificateEntry> entry = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT node_id, certificate, key, source_type, valid_from, expires_at " +
                                "FROM crypto_tls_certificates WHERE node_id = :node_id")
                        .bind("node_id", nodeId)
                        .mapTo(TLSKeyAndCertificateEntry.class)
                        .findFirst()
        );

        if (entry.isEmpty()) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(tlsKeyAndCertificateEntryToObject(entry.get()));
            } catch(Exception e) {
                throw new RuntimeException("Could not read TLS data.", e);
            }
        }
    }

    private TLSKeyAndCertificate tlsKeyAndCertificateEntryToObject(TLSKeyAndCertificateEntry entry)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, TLSUtils.PEMParserException {
        String decryptedCertificate, decryptedKey;
        try {
            decryptedCertificate = new String(decrypt(entry.certificate().getBytes()));
            decryptedKey = new String(decrypt(entry.key().getBytes()));
        } catch(CryptoOperationException e) {
            throw new RuntimeException("Could not decrypt TLS certificate/key.", e);
        }

        List<X509Certificate> certificates = TLSUtils.deSerializeCertificateChain(decryptedCertificate);
        X509Certificate firstCertificate = certificates.get(0);

        PrivateKey key = TLSUtils.deserializeKey(decryptedKey);

        return TLSKeyAndCertificate.create(
                entry.nodeId(),
                entry.sourceType(),
                certificates,
                key,
                TLSUtils.calculateTLSCertificateFingerprint(firstCertificate),
                new DateTime(firstCertificate.getNotBefore()),
                new DateTime(firstCertificate.getNotAfter())
        );
    }

    private TLSWildcardKeyAndCertificate tlsWildcardKeyAndCertificateEntryToObject(TLSWildcardKeyAndCertificateEntry entry)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, TLSUtils.PEMParserException {
        String decryptedCertificate, decryptedKey;
        try {
            decryptedCertificate = new String(decrypt(entry.certificate().getBytes()));
            decryptedKey = new String(decrypt(entry.key().getBytes()));
        } catch(CryptoOperationException e) {
            throw new RuntimeException("Could not decrypt TLS certificate/key.", e);
        }

        List<X509Certificate> certificates = TLSUtils.deSerializeCertificateChain(decryptedCertificate);
        X509Certificate firstCertificate = certificates.get(0);

        PrivateKey key = TLSUtils.deserializeKey(decryptedKey);

        return TLSWildcardKeyAndCertificate.create(
                entry.id(),
                entry.nodeMatcher(),
                entry.sourceType(),
                certificates,
                key,
                TLSUtils.calculateTLSCertificateFingerprint(firstCertificate),
                new DateTime(firstCertificate.getNotBefore()),
                new DateTime(firstCertificate.getNotAfter())
        );
    }

    private TLSKeyAndCertificate tlsWildcardKeyAndCertificateEntryToNodeCertificate(TLSWildcardKeyAndCertificateEntry entry, Node node)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, TLSUtils.PEMParserException {
        String decryptedCertificate, decryptedKey;
        try {
            decryptedCertificate = new String(decrypt(entry.certificate().getBytes()));
            decryptedKey = new String(decrypt(entry.key().getBytes()));
        } catch(CryptoOperationException e) {
            throw new RuntimeException("Could not decrypt TLS certificate/key.", e);
        }

        List<X509Certificate> certificates = TLSUtils.deSerializeCertificateChain(decryptedCertificate);
        X509Certificate firstCertificate = certificates.get(0);

        PrivateKey key = TLSUtils.deserializeKey(decryptedKey);

        return TLSKeyAndCertificate.create(
                node.uuid(),
                entry.sourceType(),
                certificates,
                key,
                TLSUtils.calculateTLSCertificateFingerprint(firstCertificate),
                new DateTime(firstCertificate.getNotBefore()),
                new DateTime(firstCertificate.getNotAfter())
        );
    }

    private void setTLSKeyAndCertificateOfNode(UUID nodeId, TLSKeyAndCertificate tls) {
        String certificate;
        String key;
        try {
            certificate = TLSUtils.serializeCertificateChain(tls.certificates());
            key = BaseEncoding.base64().encode(tls.key().getEncoded());
        } catch(Exception e) {
            throw new RuntimeException("Could not encode TLS data.", e);
        }

        // We are double-encoding some things here to avoid confusion later on. It's base64, encrypted, base64 again for storage.
        String encryptedCertificate, encryptedKey;
        try {
            encryptedCertificate = BaseEncoding.base64().encode(encrypt(certificate.getBytes()));
            encryptedKey = BaseEncoding.base64().encode(encrypt(key.getBytes()));
        } catch(CryptoOperationException e) {
            throw new RuntimeException("Could not encrypt TLS certificate/key for database storage.", e);
        }

        nzyme.getDatabase().useHandle(handle ->
                handle.createUpdate("INSERT INTO crypto_tls_certificates(node_id, certificate, key, source_type, " +
                                "valid_from, expires_at) VALUES(:node_id, :certificate, :key, :source_type, " +
                                ":valid_from, :expires_at)")
                        .bind("node_id", nodeId)
                        .bind("certificate", encryptedCertificate)
                        .bind("key", encryptedKey)
                        .bind("source_type", tls.sourceType().name())
                        .bind("valid_from", tls.validFrom())
                        .bind("expires_at", tls.expiresAt())
                        .execute()
        );
    }

    private void retentionCleanKeys() {
        List<UUID> activeNodeIds = Lists.newArrayList();

        for (Node node : nzyme.getNodeManager().getNodes()) {
            if (node.lastSeen().isAfter(DateTime.now().minusMinutes(2))) {
                activeNodeIds.add(node.uuid());
            }
        }

        for (PGPKeyFingerprint fingerprint : getPGPKeysByNode()) {
            if (!activeNodeIds.contains(fingerprint.nodeId())) {
                LOG.info("Retention cleaning keys of inactive node [{}/{}].",
                        fingerprint.nodeName(), fingerprint.nodeId());
                nzyme.getDatabase().useHandle(handle ->
                        handle.createUpdate("DELETE FROM crypto_keys WHERE node_id = :node_id")
                                .bind("node_id", fingerprint.nodeId())
                                .execute()
                );
            }
        }

    }

    public static final class CryptoInitializationException extends Throwable {
        public CryptoInitializationException(String msg) {
            super(msg);
        }

        public CryptoInitializationException(String msg, Throwable e) {
            super(msg, e);
        }
    }

    public static final class CryptoOperationException extends Throwable {

        public CryptoOperationException(String msg) {
            super(msg);
        }

        public CryptoOperationException(String msg, Throwable e) {
            super(msg, e);
        }
    }

}
