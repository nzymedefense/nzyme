package app.nzyme.core.integrations.tenant.cot.protocol;

import com.google.auto.value.AutoValue;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@AutoValue
public abstract class CotContact {

    @JacksonXmlProperty(isAttribute = true)
    public abstract String callsign();

    public static CotContact create(String callsign) {
        return builder()
                .callsign(callsign)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CotContact.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder callsign(String callsign);

        public abstract CotContact build();
    }
}
