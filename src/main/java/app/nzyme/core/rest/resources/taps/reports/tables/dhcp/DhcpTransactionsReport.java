package app.nzyme.core.rest.resources.taps.reports.tables.dhcp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class DhcpTransactionsReport {

    public abstract List<Dhcpv4TransactionReport> four();

    @JsonCreator
    public static DhcpTransactionsReport create(@JsonProperty("four") List<Dhcpv4TransactionReport> four) {
        return builder()
                .four(four)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DhcpTransactionsReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder four(List<Dhcpv4TransactionReport> four);

        public abstract DhcpTransactionsReport build();
    }
}
