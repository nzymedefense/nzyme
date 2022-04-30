package horse.wtf.nzyme.taps;

import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.rest.resources.taps.reports.StatusReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Optional;

public class TapManager {

    private static final Logger LOG = LogManager.getLogger(TapManager.class);

    private final NzymeLeader nzyme;

    public TapManager(NzymeLeader nzyme) {
        this.nzyme = nzyme;
    }

    public void registerTapStatus(StatusReport report) {
        long count = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) AS count FROM taps WHERE name = :name")
                        .bind("name", report.tapName())
                        .mapTo(Long.class)
                        .one()
        );

        DateTime now = DateTime.now();

        if (count == 0) {
            LOG.info("Registering first report from new tap [{}].", report.tapName());

            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("INSERT INTO taps(name, local_time, processed_bytes_total, processed_bytes_10, " +
                            "memory_total, memory_free, memory_used, cpu_load, created_at, updated_at) " +
                            "VALUES(:name, :local_time, :processed_bytes_total, :processed_bytes_10, :memory_total, " +
                            ":memory_free, :memory_used, :cpu_load, :created_at, :updated_at)")
                            .bind("name", report.tapName())
                            .bind("local_time", report.timestamp())
                            .bind("processed_bytes_total", report.processedBytesTotal())
                            .bind("processed_bytes_10", report.processedBytes10())
                            .bind("memory_total", report.systemMetrics().memoryTotal())
                            .bind("memory_free", report.systemMetrics().memoryFree())
                            .bind("memory_used", report.systemMetrics().memoryTotal()-report.systemMetrics().memoryFree())
                            .bind("cpu_load", report.systemMetrics().cpuLoad())
                            .bind("created_at", now)
                            .bind("updated_at", now)
                            .execute()
            );
        } else {
            if (count > 1) {
                LOG.warn("Found multiple tap status entries for tap [{}]. This should never happen and can lead " +
                        "to inconsistencies.", report.tapName());
            }

            LOG.debug("Registering report from existing tap [{}].", report.tapName());

            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("UPDATE taps SET local_time = :local_time, " +
                            "processed_bytes_total = :processed_bytes_total, " +
                            "processed_bytes_10 = :processed_bytes_10, memory_total = :memory_total, " +
                            "memory_free = :memory_free, memory_used = :memory_used, cpu_load = :cpu_load, " +
                            "updated_at = :updated_at WHERE name = :name")
                            .bind("local_time", report.timestamp())
                            .bind("processed_bytes_total", report.processedBytesTotal())
                            .bind("processed_bytes_10", report.processedBytes10())
                            .bind("memory_total", report.systemMetrics().memoryTotal())
                            .bind("memory_free", report.systemMetrics().memoryFree())
                            .bind("memory_used", report.systemMetrics().memoryTotal()-report.systemMetrics().memoryFree())
                            .bind("cpu_load", report.systemMetrics().cpuLoad())
                            .bind("updated_at", now)
                            .bind("name", report.tapName())
                            .execute()
            );
        }

    }

    public List<Tap> findAllTaps() {
        return nzyme.getDatabase().withHandle(handle -> handle.createQuery("SELECT * FROM taps;")
                .mapTo(Tap.class)
                .list());
    }

    public Optional<Tap> findTap(String name) {
        Tap tap = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM taps WHERE name = :name")
                        .bind("name", name)
                        .mapTo(Tap.class)
                        .first()
        );

        return tap == null ? Optional.empty() : Optional.of(tap);
    }

}
