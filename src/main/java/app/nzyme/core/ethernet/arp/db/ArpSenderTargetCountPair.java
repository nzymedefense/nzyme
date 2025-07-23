package app.nzyme.core.ethernet.arp.db;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ArpSenderTargetCountPair {

    public abstract String senderMac();
    public abstract String targetMac();
    public abstract long count();

    public static ArpSenderTargetCountPair create(String senderMac, String targetMac, long count) {
        return builder()
                .senderMac(senderMac)
                .targetMac(targetMac)
                .count(count)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ArpSenderTargetCountPair.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder senderMac(String senderMac);

        public abstract Builder targetMac(String targetMac);

        public abstract Builder count(long count);

        public abstract ArpSenderTargetCountPair build();
    }

}
