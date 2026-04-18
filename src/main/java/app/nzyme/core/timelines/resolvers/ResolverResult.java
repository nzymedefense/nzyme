package app.nzyme.core.timelines.resolvers;

import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class ResolverResult {

    public abstract Map<String, Object> payload();

    public static ResolverResult create(Map<String, Object> payload) {
        return builder()
                .payload(payload)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ResolverResult.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder payload(Map<String, Object> payload);

        public abstract ResolverResult build();
    }
}
