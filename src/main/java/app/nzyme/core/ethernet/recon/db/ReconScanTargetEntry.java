package app.nzyme.core.ethernet.recon.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class ReconScanTargetEntry {

    public abstract long id();
    public abstract long scanId();
    public abstract String targetAddress();
    @Nullable
    public abstract Integer targetPort();
    public abstract String outcome();
    public abstract long probeCount();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();

    public static ReconScanTargetEntry create(long id, long scanId, String targetAddress, Integer targetPort, String outcome, long probeCount, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .id(id)
                .scanId(scanId)
                .targetAddress(targetAddress)
                .targetPort(targetPort)
                .outcome(outcome)
                .probeCount(probeCount)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ReconScanTargetEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);
        public abstract Builder scanId(long scanId);
        public abstract Builder targetAddress(String targetAddress);
        public abstract Builder targetPort(Integer targetPort);
        public abstract Builder outcome(String outcome);
        public abstract Builder probeCount(long probeCount);
        public abstract Builder firstSeen(DateTime firstSeen);
        public abstract Builder lastSeen(DateTime lastSeen);
        public abstract ReconScanTargetEntry build();
    }
}
