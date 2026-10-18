package app.nzyme.core.ethernet.tls.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class TLSSessionTapEntry {

    public abstract UUID tapUuid();
    @Nullable
    public abstract String clientHello();
    @Nullable
    public abstract String serverHello();
    @Nullable
    public abstract String alerts();
    public abstract List<String> serverChainSha256();
    public abstract List<String> clientChainSha256();
    public abstract boolean clientChangeCipherSpec();
    public abstract boolean serverChangeCipherSpec();
    public abstract long clientAppDataRecords();
    public abstract long clientAppDataBytes();
    public abstract long serverAppDataRecords();
    public abstract long serverAppDataBytes();
    public abstract DateTime mostRecentSegmentTime();

    public static Builder builder() {
        return new AutoValue_TLSSessionTapEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tapUuid(UUID tapUuid);
        public abstract Builder clientHello(String clientHello);
        public abstract Builder serverHello(String serverHello);
        public abstract Builder alerts(String alerts);
        public abstract Builder serverChainSha256(List<String> serverChainSha256);
        public abstract Builder clientChainSha256(List<String> clientChainSha256);
        public abstract Builder clientChangeCipherSpec(boolean clientChangeCipherSpec);
        public abstract Builder serverChangeCipherSpec(boolean serverChangeCipherSpec);
        public abstract Builder clientAppDataRecords(long clientAppDataRecords);
        public abstract Builder clientAppDataBytes(long clientAppDataBytes);
        public abstract Builder serverAppDataRecords(long serverAppDataRecords);
        public abstract Builder serverAppDataBytes(long serverAppDataBytes);
        public abstract Builder mostRecentSegmentTime(DateTime mostRecentSegmentTime);
        public abstract TLSSessionTapEntry build();
    }
}
