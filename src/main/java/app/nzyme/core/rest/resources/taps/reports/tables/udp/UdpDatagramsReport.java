package app.nzyme.core.rest.resources.taps.reports.tables.udp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class UdpDatagramsReport {

    public abstract List<UdpDatagramReport> datagrams();

    @JsonCreator
    public static UdpDatagramsReport create(@JsonProperty("datagrams") List<UdpDatagramReport> datagrams) {
        return builder()
                .datagrams(datagrams)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UdpDatagramsReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder datagrams(List<UdpDatagramReport> datagrams);

        public abstract UdpDatagramsReport build();
    }

}
