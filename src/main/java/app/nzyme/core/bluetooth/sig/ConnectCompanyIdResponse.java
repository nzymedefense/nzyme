package app.nzyme.core.bluetooth.sig;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ConnectCompanyIdResponse {

    public abstract int companyId();
    public abstract String name();

    @JsonCreator
    public static ConnectCompanyIdResponse create(@JsonProperty("company_id") int companyId,
                                                  @JsonProperty("name") String name) {
        return builder()
                .companyId(companyId)
                .name(name)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectCompanyIdResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder companyId(int companyId);

        public abstract Builder name(String name);

        public abstract ConnectCompanyIdResponse build();
    }
}
