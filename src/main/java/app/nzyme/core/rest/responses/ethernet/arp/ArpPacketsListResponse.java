package app.nzyme.core.rest.responses.ethernet.arp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class ArpPacketsListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("packets")
    public abstract List<ArpPacketDetailsResponse> packets();

    public static ArpPacketsListResponse create(long total, List<ArpPacketDetailsResponse> packets) {
        return builder()
                .total(total)
                .packets(packets)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ArpPacketsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder packets(List<ArpPacketDetailsResponse> packets);

        public abstract ArpPacketsListResponse build();
    }

}
