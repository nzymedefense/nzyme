package app.nzyme.core.integrations.tenant.cot.protocol;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.Null;

@AutoValue
public abstract class CotTrack {

    @JacksonXmlProperty(isAttribute = true)
    @Null
    public abstract Integer course();

    @JacksonXmlProperty(isAttribute = true)
    @Null
    public abstract Double speed();

    public static CotTrack create(@Null Integer course, @Null Double speed) {
        return builder()
                .course(course)
                .speed(speed)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CotTrack.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder course(@Null Integer course);

        public abstract Builder speed(@Null Double speed);

        public abstract CotTrack build();
    }

}
