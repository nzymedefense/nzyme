package app.nzyme.core.integrations.tenant.cot.protocol;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class CotEventDetail {

    @JacksonXmlProperty(localName = "remarks")
    public abstract String remarks();

    @JacksonXmlProperty(localName = "contact")
    public abstract CotContact contact();

    public static CotEventDetail create(String remarks, CotContact contact) {
        return builder()
                .remarks(remarks)
                .contact(contact)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CotEventDetail.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder remarks(String remarks);

        public abstract Builder contact(CotContact contact);

        public abstract CotEventDetail build();
    }
}
