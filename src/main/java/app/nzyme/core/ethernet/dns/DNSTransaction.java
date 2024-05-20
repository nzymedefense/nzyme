package app.nzyme.core.ethernet.dns;

import app.nzyme.core.ethernet.dns.db.DNSLogEntry;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class DNSTransaction {

    public abstract DNSLogEntry query();
    public abstract List<DNSLogEntry> responses();

    public static DNSTransaction create(DNSLogEntry query, List<DNSLogEntry> responses) {
        return builder()
                .query(query)
                .responses(responses)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DNSTransaction.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder query(DNSLogEntry query);

        public abstract Builder responses(List<DNSLogEntry> responses);

        public abstract DNSTransaction build();
    }
}
