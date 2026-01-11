package app.nzyme.core.rest.responses.ethernet.ntp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class NTPTransactionsListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("transactions")
    public abstract List<NTPTransactionDetailsResponse> transactions();

    public static NTPTransactionsListResponse create(long total, List<NTPTransactionDetailsResponse> transactions) {
        return builder()
                .total(total)
                .transactions(transactions)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_NTPTransactionsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder transactions(List<NTPTransactionDetailsResponse> transactions);

        public abstract NTPTransactionsListResponse build();
    }
}
