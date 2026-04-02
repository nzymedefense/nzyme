package app.nzyme.core.integrations.tenant.cot.protocol;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@AutoValue
public abstract class CotEventDetail {

    @JacksonXmlProperty
    public abstract String remarks();

    @JacksonXmlProperty
    @Nullable
    public abstract CotHeight height();

    @JacksonXmlProperty
    @Nullable
    public abstract CotTrack track();

    @JacksonXmlProperty
    public abstract CotContact contact();

    @JacksonXmlProperty
    @Nullable
    public abstract CotRelation relation();

    public static CotEventDetail create(String remarks, CotHeight height, CotTrack track, CotContact contact, CotRelation relation) {
        return builder()
                .remarks(remarks)
                .height(height)
                .track(track)
                .contact(contact)
                .relation(relation)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CotEventDetail.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder remarks(String remarks);

        public abstract Builder height(CotHeight height);

        public abstract Builder track(CotTrack track);

        public abstract Builder contact(CotContact contact);

        public abstract Builder relation(CotRelation relation);

        public abstract CotEventDetail build();
    }
}
