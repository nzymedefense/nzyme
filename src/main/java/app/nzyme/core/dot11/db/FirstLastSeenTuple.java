package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

@AutoValue
public abstract class FirstLastSeenTuple {

    @Nullable
    public abstract DateTime firstSeen();

    @Nullable
    public abstract DateTime lastSeen();

    public static FirstLastSeenTuple create(DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_FirstLastSeenTuple.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract FirstLastSeenTuple build();
    }
}
