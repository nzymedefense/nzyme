package horse.wtf.nzyme.crypto;

import app.nzyme.plugin.Database;
import horse.wtf.nzyme.NzymeLeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.*;
import org.joda.time.DateTime;

import java.io.*;
import java.nio.file.Paths;
import java.security.*;
import java.util.Date;
import java.util.Iterator;

public class Crypto {

    private static final Logger LOG = LogManager.getLogger(Crypto.class);

    private enum KeyType {
        PGP
    }

    private final File cryptoKeyDirectoryConfig;
    private final Database database;
    private final String nodeId;

    public Crypto(NzymeLeader nzyme) {
        this.cryptoKeyDirectoryConfig = new File(nzyme.getConfiguration().cryptoKeyDirectory());
        this.database = nzyme.getDatabase();
        this.nodeId = nzyme.getNodeID();

        Security.addProvider(new BouncyCastleProvider());
    }

    public void initialize() throws CryptoInitializationException {
        File secretKeyLocation = Paths.get(cryptoKeyDirectoryConfig.toString(), "secret.asc").toFile();
        File publicKeyLocation = Paths.get(cryptoKeyDirectoryConfig.toString(), "public.asc").toFile();

        if (!secretKeyLocation.exists() || !publicKeyLocation.exists()) {
            LOG.warn("PGP secret or public key missing. Re-generating pair. This will make existing encrypted registry " +
                    "values unreadable. Please consult the nzyme documentation.");

            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
                keyPairGenerator.initialize(4096);
                KeyPair pair = keyPairGenerator.generateKeyPair();

                FileOutputStream secretOut = new FileOutputStream(secretKeyLocation);
                ArmoredOutputStream armoredSecretOut = new ArmoredOutputStream(secretOut);
                FileOutputStream publicOut = new FileOutputStream(publicKeyLocation);
                ArmoredOutputStream armoredPublicOut = new ArmoredOutputStream(publicOut);

                PGPDigestCalculator shaCalc = new JcaPGPDigestCalculatorProviderBuilder()
                        .build()
                        .get(HashAlgorithmTags.SHA1);
                PGPKeyPair keyPair = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, pair, new Date());
                PGPSecretKey secretKey = new PGPSecretKey(
                        PGPSignature.DEFAULT_CERTIFICATION,
                        keyPair,
                        "nzyme-leader",
                        shaCalc,
                        null,
                        null,
                        new JcaPGPContentSignerBuilder(
                                keyPair.getPublicKey().getAlgorithm(),
                                HashAlgorithmTags.SHA256),
                        new JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256, shaCalc)
                                .setProvider("BC")
                                .build("".toCharArray())
                );

                // Write secret key.
                secretKey.encode(armoredSecretOut);

                // Write public key.
                PGPPublicKey key = secretKey.getPublicKey();
                key.encode(armoredPublicOut);

                armoredSecretOut.close();
                armoredPublicOut.close();
                secretOut.close();
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

    private PGPPublicKey readPublicKey(File file) throws IOException, PGPException
    {
        InputStream keyIn = new BufferedInputStream(new FileInputStream(file));
        PGPPublicKey pubKey = readPublicKey(keyIn);
        keyIn.close();
        return pubKey;
    }

    static PGPPublicKey readPublicKey(InputStream input) throws IOException, PGPException
    {
        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(
                PGPUtil.getDecoderStream(input), new JcaKeyFingerprintCalculator());

        //
        // we just loop through the collection till we find a key suitable for encryption, in the real
        // world you would probably want to be a bit smarter about this.
        //

        Iterator keyRingIter = pgpPub.getKeyRings();
        while (keyRingIter.hasNext())
        {
            PGPPublicKeyRing keyRing = (PGPPublicKeyRing)keyRingIter.next();

            Iterator keyIter = keyRing.getPublicKeys();
            while (keyIter.hasNext())
            {
                PGPPublicKey key = (PGPPublicKey)keyIter.next();

                if (key.isEncryptionKey())
                {
                    return key;
                }
            }
        }

        throw new IllegalArgumentException("Can't find encryption key in key ring.");
    }

    public static final class CryptoInitializationException extends Throwable {
        public CryptoInitializationException(String msg, Throwable e) {
            super(msg, e);
        }
    }

}
