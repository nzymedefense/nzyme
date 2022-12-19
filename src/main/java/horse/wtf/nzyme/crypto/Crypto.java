package horse.wtf.nzyme.crypto;

import app.nzyme.plugin.Database;
import com.codahale.metrics.Timer;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.util.MetricNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.*;
import org.bouncycastle.util.io.Streams;
import org.joda.time.DateTime;

import java.io.*;
import java.nio.file.Paths;
import java.security.*;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


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

    public static final String PGP_PRIVATE_KEY_NAME = "pgp_private.pgp";
    public static final String PGP_PUBLIC_KEY_NAME = "pgp_public.pgp";

    private final File cryptoDirectoryConfig;
    private final Database database;
    private final String nodeId;

    private final Timer encryptionTimer;
    private final Timer decryptionTimer;

    public Crypto(NzymeLeader nzyme) {
        this.cryptoDirectoryConfig = new File(nzyme.getConfiguration().cryptoDirectory());
        this.database = nzyme.getDatabase();
        this.nodeId = nzyme.getNodeID();

        this.encryptionTimer = nzyme.getMetrics().timer(MetricNames.PGP_ENCRYPTION_TIMING);
        this.decryptionTimer = nzyme.getMetrics().timer(MetricNames.PGP_DECRYPTION_TIMING);

        Security.addProvider(new BouncyCastleProvider());
    }

    public void initialize() throws CryptoInitializationException {
        File privateKeyLocation = Paths.get(cryptoDirectoryConfig.toString(), PGP_PRIVATE_KEY_NAME).toFile();
        File publicKeyLocation = Paths.get(cryptoDirectoryConfig.toString(), PGP_PUBLIC_KEY_NAME).toFile();

        if (!privateKeyLocation.exists() || !publicKeyLocation.exists()) {
            LOG.warn("PGP private or public key missing. Re-generating pair. This will make existing encrypted registry " +
                    "values unreadable. Please consult the nzyme documentation.");

            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
                keyPairGenerator.initialize(4096);
                KeyPair pair = keyPairGenerator.generateKeyPair();

                FileOutputStream privateOut = new FileOutputStream(privateKeyLocation);
                FileOutputStream publicOut = new FileOutputStream(publicKeyLocation);

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

                privateOut.close();
                publicOut.close();
            } catch (NoSuchAlgorithmException | NoSuchProviderException | PGPException e) {
                throw new CryptoInitializationException("Unexpected crypto provider exception when trying " +
                        "to create key.", e);
            } catch (IOException e) {
                throw new CryptoInitializationException("Could not write key file.", e);
            }
        }

        // Load Keys. Build fingerprint.
        String keySignature;
        try {
            keySignature = String.format("%016X", readPublicKey(publicKeyLocation).getKeyID());
        } catch (IOException e) {
            throw new CryptoInitializationException("Could not read key file.", e);
        } catch (PGPException e) {
            throw new CryptoInitializationException("Unexpected crypto provider exception when trying " +
                    "to read existing key.", e);
        }

        // Update DB.
        long keyCount = database.withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM crypto_keys " +
                        "WHERE node = :node AND key_type = :key_type")
                        .bind("node", nodeId)
                        .bind("key_type", KeyType.PGP)
                        .mapTo(Long.class)
                        .one()
        );

        if (keyCount > 0) {
            // Update existing key entry.
            database.useHandle(handle ->
                    handle.createUpdate("UPDATE crypto_keys SET key_signature = :key_signature, " +
                                    "created_at = :created_at WHERE key_type = :key_type AND node = :node")
                            .bind("node", nodeId)
                            .bind("key_type", KeyType.PGP)
                            .bind("key_signature", keySignature)
                            .bind("created_at", DateTime.now())
                            .execute()
            );
        } else {
            database.useHandle(handle ->
                    handle.createUpdate("INSERT INTO crypto_keys(node, key_type, key_signature, created_at) " +
                                    "VALUES(:node, :key_type, :key_signature, :created_at)")
                            .bind("node", nodeId)
                            .bind("key_type", KeyType.PGP)
                            .bind("key_signature", keySignature)
                            .bind("created_at", DateTime.now())
                            .execute()
            );
        }
    }

    public byte[] encrypt(byte[] value) throws CryptoOperationException {
        try {
            Timer.Context timer = encryptionTimer.time();
            File publicKeyLocation = Paths.get(cryptoDirectoryConfig.toString(), PGP_PUBLIC_KEY_NAME).toFile();
            PGPPublicKey publicKey = readPublicKey(publicKeyLocation);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // Write header and literal data.
            ByteArrayOutputStream literalData = new ByteArrayOutputStream();
            PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
            OutputStream literalOut = literalDataGenerator.open(literalData, PGPLiteralData.BINARY, "nzymepgp", value.length, DateTime.now().toDate());
            literalOut.write(value);
            byte[] bytes = literalData.toByteArray();
            literalDataGenerator.close();
            literalData.close();

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

            out.close();


            timer.stop();
            return out.toByteArray();
        } catch (PGPException | IOException e) {
            throw new CryptoOperationException("Cannot encrypt value.", e);
        }
    }

    public byte[] decrypt(byte[] value) throws CryptoOperationException {
        try {
            Timer.Context timer = decryptionTimer.time();
            File privateKeyLocation = Paths.get(cryptoDirectoryConfig.toString(), PGP_PRIVATE_KEY_NAME).toFile();
            InputStream dataIn = PGPUtil.getDecoderStream(new ByteArrayInputStream(value));
            InputStream keyIn = new FileInputStream(privateKeyLocation);

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
            keyIn.close();
            dataIn.close();

            timer.stop();
            return data.toByteArray();
        } catch(IOException | PGPException | IllegalArgumentException e) {
            throw new CryptoOperationException("Cannot decrypt value.", e);
        }
    }

    public List<PGPKeyFingerprint> getPGPKeysByNode() {
        return database.withHandle(handle ->
                handle.createQuery("SELECT node, key_signature, created_at " +
                        "FROM crypto_keys WHERE key_type = :key_type")
                        .bind("key_type", KeyType.PGP)
                        .mapTo(PGPKeyFingerprint.class)
                        .list()
        );
    }

    public String getLocalPGPKeyFingerprint() {
        return database.withHandle(handle ->
                handle.createQuery("SELECT key_signature FROM crypto_keys WHERE key_type = :key_type AND node = :node")
                        .bind("key_type", KeyType.PGP)
                        .bind("node", nodeId)
                        .mapTo(String.class)
                        .one()
        );
    }

    private PGPPublicKey readPublicKey(File file) throws IOException, PGPException {
        InputStream keyIn = new BufferedInputStream(new FileInputStream(file));
        PGPPublicKey pubKey = readPublicKey(keyIn);
        keyIn.close();
        return pubKey;
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

    public static final class CryptoInitializationException extends Throwable {
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
