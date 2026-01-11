package app.nzyme.core.rest.responses.ethernet.ntp;

import app.nzyme.core.rest.responses.ethernet.EthernetMacAddressResponse;
import app.nzyme.core.rest.responses.ethernet.L4AddressResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class NTPTransactionDetailsResponse {

    @JsonProperty("transaction_key")
    public abstract String transactionKey();
    @JsonProperty("complete")
    public abstract boolean complete();
    @JsonProperty("notes")
    public abstract String notes();
    @Nullable
    @JsonProperty("client")
    public abstract L4AddressResponse client();
    @Nullable
    @JsonProperty("server")
    public abstract L4AddressResponse server();

    @Nullable
    @JsonProperty("request_size")
    public abstract Integer requestSize();
    @Nullable
    @JsonProperty("response_size")
    public abstract Integer responseSize();
    @Nullable
    @JsonProperty("timestamp_client_transmit")
    public abstract DateTime timestampClientTransmit();
    @Nullable
    @JsonProperty("timestamp_server_receive")
    public abstract DateTime timestampServerReceive();
    @Nullable
    @JsonProperty("timestamp_server_transmit")
    public abstract DateTime timestampServerTransmit();
    @Nullable
    @JsonProperty("timestamp_client_tap_receive")
    public abstract DateTime timestampClientTapReceive();
    @Nullable
    @JsonProperty("timestamp_server_tap_receive")
    public abstract DateTime timestampServerTapReceive();
    @Nullable
    @JsonProperty("server_version")
    public abstract Integer serverVersion();
    @Nullable
    @JsonProperty("client_version")
    public abstract Integer clientVersion();
    @Nullable
    @JsonProperty("server_mode")
    public abstract Integer serverMode();
    @Nullable
    @JsonProperty("client_mode")
    public abstract Integer clientMode();
    @Nullable
    @JsonProperty("stratum")
    public abstract Integer stratum();
    @Nullable
    @JsonProperty("leap_indicator")
    public abstract Integer leapIndicator();
    @Nullable
    @JsonProperty("precision")
    public abstract Long precision();
    @Nullable
    @JsonProperty("poll_interval")
    public abstract Long pollInterval();
    @Nullable
    @JsonProperty("root_delay_seconds")
    public abstract Double rootDelaySeconds();
    @Nullable
    @JsonProperty("root_dispersion_seconds")
    public abstract Double rootDispersionSeconds();
    @Nullable
    @JsonProperty("delay_seconds")
    public abstract Double delaySeconds();
    @Nullable
    @JsonProperty("offset_seconds")
    public abstract Double offsetSeconds();
    @Nullable
    @JsonProperty("rtt_seconds")
    public abstract Double rttSeconds();
    @Nullable
    @JsonProperty("server_processing_seconds")
    public abstract Double serverProcessingSeconds();
    @Nullable
    @JsonProperty("reference_id")
    public abstract String referenceId();
    @Nullable
    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    public static NTPTransactionDetailsResponse create(String transactionKey, boolean complete, String notes, L4AddressResponse client, L4AddressResponse server, Integer requestSize, Integer responseSize, DateTime timestampClientTransmit, DateTime timestampServerReceive, DateTime timestampServerTransmit, DateTime timestampClientTapReceive, DateTime timestampServerTapReceive, Integer serverVersion, Integer clientVersion, Integer serverMode, Integer clientMode, Integer stratum, Integer leapIndicator, Long precision, Long pollInterval, Double rootDelaySeconds, Double rootDispersionSeconds, Double delaySeconds, Double offsetSeconds, Double rttSeconds, Double serverProcessingSeconds, String referenceId, DateTime createdAt) {
        return builder()
                .transactionKey(transactionKey)
                .complete(complete)
                .notes(notes)
                .client(client)
                .server(server)
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
        return new AutoValue_NTPTransactionDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder transactionKey(String transactionKey);

        public abstract Builder complete(boolean complete);

        public abstract Builder notes(String notes);

        public abstract Builder client(L4AddressResponse client);

        public abstract Builder server(L4AddressResponse server);

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

        public abstract NTPTransactionDetailsResponse build();
    }
}
