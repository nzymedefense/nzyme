package app.nzyme.core.rest.responses.ethernet.dhcp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class DHCPTransactionsListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("transactions")
    public abstract List<DHCPTransactionDetailsResponse> transactions();

    public static DHCPTransactionsListResponse create(long total, List<DHCPTransactionDetailsResponse> transactions) {
        return builder()
                .total(total)
                .transactions(transactions)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DHCPTransactionsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder transactions(List<DHCPTransactionDetailsResponse> transactions);

        public abstract DHCPTransactionsListResponse build();
    }
}
