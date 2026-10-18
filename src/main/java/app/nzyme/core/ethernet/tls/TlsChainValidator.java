package app.nzyme.core.ethernet.tls;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;

public class TlsChainValidator {

    private static final Logger LOG = LogManager.getLogger(TlsChainValidator.class);

    private static final int MAX_ERROR_LENGTH = 512;

    private final X509TrustManager trustManager;

    public TlsChainValidator() {
        this.trustManager = defaultTrustManager();
    }

    public Result validate(List<CertificateDetails> chain) {
        if (chain.isEmpty() || trustManager == null) {
            return Result.create(null, null);
        }

        X509Certificate[] certs = new X509Certificate[chain.size()];
        for (int i = 0; i < chain.size(); i++) {
            if (!chain.get(i).parsed()) {
                return Result.create(false, "unparseable_certificate");
            }
            certs[i] = chain.get(i).certificate();
        }

        try {
            /*
             * The JDK trust manager accepts only the TLS key exchange names as authType and rejects
             * key algorithm names such as "EC". "UNKNOWN" skips the key usage check against the
             * exchange, which is what we want for a passively observed handshake.
             */
            trustManager.checkServerTrusted(certs, "UNKNOWN");
            return Result.create(true, null);
        } catch (Exception e) {
            String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            return Result.create(false, message.length() > MAX_ERROR_LENGTH
                    ? message.substring(0, MAX_ERROR_LENGTH) : message);
        }
    }

    private static X509TrustManager defaultTrustManager() {
        try {
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init((KeyStore) null);
            for (TrustManager tm : factory.getTrustManagers()) {
                if (tm instanceof X509TrustManager x509) {
                    return x509;
                }
            }
        } catch (Exception e) {
            LOG.error("Could not initialize default trust manager. TLS chains will not be validated.", e);
        }

        return null;
    }

    @AutoValue
    public abstract static class Result {
        @Nullable
        public abstract Boolean trusted();
        @Nullable
        public abstract String error();

        public static Result create(Boolean trusted, String error) {
            return new AutoValue_TlsChainValidator_Result(trusted, error);
        }
    }

}
