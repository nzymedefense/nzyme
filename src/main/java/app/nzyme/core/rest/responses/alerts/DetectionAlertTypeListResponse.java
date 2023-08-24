package app.nzyme.core.rest.responses.alerts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class DetectionAlertTypeListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("types")
    public abstract List<DetectionAlertTypeDetailsResponse> types();

    public static DetectionAlertTypeListResponse create(long total, List<DetectionAlertTypeDetailsResponse> types) {
        return builder()
                .total(total)
                .types(types)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DetectionAlertTypeListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder types(List<DetectionAlertTypeDetailsResponse> types);

        public abstract DetectionAlertTypeListResponse build();
    }
}
