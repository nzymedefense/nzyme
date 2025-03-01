package app.nzyme.core.rest.responses.uav.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class UavCustomTypeDetailsResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("organization_id")
    public abstract UUID organizationId();

    @JsonProperty("tenant_id")
    public abstract UUID tenantId();

    @JsonProperty("match_type")
    public abstract String matchType();

    @JsonProperty("match_value")
    public abstract String matchValue();

    @JsonProperty("default_classification")
    @Nullable
    public abstract String defaultClassification();

    @JsonProperty("type")
    public abstract String type();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    public static UavCustomTypeDetailsResponse create(UUID uuid, UUID organizationId, UUID tenantId, String matchType, String matchValue, String defaultClassification, String type, String name, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .matchType(matchType)
                .matchValue(matchValue)
                .defaultClassification(defaultClassification)
                .type(type)
                .name(name)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavCustomTypeDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder matchType(String matchType);

        public abstract Builder matchValue(String matchValue);

        public abstract Builder defaultClassification(String defaultClassification);

        public abstract Builder type(String type);

        public abstract Builder name(String name);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract UavCustomTypeDetailsResponse build();
    }
}
