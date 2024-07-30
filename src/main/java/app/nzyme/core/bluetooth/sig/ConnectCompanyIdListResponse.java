package app.nzyme.core.bluetooth.sig;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class ConnectCompanyIdListResponse {

    public abstract List<ConnectCompanyIdResponse> companyIds();

    @JsonCreator
    public static ConnectCompanyIdListResponse create(@JsonProperty("company_ids") List<ConnectCompanyIdResponse> companyIds) {
        return builder()
                .companyIds(companyIds)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectCompanyIdListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder companyIds(List<ConnectCompanyIdResponse> companyIds);

        public abstract ConnectCompanyIdListResponse build();
    }
}
