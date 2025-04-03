package app.nzyme.core.integrations.tenant.cot.protocol;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class CotRelation {

    @JacksonXmlProperty(isAttribute = true)
    public abstract String type();

    @JacksonXmlProperty(isAttribute = true)
    public abstract String uid();

    public static CotRelation create(String type, String uid) {
        return builder()
                .type(type)
                .uid(uid)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CotRelation.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder type(String type);

        public abstract Builder uid(String uid);

        public abstract CotRelation build();
    }
}
