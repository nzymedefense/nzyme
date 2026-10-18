package app.nzyme.core.rest.responses.ethernet.tls;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

import java.util.List;

@AutoValue
public abstract class TLSSessionFullDetailsResponse {

    @JsonProperty("session")
    public abstract TLSSessionDetailsResponse session();

    @Nullable
    @JsonProperty("client_hello")
    public abstract TLSClientHelloResponse clientHello();

    @Nullable
    @JsonProperty("server_hello")
    public abstract TLSServerHelloResponse serverHello();

    @JsonProperty("alerts")
    public abstract List<TLSAlertResponse> alerts();

    @JsonProperty("server_chain")
    public abstract List<TLSChainCertificateResponse> serverChain();

    @JsonProperty("client_chain")
    public abstract List<TLSChainCertificateResponse> clientChain();

    @JsonProperty("client_change_cipher_spec")
    public abstract boolean clientChangeCipherSpec();

    @JsonProperty("server_change_cipher_spec")
    public abstract boolean serverChangeCipherSpec();

    @JsonProperty("client_app_data_records")
    public abstract long clientAppDataRecords();

    @JsonProperty("client_app_data_bytes")
    public abstract long clientAppDataBytes();

    @JsonProperty("server_app_data_records")
    public abstract long serverAppDataRecords();

    @JsonProperty("server_app_data_bytes")
    public abstract long serverAppDataBytes();

    public static Builder builder() {
        return new AutoValue_TLSSessionFullDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder session(TLSSessionDetailsResponse session);
        public abstract Builder clientHello(TLSClientHelloResponse clientHello);
        public abstract Builder serverHello(TLSServerHelloResponse serverHello);
        public abstract Builder alerts(List<TLSAlertResponse> alerts);
        public abstract Builder serverChain(List<TLSChainCertificateResponse> serverChain);
        public abstract Builder clientChain(List<TLSChainCertificateResponse> clientChain);
        public abstract Builder clientChangeCipherSpec(boolean clientChangeCipherSpec);
        public abstract Builder serverChangeCipherSpec(boolean serverChangeCipherSpec);
        public abstract Builder clientAppDataRecords(long clientAppDataRecords);
        public abstract Builder clientAppDataBytes(long clientAppDataBytes);
        public abstract Builder serverAppDataRecords(long serverAppDataRecords);
        public abstract Builder serverAppDataBytes(long serverAppDataBytes);
        public abstract TLSSessionFullDetailsResponse build();
    }
}
