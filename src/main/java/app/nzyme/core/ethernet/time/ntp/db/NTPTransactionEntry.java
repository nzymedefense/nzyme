package app.nzyme.core.ethernet.time.ntp.db;

import app.nzyme.core.rest.constraints.UUID;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class NTPTransactionEntry {

    public abstract String transactionKey();
    public abstract boolean complete();
    public abstract String notes();
    @Nullable
    public abstract String clientMac();
    @Nullable
    public abstract String serverMac();
    public abstract String clientAddress();
    public abstract String serverAddress();
    public abstract Integer clientPort();
    public abstract Integer serverPort();

    @Nullable
    public abstract Integer requestSize();
    @Nullable
    public abstract Integer responseSize();
    @Nullable
    public abstract DateTime timestampClientTransmit();
    @Nullable
    public abstract DateTime timestampServerReceive();
    @Nullable
    public abstract DateTime timestampServerTransmit();
    @Nullable
    public abstract DateTime timestampClientTapReceive();
    @Nullable
    public abstract DateTime timestampServerTapReceive();
    @Nullable
    public abstract Integer serverVersion();
    @Nullable
    public abstract Integer clientVersion();
    @Nullable
    public abstract Integer serverMode();
    @Nullable
    public abstract Integer clientMode();
    @Nullable
    public abstract Integer stratum();
    @Nullable
    public abstract Integer leapIndicator();
    @Nullable
    public abstract Long precision();
    @Nullable
    public abstract Long pollInterval();
    @Nullable
    public abstract Double rootDelaySeconds();
    @Nullable
    public abstract Double rootDispersionSeconds();
    @Nullable
    public abstract Double delaySeconds();
    @Nullable
    public abstract Double offsetSeconds();
    @Nullable
    public abstract Double rttSeconds();
    @Nullable
    public abstract Double serverProcessingSeconds();
    @Nullable
    public abstract String referenceId();
    @Nullable
    public abstract DateTime createdAt();

    public static NTPTransactionEntry create(String transactionKey, boolean complete, String notes, String clientMac, String serverMac, String clientAddress, String serverAddress, Integer clientPort, Integer serverPort, Integer requestSize, Integer responseSize, DateTime timestampClientTransmit, DateTime timestampServerReceive, DateTime timestampServerTransmit, DateTime timestampClientTapReceive, DateTime timestampServerTapReceive, Integer serverVersion, Integer clientVersion, Integer serverMode, Integer clientMode, Integer stratum, Integer leapIndicator, Long precision, Long pollInterval, Double rootDelaySeconds, Double rootDispersionSeconds, Double delaySeconds, Double offsetSeconds, Double rttSeconds, Double serverProcessingSeconds, String referenceId, DateTime createdAt) {
        return builder()
                .transactionKey(transactionKey)
                .complete(complete)
                .notes(notes)
                .clientMac(clientMac)
                .serverMac(serverMac)
                .clientAddress(clientAddress)
                .serverAddress(serverAddress)
                .clientPort(clientPort)
                .serverPort(serverPort)
                .requestSize(requestSize)
                .responseSize(responseSize)
                .timestampClientTransmit(timestampClientTransmit)
                .timestampServerReceive(timestampServerReceive)
                .timestampServerTransmit(timestampServerTransmit)
                .timestampClientTapReceive(timestampClientTapReceive)
                .timestampServerTapReceive(timestampServerTapReceive)
                .serverVersion(serverVersion)
                .clientVersion(clientVersion)
                .serverMode(serverMode)
                .clientMode(clientMode)
                .stratum(stratum)
                .leapIndicator(leapIndicator)
                .precision(precision)
                .pollInterval(pollInterval)
                .rootDelaySeconds(rootDelaySeconds)
                .rootDispersionSeconds(rootDispersionSeconds)
                .delaySeconds(delaySeconds)
                .offsetSeconds(offsetSeconds)
                .rttSeconds(rttSeconds)
                .serverProcessingSeconds(serverProcessingSeconds)
                .referenceId(referenceId)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_NTPTransactionEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder transactionKey(String transactionKey);

        public abstract Builder complete(boolean complete);

        public abstract Builder notes(String notes);

        public abstract Builder clientMac(String clientMac);

        public abstract Builder serverMac(String serverMac);

        public abstract Builder clientAddress(String clientAddress);

        public abstract Builder serverAddress(String serverAddress);

        public abstract Builder clientPort(Integer clientPort);

        public abstract Builder serverPort(Integer serverPort);

        public abstract Builder requestSize(Integer requestSize);

        public abstract Builder responseSize(Integer responseSize);

        public abstract Builder timestampClientTransmit(DateTime timestampClientTransmit);

        public abstract Builder timestampServerReceive(DateTime timestampServerReceive);

        public abstract Builder timestampServerTransmit(DateTime timestampServerTransmit);

        public abstract Builder timestampClientTapReceive(DateTime timestampClientTapReceive);

        public abstract Builder timestampServerTapReceive(DateTime timestampServerTapReceive);

        public abstract Builder serverVersion(Integer serverVersion);

        public abstract Builder clientVersion(Integer clientVersion);

        public abstract Builder serverMode(Integer serverMode);

        public abstract Builder clientMode(Integer clientMode);

        public abstract Builder stratum(Integer stratum);

        public abstract Builder leapIndicator(Integer leapIndicator);

        public abstract Builder precision(Long precision);

        public abstract Builder pollInterval(Long pollInterval);

        public abstract Builder rootDelaySeconds(Double rootDelaySeconds);

        public abstract Builder rootDispersionSeconds(Double rootDispersionSeconds);

        public abstract Builder delaySeconds(Double delaySeconds);

        public abstract Builder offsetSeconds(Double offsetSeconds);

        public abstract Builder rttSeconds(Double rttSeconds);

        public abstract Builder serverProcessingSeconds(Double serverProcessingSeconds);

        public abstract Builder referenceId(String referenceId);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract NTPTransactionEntry build();
    }
}
