package app.nzyme.core.rest.responses.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class TLSCertificatePrincipalResponse {

    @JsonProperty("alternative_names")
    public abstract List<String> alternativeNames();

    @Nullable
    @JsonProperty("cn")
    public abstract String cn();

    @Nullable
    @JsonProperty("o")
    public abstract String o();

    @Nullable
    @JsonProperty("c")
    public abstract String c();

    public static TLSCertificatePrincipalResponse create(List<String> alternativeNames, String cn, String o, String c) {
        return builder()
                .alternativeNames(alternativeNames)
                .cn(cn)
                .o(o)
                .c(c)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TLSCertificatePrincipalResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder alternativeNames(List<String> alternativeNames);

        public abstract Builder cn(String cn);

        public abstract Builder o(String o);

        public abstract Builder c(String c);

        public abstract TLSCertificatePrincipalResponse build();
    }

}
