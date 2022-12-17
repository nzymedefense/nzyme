package horse.wtf.nzyme.rest.responses.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.rest.responses.metrics.TimerResponse;

@AutoValue
public abstract class CryptoMetricsResponse {

    @JsonProperty("pgp_encryption_timer")
    public abstract TimerResponse pgpEncryptionTimer();

    @JsonProperty("pgp_decryption_timer")
    public abstract TimerResponse pgpDecryptionTimer();

    public static CryptoMetricsResponse create(TimerResponse pgpEncryptionTimer, TimerResponse pgpDecryptionTimer) {
        return builder()
                .pgpEncryptionTimer(pgpEncryptionTimer)
                .pgpDecryptionTimer(pgpDecryptionTimer)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CryptoMetricsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder pgpEncryptionTimer(TimerResponse pgpEncryptionTimer);

        public abstract Builder pgpDecryptionTimer(TimerResponse pgpDecryptionTimer);

        public abstract CryptoMetricsResponse build();
    }

}
