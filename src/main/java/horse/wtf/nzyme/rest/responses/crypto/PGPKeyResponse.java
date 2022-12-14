package horse.wtf.nzyme.rest.responses.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class PGPKeyResponse {

    @JsonProperty("node")
    public abstract String node();

    @JsonProperty("fingerprint")
    public abstract String fingerprint();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    public static PGPKeyResponse create(String node, String fingerprint, DateTime createdAt) {
        return builder()
                .node(node)
                .fingerprint(fingerprint)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PGPKeyResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder node(String node);

        public abstract Builder fingerprint(String fingerprint);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract PGPKeyResponse build();
    }

}
