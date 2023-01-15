package app.nzyme.core.distributed.database;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class NodeEntry {

    public abstract UUID uuid();
    public abstract String name();
    public abstract String transportAddress();

    public abstract long memoryBytesTotal();
    public abstract long memoryBytesAvailable();
    public abstract long memoryBytesUsed();
    public abstract double cpuSystemLoad();
    public abstract int cpuThreadCount();
    public abstract DateTime processStartTime();
    public abstract long processVirtualSize();
    public abstract String processArguments();
    public abstract String osInformation();

    public abstract String version();
    public abstract DateTime lastSeen();

    public static NodeEntry create(UUID uuid, String name, String transportAddress, long memoryBytesTotal, long memoryBytesAvailable, long memoryBytesUsed, double cpuSystemLoad, int cpuThreadCount, DateTime processStartTime, long processVirtualSize, String processArguments, String osInformation, String version, DateTime lastSeen) {
        return builder()
                .uuid(uuid)
                .name(name)
                .transportAddress(transportAddress)
                .memoryBytesTotal(memoryBytesTotal)
                .memoryBytesAvailable(memoryBytesAvailable)
                .memoryBytesUsed(memoryBytesUsed)
                .cpuSystemLoad(cpuSystemLoad)
                .cpuThreadCount(cpuThreadCount)
                .processStartTime(processStartTime)
                .processVirtualSize(processVirtualSize)
                .processArguments(processArguments)
                .osInformation(osInformation)
                .version(version)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_NodeEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder name(String name);

        public abstract Builder transportAddress(String transportAddress);

        public abstract Builder memoryBytesTotal(long memoryBytesTotal);

        public abstract Builder memoryBytesAvailable(long memoryBytesAvailable);

        public abstract Builder memoryBytesUsed(long memoryBytesUsed);

        public abstract Builder cpuSystemLoad(double cpuSystemLoad);

        public abstract Builder cpuThreadCount(int cpuThreadCount);

        public abstract Builder processStartTime(DateTime processStartTime);

        public abstract Builder processVirtualSize(long processVirtualSize);

        public abstract Builder processArguments(String processArguments);

        public abstract Builder osInformation(String osInformation);

        public abstract Builder version(String version);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract NodeEntry build();
    }

}
