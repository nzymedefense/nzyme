package app.nzyme.core.integrations.tenant.cot.protocol;

import com.google.auto.value.AutoValue;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@AutoValue
public abstract class CotHeight {

    @JacksonXmlProperty()
    public abstract Double agl();

    public static CotHeight create(Double agl) {
        return builder()
                .agl(agl)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CotHeight.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder agl(Double agl);

        public abstract CotHeight build();
    }
}
