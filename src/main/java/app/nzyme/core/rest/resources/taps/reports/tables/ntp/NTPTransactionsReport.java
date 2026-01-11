package app.nzyme.core.rest.resources.taps.reports.tables.ntp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class NTPTransactionsReport {

    public abstract List<NTPTransactionReport> transactions();

    @JsonCreator
    public static NTPTransactionsReport create(@JsonProperty("transactions") List<NTPTransactionReport> transactions) {
        return builder()
                .transactions(transactions)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_NTPTransactionsReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder transactions(List<NTPTransactionReport> transactions);

        public abstract NTPTransactionsReport build();
    }

}
