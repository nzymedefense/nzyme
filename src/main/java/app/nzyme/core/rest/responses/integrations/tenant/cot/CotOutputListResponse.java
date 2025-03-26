package app.nzyme.core.rest.responses.integrations.tenant.cot;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class CotOutputListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("outputs")
    public abstract List<CotOutputDetailsResponse> outputs();

    public static CotOutputListResponse create(long count, List<CotOutputDetailsResponse> outputs) {
        return builder()
                .count(count)
                .outputs(outputs)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CotOutputListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder outputs(List<CotOutputDetailsResponse> outputs);

        public abstract CotOutputListResponse build();
    }

}
