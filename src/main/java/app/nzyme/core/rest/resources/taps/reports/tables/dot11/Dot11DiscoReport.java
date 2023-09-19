package app.nzyme.core.rest.resources.taps.reports.tables.dot11;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class Dot11DiscoReport {

    public abstract Map<String, Dot11DiscoTransmitterReport> deauthentication();
    public abstract Map<String, Dot11DiscoTransmitterReport> disassociation();

    @JsonCreator
    public static Dot11DiscoReport create(@JsonProperty("deauth") Map<String, Dot11DiscoTransmitterReport> deauthentication,
                                          @JsonProperty("disassoc") Map<String, Dot11DiscoTransmitterReport> disassociation) {
        return builder()
                .deauthentication(deauthentication)
                .disassociation(disassociation)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11DiscoReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder deauthentication(Map<String, Dot11DiscoTransmitterReport> deauthentication);

        public abstract Builder disassociation(Map<String, Dot11DiscoTransmitterReport> disassociation);

        public abstract Dot11DiscoReport build();
    }
}
