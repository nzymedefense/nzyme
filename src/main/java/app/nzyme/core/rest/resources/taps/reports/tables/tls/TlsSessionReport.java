package app.nzyme.core.rest.resources.taps.reports.tables.tls;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class TlsSessionReport {

    public abstract String connectionStatus();
    @Nullable
    public abstract String sourceMac();
    @Nullable
    public abstract String destinationMac();
    public abstract String sourceAddress();
    public abstract int sourcePort();
    public abstract String destinationAddress();
    public abstract int destinationPort();
    public abstract long bytesCount();
    public abstract DateTime establishedAt();
    @Nullable
    public abstract DateTime terminatedAt();
    public abstract DateTime mostRecentSegmentTime();
    @Nullable
    public abstract TlsClientHelloReport clientHello();
    @Nullable
    public abstract TlsServerHelloReport serverHello();
    public abstract boolean helloRetryRequest();
    public abstract List<TlsCertificateReport> serverCertificates();
    public abstract List<TlsCertificateReport> clientCertificates();
    public abstract boolean clientCertificateRequested();
    public abstract List<TlsAlertReport> alerts();
    public abstract boolean clientChangeCipherSpec();
    public abstract boolean serverChangeCipherSpec();
    public abstract long clientApplicationDataRecords();
    public abstract long clientApplicationDataBytes();
    public abstract long serverApplicationDataRecords();
    public abstract long serverApplicationDataBytes();

    @JsonCreator
    public static TlsSessionReport create(@JsonProperty("connection_status") String connectionStatus,
                                          @JsonProperty("source_mac") String sourceMac,
                                          @JsonProperty("destination_mac") String destinationMac,
                                          @JsonProperty("source_address") String sourceAddress,
                                          @JsonProperty("source_port") int sourcePort,
                                          @JsonProperty("destination_address") String destinationAddress,
                                          @JsonProperty("destination_port") int destinationPort,
                                          @JsonProperty("bytes_count") long bytesCount,
                                          @JsonProperty("established_at") DateTime establishedAt,
                                          @JsonProperty("terminated_at") DateTime terminatedAt,
                                          @JsonProperty("most_recent_segment_time") DateTime mostRecentSegmentTime,
                                          @JsonProperty("client_hello") TlsClientHelloReport clientHello,
                                          @JsonProperty("server_hello") TlsServerHelloReport serverHello,
                                          @JsonProperty("hello_retry_request") boolean helloRetryRequest,
                                          @JsonProperty("server_certificates") List<TlsCertificateReport> serverCertificates,
                                          @JsonProperty("client_certificates") List<TlsCertificateReport> clientCertificates,
                                          @JsonProperty("client_certificate_requested") boolean clientCertificateRequested,
                                          @JsonProperty("alerts") List<TlsAlertReport> alerts,
                                          @JsonProperty("client_change_cipher_spec") boolean clientChangeCipherSpec,
                                          @JsonProperty("server_change_cipher_spec") boolean serverChangeCipherSpec,
                                          @JsonProperty("client_application_data_records") long clientApplicationDataRecords,
                                          @JsonProperty("client_application_data_bytes") long clientApplicationDataBytes,
                                          @JsonProperty("server_application_data_records") long serverApplicationDataRecords,
                                          @JsonProperty("server_application_data_bytes") long serverApplicationDataBytes) {
        return builder()
                .connectionStatus(connectionStatus)
                .sourceMac(sourceMac)
                .destinationMac(destinationMac)
                .sourceAddress(sourceAddress)
                .sourcePort(sourcePort)
                .destinationAddress(destinationAddress)
                .destinationPort(destinationPort)
                .bytesCount(bytesCount)
                .establishedAt(establishedAt)
                .terminatedAt(terminatedAt)
                .mostRecentSegmentTime(mostRecentSegmentTime)
                .clientHello(clientHello)
                .serverHello(serverHello)
                .helloRetryRequest(helloRetryRequest)
                .serverCertificates(serverCertificates)
                .clientCertificates(clientCertificates)
                .clientCertificateRequested(clientCertificateRequested)
                .alerts(alerts)
                .clientChangeCipherSpec(clientChangeCipherSpec)
                .serverChangeCipherSpec(serverChangeCipherSpec)
                .clientApplicationDataRecords(clientApplicationDataRecords)
                .clientApplicationDataBytes(clientApplicationDataBytes)
                .serverApplicationDataRecords(serverApplicationDataRecords)
                .serverApplicationDataBytes(serverApplicationDataBytes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TlsSessionReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder connectionStatus(String connectionStatus);

        public abstract Builder sourceMac(String sourceMac);

        public abstract Builder destinationMac(String destinationMac);

        public abstract Builder sourceAddress(String sourceAddress);

        public abstract Builder sourcePort(int sourcePort);

        public abstract Builder destinationAddress(String destinationAddress);

        public abstract Builder destinationPort(int destinationPort);

        public abstract Builder bytesCount(long bytesCount);

        public abstract Builder establishedAt(DateTime establishedAt);

        public abstract Builder terminatedAt(DateTime terminatedAt);

        public abstract Builder mostRecentSegmentTime(DateTime mostRecentSegmentTime);

        public abstract Builder clientHello(TlsClientHelloReport clientHello);

        public abstract Builder serverHello(TlsServerHelloReport serverHello);

        public abstract Builder helloRetryRequest(boolean helloRetryRequest);

        public abstract Builder serverCertificates(List<TlsCertificateReport> serverCertificates);

        public abstract Builder clientCertificates(List<TlsCertificateReport> clientCertificates);

        public abstract Builder clientCertificateRequested(boolean clientCertificateRequested);

        public abstract Builder alerts(List<TlsAlertReport> alerts);

        public abstract Builder clientChangeCipherSpec(boolean clientChangeCipherSpec);

        public abstract Builder serverChangeCipherSpec(boolean serverChangeCipherSpec);

        public abstract Builder clientApplicationDataRecords(long clientApplicationDataRecords);

        public abstract Builder clientApplicationDataBytes(long clientApplicationDataBytes);

        public abstract Builder serverApplicationDataRecords(long serverApplicationDataRecords);

        public abstract Builder serverApplicationDataBytes(long serverApplicationDataBytes);

        public abstract TlsSessionReport build();
    }
}
