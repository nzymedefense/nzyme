package app.nzyme.core.rest.responses.authentication.mgmt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class UserDetailsResponse {

    @JsonProperty("id")
    public abstract long id();

    @JsonProperty("organization_id")
    @Nullable
    public abstract Long organization_id();

    @JsonProperty("tenant_id")
    @Nullable
    public abstract Long tenantId();

    @JsonProperty("role_id")
    @Nullable
    public abstract Long roleId();

    @JsonProperty("email")
    public abstract String email();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    @JsonProperty("last_activity")
    @Nullable
    public abstract DateTime lastActivity();

    @JsonProperty("last_remote_ip")
    @Nullable
    public abstract String lastRemoteIp();

    @JsonProperty("last_geo_city")
    @Nullable
    public abstract String lastGeoCity();

    @JsonProperty("last_geo_country")
    @Nullable
    public abstract String lastGeoCountry();

    @JsonProperty("last_geo_asn")
    @Nullable
    public abstract String lastGeoAsn();

    @JsonProperty("permissions")
    public abstract List<String> permissions();

    @JsonProperty("allow_access_all_tenant_taps")
    public abstract boolean allowAccessAllTenantTaps();

    @JsonProperty("tap_permissions")
    public abstract List<UUID> tapPermissions();

    public static UserDetailsResponse create(long id, Long organization_id, Long tenantId, Long roleId, String email, String name, DateTime createdAt, DateTime updatedAt, DateTime lastActivity, String lastRemoteIp, String lastGeoCity, String lastGeoCountry, String lastGeoAsn, List<String> permissions, boolean allowAccessAllTenantTaps, List<UUID> tapPermissions) {
        return builder()
                .id(id)
                .organization_id(organization_id)
                .tenantId(tenantId)
                .roleId(roleId)
                .email(email)
                .name(name)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .lastActivity(lastActivity)
                .lastRemoteIp(lastRemoteIp)
                .lastGeoCity(lastGeoCity)
                .lastGeoCountry(lastGeoCountry)
                .lastGeoAsn(lastGeoAsn)
                .permissions(permissions)
                .allowAccessAllTenantTaps(allowAccessAllTenantTaps)
                .tapPermissions(tapPermissions)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UserDetailsResponse.Builder();
    }
    
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder organization_id(Long organization_id);

        public abstract Builder tenantId(Long tenantId);

        public abstract Builder roleId(Long roleId);

        public abstract Builder email(String email);

        public abstract Builder name(String name);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder lastActivity(DateTime lastActivity);

        public abstract Builder lastRemoteIp(String lastRemoteIp);

        public abstract Builder lastGeoCity(String lastGeoCity);

        public abstract Builder lastGeoCountry(String lastGeoCountry);

        public abstract Builder lastGeoAsn(String lastGeoAsn);

        public abstract Builder permissions(List<String> permissions);

        public abstract Builder allowAccessAllTenantTaps(boolean allowAccessAllTenantTaps);

        public abstract Builder tapPermissions(List<UUID> tapPermissions);

        public abstract UserDetailsResponse build();
    }
}
