package app.nzyme.core.integrations.tenant.cot.protocol;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.google.auto.value.AutoValue;

@JacksonXmlRootElement(localName = "event")
@AutoValue
public abstract class CotEvent {

    @JacksonXmlProperty(isAttribute = true)
    public abstract String version();

    @JacksonXmlProperty(isAttribute = true)
    public abstract String uid();

    @JacksonXmlProperty(isAttribute = true)
    public abstract String how();

    @JacksonXmlProperty(isAttribute = true)
    public abstract String type();

    @JacksonXmlProperty(isAttribute = true)
    public abstract String time();

    @JacksonXmlProperty(isAttribute = true)
    public abstract String start();

    @JacksonXmlProperty(isAttribute = true)
    public abstract String stale();

    @JacksonXmlProperty(localName = "point")
    public abstract CotPoint point();

    @JacksonXmlProperty(localName = "detail")
    public abstract  CotEventDetail detail();

    public static CotEvent create(String version, String uid, String how, String type, String time, String start, String stale, CotPoint point, CotEventDetail detail) {
        return builder()
                .version(version)
                .uid(uid)
                .how(how)
                .type(type)
                .time(time)
                .start(start)
                .stale(stale)
                .point(point)
                .detail(detail)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CotEvent.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder version(String version);

        public abstract Builder uid(String uid);

        public abstract Builder how(String how);

        public abstract Builder type(String type);

        public abstract Builder time(String time);

        public abstract Builder start(String start);

        public abstract Builder stale(String stale);

        public abstract Builder point(CotPoint point);

        public abstract Builder detail(CotEventDetail detail);

        public abstract CotEvent build();
    }
}
