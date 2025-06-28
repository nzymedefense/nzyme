package app.nzyme.core.rest.resources.taps.reports.tables.udp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class UdpConversationsReport {

    public abstract List<UdpConversationReport> conversations();

    @JsonCreator
    public static UdpConversationsReport create(@JsonProperty("conversations") List<UdpConversationReport> conversations) {
        return builder()
                .conversations(conversations)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UdpConversationsReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder conversations(List<UdpConversationReport> conversations);

        public abstract UdpConversationsReport build();
    }
}
