package app.nzyme.core.rest.resources.taps.reports.tables.ntp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.Set;

@AutoValue
public abstract class NTPTransactionReport {

    public abstract boolean complete();
    public abstract Set<String> notes();
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
    public abstract String referenceId();
    @Nullable
    public abstract Double delaySeconds();
    @Nullable
    public abstract Double offsetSeconds();
    @Nullable
    public abstract Double rttSeconds();
    @Nullable
    public abstract Double serverProcessingSeconds();

    @JsonCreator
    public static NTPTransactionReport create(@JsonProperty("complete") boolean complete,
                                              @JsonProperty("notes") Set<String> notes,
                                              @JsonProperty("client_mac") String clientMac,
                                              @JsonProperty("server_mac") String serverMac,
                                              @JsonProperty("client_address") String clientAddress,
                                              @JsonProperty("server_address") String serverAddress,
                                              @JsonProperty("client_port") Integer clientPort,
                                              @JsonProperty("server_port") Integer serverPort,
                                              @JsonProperty("request_size") Integer requestSize,
                                              @JsonProperty("response_size") Integer responseSize,
                                              @JsonProperty("timestamp_client_transmit") DateTime timestampClientTransmit,
                                              @JsonProperty("timestamp_server_receive") DateTime timestampServerReceive,
                                              @JsonProperty("timestamp_server_transmit") DateTime timestampServerTransmit,
                                              @JsonProperty("timestamp_client_tap_receive") DateTime timestampClientTapReceive,
                                              @JsonProperty("timestamp_server_tap_receive") DateTime timestampServerTapReceive,
                                              @JsonProperty("server_version") Integer serverVersion,
                                              @JsonProperty("client_version") Integer clientVersion,
                                              @JsonProperty("server_mode") Integer serverMode,
                                              @JsonProperty("client_mode") Integer clientMode,
                                              @JsonProperty("stratum") Integer stratum,
                                              @JsonProperty("leap_indicator") Integer leapIndicator,
                                              @JsonProperty("precision") Long precision,
                                              @JsonProperty("poll_interval") Long pollInterval,
                                              @JsonProperty("root_delay_seconds") Double rootDelaySeconds,
                                              @JsonProperty("root_dispersion_seconds") Double rootDispersionSeconds,
                                              @JsonProperty("reference_id") String referenceId,
                                              @JsonProperty("delay_seconds") Double delaySeconds,
                                              @JsonProperty("offset_seconds") Double offsetSeconds,
                                              @JsonProperty("rtt_seconds") Double rttSeconds,
                                              @JsonProperty("server_processing_seconds") Double serverProcessingSeconds) {
        return builder()
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
                .referenceId(referenceId)
                .delaySeconds(delaySeconds)
                .offsetSeconds(offsetSeconds)
                .rttSeconds(rttSeconds)
                .serverProcessingSeconds(serverProcessingSeconds)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_NTPTransactionReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder complete(boolean complete);

        public abstract Builder notes(Set<String> notes);

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

        public abstract Builder referenceId(String referenceId);

        public abstract Builder delaySeconds(Double delaySeconds);

        public abstract Builder offsetSeconds(Double offsetSeconds);

        public abstract Builder rttSeconds(Double rttSeconds);

        public abstract Builder serverProcessingSeconds(Double serverProcessingSeconds);

        public abstract NTPTransactionReport build();
    }
}
