package app.nzyme.core.rest.resources.taps.reports.tables.tls;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

import java.util.List;

@AutoValue
public abstract class TlsServerHelloReport {

    public abstract String recordVersion();
    public abstract String legacyVersion();
    public abstract String version();
    public abstract int cipherSuite();
    public abstract int compressionMethod();
    public abstract List<Integer> extensions();
    @Nullable
    public abstract String alpn();
    public abstract boolean sessionResumed();

    @JsonCreator
    public static TlsServerHelloReport create(@JsonProperty("record_version") String recordVersion,
                                              @JsonProperty("legacy_version") String legacyVersion,
                                              @JsonProperty("version") String version,
                                              @JsonProperty("cipher_suite") int cipherSuite,
                                              @JsonProperty("compression_method") int compressionMethod,
                                              @JsonProperty("extensions") List<Integer> extensions,
                                              @JsonProperty("alpn") String alpn,
                                              @JsonProperty("session_resumed") boolean sessionResumed) {
        return builder()
                .recordVersion(recordVersion)
                .legacyVersion(legacyVersion)
                .version(version)
                .cipherSuite(cipherSuite)
                .compressionMethod(compressionMethod)
                .extensions(extensions)
                .alpn(alpn)
                .sessionResumed(sessionResumed)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TlsServerHelloReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder recordVersion(String recordVersion);

        public abstract Builder legacyVersion(String legacyVersion);

        public abstract Builder version(String version);

        public abstract Builder cipherSuite(int cipherSuite);

        public abstract Builder compressionMethod(int compressionMethod);

        public abstract Builder extensions(List<Integer> extensions);

        public abstract Builder alpn(String alpn);

        public abstract Builder sessionResumed(boolean sessionResumed);

        public abstract TlsServerHelloReport build();
    }
}
