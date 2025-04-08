package app.nzyme.core.uav.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class ConnectUavModelListResponse {

    public abstract List<ConnectUavModelDetailsResponse> models();

    @JsonCreator
    public static ConnectUavModelListResponse create(@JsonProperty("models") List<ConnectUavModelDetailsResponse> models) {
        return builder()
                .models(models)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectUavModelListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder models(List<ConnectUavModelDetailsResponse> models);

        public abstract ConnectUavModelListResponse build();
    }
}
