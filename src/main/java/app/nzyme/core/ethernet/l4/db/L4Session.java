package app.nzyme.core.ethernet.l4.db;

import app.nzyme.core.ethernet.L4AddressData;
import app.nzyme.core.ethernet.L4Type;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class L4Session {

    public abstract String sessionKey();
    public abstract L4Type l4Type();
    public abstract L4AddressData source();
    public abstract L4AddressData destination();
    public abstract long bytesRxCount();
    public abstract long bytesTxCount();
    public abstract long segmentsCount();
    public abstract DateTime startTime();
    @Nullable
    public abstract DateTime endTime();
    public abstract DateTime mostRecentSegmentTime();
    public abstract long durationMs();
    public abstract String state();
    @Nullable
    public abstract String fingerprint();
    @Nullable
    public abstract List<String> tags();
    public abstract DateTime createdAt();

    public static L4Session create(String sessionKey, L4Type l4Type, L4AddressData source, L4AddressData destination, long bytesRxCount, long bytesTxCount, long segmentsCount, DateTime startTime, DateTime endTime, DateTime mostRecentSegmentTime, long durationMs, String state, String fingerprint, List<String> tags, DateTime createdAt) {
        return builder()
                .sessionKey(sessionKey)
                .l4Type(l4Type)
                .source(source)
                .destination(destination)
                .bytesRxCount(bytesRxCount)
                .bytesTxCount(bytesTxCount)
                .segmentsCount(segmentsCount)
                .startTime(startTime)
                .endTime(endTime)
                .mostRecentSegmentTime(mostRecentSegmentTime)
                .durationMs(durationMs)
                .state(state)
                .fingerprint(fingerprint)
                .tags(tags)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_L4Session.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder sessionKey(String sessionKey);

        public abstract Builder l4Type(L4Type l4Type);

        public abstract Builder source(L4AddressData source);

        public abstract Builder destination(L4AddressData destination);

        public abstract Builder bytesRxCount(long bytesRxCount);

        public abstract Builder bytesTxCount(long bytesTxCount);

        public abstract Builder segmentsCount(long segmentsCount);

        public abstract Builder startTime(DateTime startTime);

        public abstract Builder endTime(DateTime endTime);

        public abstract Builder mostRecentSegmentTime(DateTime mostRecentSegmentTime);

        public abstract Builder durationMs(long durationMs);

        public abstract Builder state(String state);

        public abstract Builder fingerprint(String fingerprint);

        public abstract Builder tags(List<String> tags);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract L4Session build();
    }
}
