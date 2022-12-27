package app.nzyme.core.dot11.clients;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class Client {

    @JsonProperty
    public abstract String oui();

    @JsonProperty
    public abstract String mac();

    @JsonProperty("last_seen")
    public DateTime lastSeen = new DateTime();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder oui(String oui);

        public abstract Builder mac(String mac);

        public abstract Client build();
    }

    @JsonIgnore
    public void updateLastSeen() {
        this.lastSeen = new DateTime();
    }

    public static Client create(String oui, String mac) {
        return builder()
                .oui(oui)
                .mac(mac)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Client.Builder();
    }

}
