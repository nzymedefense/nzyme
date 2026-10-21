package app.nzyme.core.rest.responses.ethernet.beacons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class BeaconExclusionMatchedBeaconResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("type")
    public abstract String type();

    @JsonProperty("destination")
    public abstract String destination();

    @JsonProperty("matching_sources")
    public abstract int matchingSources();

    @JsonProperty("total_sources")
    public abstract int totalSources();

    @JsonProperty("complete")
    public abstract boolean complete();

    public static BeaconExclusionMatchedBeaconResponse create(UUID uuid, String name, String type, String destination, int matchingSources, int totalSources, boolean complete) {
        return builder()
                .uuid(uuid)
                .name(name)
                .type(type)
                .destination(destination)
                .matchingSources(matchingSources)
                .totalSources(totalSources)
                .complete(complete)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconExclusionMatchedBeaconResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);
        public abstract Builder name(String name);
        public abstract Builder type(String type);
        public abstract Builder destination(String destination);
        public abstract Builder matchingSources(int matchingSources);
        public abstract Builder totalSources(int totalSources);
        public abstract Builder complete(boolean complete);
        public abstract BeaconExclusionMatchedBeaconResponse build();
    }

}
