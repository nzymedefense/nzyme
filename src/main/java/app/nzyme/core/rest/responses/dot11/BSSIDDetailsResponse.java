package app.nzyme.core.rest.responses.dot11;

import app.nzyme.core.dot11.db.BSSIDClientDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class BSSIDDetailsResponse {

    @JsonProperty("summary")
    public abstract BSSIDSummaryDetailsResponse summary();

    @JsonProperty("clients")
    public abstract List<BSSIDClientDetails> clients();

    @JsonProperty("signal_strength")
    public abstract List<TapBasedSignalStrengthResponse> signalStrength();

    @JsonProperty("data_retention_days")
    public abstract int dataRetentionDays();

    public static BSSIDDetailsResponse create(BSSIDSummaryDetailsResponse summary, List<BSSIDClientDetails> clients, List<TapBasedSignalStrengthResponse> signalStrength, int dataRetentionDays) {
        return builder()
                .summary(summary)
                .clients(clients)
                .signalStrength(signalStrength)
                .dataRetentionDays(dataRetentionDays)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BSSIDDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder summary(BSSIDSummaryDetailsResponse summary);

        public abstract Builder clients(List<BSSIDClientDetails> clients);

        public abstract Builder signalStrength(List<TapBasedSignalStrengthResponse> signalStrength);

        public abstract Builder dataRetentionDays(int dataRetentionDays);

        public abstract BSSIDDetailsResponse build();
    }
}
