package app.nzyme.core.ethernet.dns.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class DNSStatisticsBucket {

    public abstract DateTime bucket();
    public abstract Long requestCount();
    public abstract Long requestBytes();
    public abstract Long responseCount();
    public abstract Long responseBytes();
    public abstract Long nxdomainCount();

    public static DNSStatisticsBucket create(DateTime bucket, Long requestCount, Long requestBytes, Long responseCount, Long responseBytes, Long nxdomainCount) {
        return builder()
                .bucket(bucket)
                .requestCount(requestCount)
                .requestBytes(requestBytes)
                .responseCount(responseCount)
                .responseBytes(responseBytes)
                .nxdomainCount(nxdomainCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DNSStatisticsBucket.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

        public abstract Builder requestCount(Long requestCount);

        public abstract Builder requestBytes(Long requestBytes);

        public abstract Builder responseCount(Long responseCount);

        public abstract Builder responseBytes(Long responseBytes);

        public abstract Builder nxdomainCount(Long nxdomainCount);

        public abstract DNSStatisticsBucket build();
    }

}
