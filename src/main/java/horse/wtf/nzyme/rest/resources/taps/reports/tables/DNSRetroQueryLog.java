package horse.wtf.nzyme.rest.resources.taps.reports.tables;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class DNSRetroQueryLog {

    public abstract String ip();
    public abstract String server();
    public abstract String sourceMac();
    public abstract String destinationMac();
    public abstract Long port();
    public abstract String queryValue();
    public abstract String dataType();
    public abstract DateTime timestamp();

    @JsonCreator
    public static DNSRetroQueryLog create(@JsonProperty("ip") String ip,
                                          @JsonProperty("server") String server,
                                          @JsonProperty("source_mac") String sourceMac,
                                          @JsonProperty("destination_mac") String destinationMac,
                                          @JsonProperty("port") Long port,
                                          @JsonProperty("query_value") String queryValue,
                                          @JsonProperty("data_type") String dataType,
                                          @JsonProperty("timestamp") DateTime timestamp) {
        return builder()
                .ip(ip)
                .server(server)
                .sourceMac(sourceMac)
                .destinationMac(destinationMac)
                .port(port)
                .queryValue(queryValue)
                .dataType(dataType)
                .timestamp(timestamp)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DNSRetroQueryLog.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ip(String ip);

        public abstract Builder server(String server);

        public abstract Builder sourceMac(String sourceMac);

        public abstract Builder destinationMac(String destinationMac);

        public abstract Builder port(Long port);

        public abstract Builder queryValue(String queryValue);

        public abstract Builder dataType(String dataType);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract DNSRetroQueryLog build();
    }
}
