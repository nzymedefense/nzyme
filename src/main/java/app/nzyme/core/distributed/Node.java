package app.nzyme.core.distributed;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.net.URI;
import java.util.UUID;

@AutoValue
public abstract class Node {

    public abstract UUID uuid();
    public abstract String name();
    public abstract URI httpListenUri();
    public abstract URI httpExternalUri();
    public abstract long memoryBytesTotal();
    public abstract long memoryBytesAvailable();
    public abstract long memoryBytesUsed();
    public abstract long heapBytesTotal();
    public abstract long heapBytesAvailable();
    public abstract long heapBytesUsed();
    public abstract double cpuSystemLoad();
    public abstract int cpuThreadCount();
    public abstract DateTime processStartTime();
    public abstract long processVirtualSize();
    public abstract String processArguments();
    public abstract String osInformation();
    public abstract String version();
    public abstract DateTime lastSeen();
    public abstract DateTime clock();
    public abstract Long clockDriftMs();
    public abstract boolean isEphemeral();
    public abstract boolean deleted();

    public static Node create(UUID uuid, String name, URI httpListenUri, URI httpExternalUri, long memoryBytesTotal, long memoryBytesAvailable, long memoryBytesUsed, long heapBytesTotal, long heapBytesAvailable, long heapBytesUsed, double cpuSystemLoad, int cpuThreadCount, DateTime processStartTime, long processVirtualSize, String processArguments, String osInformation, String version, DateTime lastSeen, DateTime clock, Long clockDriftMs, boolean isEphemeral, boolean deleted) {
        return builder()
                .uuid(uuid)
                .name(name)
                .httpListenUri(httpListenUri)
                .httpExternalUri(httpExternalUri)
                .memoryBytesTotal(memoryBytesTotal)
                .memoryBytesAvailable(memoryBytesAvailable)
                .memoryBytesUsed(memoryBytesUsed)
                .heapBytesTotal(heapBytesTotal)
                .heapBytesAvailable(heapBytesAvailable)
                .heapBytesUsed(heapBytesUsed)
                .cpuSystemLoad(cpuSystemLoad)
                .cpuThreadCount(cpuThreadCount)
                .processStartTime(processStartTime)
                .processVirtualSize(processVirtualSize)
                .processArguments(processArguments)
                .osInformation(osInformation)
                .version(version)
                .lastSeen(lastSeen)
                .clock(clock)
                .clockDriftMs(clockDriftMs)
                .isEphemeral(isEphemeral)
                .deleted(deleted)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Node.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder name(String name);

        public abstract Builder httpListenUri(URI httpListenUri);

        public abstract Builder httpExternalUri(URI httpExternalUri);

        public abstract Builder memoryBytesTotal(long memoryBytesTotal);

        public abstract Builder memoryBytesAvailable(long memoryBytesAvailable);

        public abstract Builder memoryBytesUsed(long memoryBytesUsed);

        public abstract Builder heapBytesTotal(long heapBytesTotal);

        public abstract Builder heapBytesAvailable(long heapBytesAvailable);

        public abstract Builder heapBytesUsed(long heapBytesUsed);

        public abstract Builder cpuSystemLoad(double cpuSystemLoad);

        public abstract Builder cpuThreadCount(int cpuThreadCount);

        public abstract Builder processStartTime(DateTime processStartTime);

        public abstract Builder processVirtualSize(long processVirtualSize);

        public abstract Builder processArguments(String processArguments);

        public abstract Builder osInformation(String osInformation);

        public abstract Builder version(String version);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder clock(DateTime clock);

        public abstract Builder clockDriftMs(Long clockDriftMs);

        public abstract Builder isEphemeral(boolean isEphemeral);

        public abstract Builder deleted(boolean deleted);

        public abstract Node build();
    }

}
