package horse.wtf.nzyme.taps;

import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.rest.resources.taps.reports.ChannelReport;
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
        long tapCount = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) AS count FROM taps WHERE name = :name")
                        .bind("name", report.tapName())
                        .mapTo(Long.class)
                        .one()
        );

        DateTime now = DateTime.now();

        if (tapCount == 0) {
            LOG.info("Registering first report from new tap [{}].", report.tapName());

            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("INSERT INTO taps(name, local_time, processed_bytes_total, processed_bytes_average, " +
                            "memory_total, memory_free, memory_used, cpu_load, created_at, updated_at) " +
                            "VALUES(:name, :local_time, :processed_bytes_total, :processed_bytes_average, :memory_total, " +
                            ":memory_free, :memory_used, :cpu_load, :created_at, :updated_at)")
                            .bind("name", report.tapName())
                            .bind("local_time", report.timestamp())
                            .bind("processed_bytes_total", report.processedBytes().total())
                            .bind("processed_bytes_average", report.processedBytes().average())
                            .bind("memory_total", report.systemMetrics().memoryTotal())
                            .bind("memory_free", report.systemMetrics().memoryFree())
                            .bind("memory_used", report.systemMetrics().memoryTotal()-report.systemMetrics().memoryFree())
                            .bind("cpu_load", report.systemMetrics().cpuLoad())
                            .bind("created_at", now)
                            .bind("updated_at", now)
                            .execute()
            );
        } else {
            LOG.debug("Registering report from existing tap [{}].", report.tapName());

            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("UPDATE taps SET local_time = :local_time, " +
                            "processed_bytes_total = :processed_bytes_total, " +
                            "processed_bytes_average = :processed_bytes_average, memory_total = :memory_total, " +
                            "memory_free = :memory_free, memory_used = :memory_used, cpu_load = :cpu_load, " +
                            "updated_at = :updated_at WHERE name = :name")
                            .bind("local_time", report.timestamp())
                            .bind("processed_bytes_total", report.processedBytes().total())
                            .bind("processed_bytes_average", report.processedBytes().average())
                            .bind("memory_total", report.systemMetrics().memoryTotal())
                            .bind("memory_free", report.systemMetrics().memoryFree())
                            .bind("memory_used", report.systemMetrics().memoryTotal()-report.systemMetrics().memoryFree())
                            .bind("cpu_load", report.systemMetrics().cpuLoad())
                            .bind("updated_at", now)
                            .bind("name", report.tapName())
                            .execute()
            );
        }

        // Register bus.
        long busCount = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) AS count FROM tap_buses WHERE tap_name = :tap_name")
                        .bind("tap_name", report.tapName())
                        .mapTo(Long.class)
                        .one()
        );

        if (busCount == 0) {
            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("INSERT INTO tap_buses(tap_name, name, created_at, updated_at) " +
                            "VALUES(:tap_name, :name, :created_at, :updated_at)")
                            .bind("tap_name", report.tapName())
                            .bind("name", report.bus().name())
                            .bind("created_at", now)
                            .bind("updated_at", now)
                            .execute()
            );
        } else {
            nzyme.getDatabase().useHandle(handle ->
                    handle.createUpdate("UPDATE tap_buses SET updated_at = :updated_at WHERE tap_name = :tap_name AND name = :name")
                            .bind("tap_name", report.tapName())
                            .bind("name", report.bus().name())
                            .bind("updated_at", now)
                            .execute()
            );
        }

        Long busId = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT id FROM tap_buses WHERE tap_name = :tap_name")
                        .bind("tap_name", report.tapName())
                        .mapTo(Long.class)
                        .one()
        );

        // Register bus channels.
        for (ChannelReport channel : report.bus().channels()) {
            long channelCount = nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery("SELECT COUNT(*) AS count FROM bus_channels " +
                                    "WHERE bus_id = :bus_id AND name = :channel_name")
                            .bind("bus_id", busId)
                            .bind("channel_name", channel.name())
                            .mapTo(Long.class)
                            .one()
            );

            if (channelCount == 0) {
                nzyme.getDatabase().withHandle(handle ->
                        handle.createUpdate("INSERT INTO bus_channels(name, bus_id, capacity, watermark, errors_total, " +
                                "errors_average, throughput_bytes_total, throughput_bytes_average, " +
                                "throughput_messages_total, throughput_messages_average, created_at, updated_at) " +
                                "VALUES(:name, :bus_id, :capacity, :watermark, :errors_total, :errors_average, " +
                                ":throughput_bytes_total, :throughput_bytes_average, :throughput_messages_total, " +
                                ":throughput_messages_average, :created_at, :updated_at)")
                                .bind("name", channel.name())
                                .bind("bus_id", busId)
                                .bind("capacity", channel.capacity())
                                .bind("watermark", channel.watermark())
                                .bind("errors_total", channel.errors().total())
                                .bind("errors_average", channel.errors().average())
                                .bind("throughput_bytes_total", channel.throughputBytes().total())
                                .bind("throughput_bytes_average", channel.throughputBytes().average())
                                .bind("throughput_messages_total", channel.throughputMessages().total())
                                .bind("throughput_messages_average", channel.throughputMessages().average())
                                .bind("created_at", now)
                                .bind("updated_at", now)
                                .execute()
                );
            } else {
                nzyme.getDatabase().withHandle(handle ->
                        handle.createUpdate("UPDATE bus_channels SET capacity = :capacity, watermark = :watermark, " +
                                "errors_total = :errors_total, errors_average = :errors_average, " +
                                "throughput_bytes_total = :throughput_bytes_total, " +
                                "throughput_bytes_average = :throughput_bytes_average, " +
                                "throughput_messages_total = :throughput_messages_total, " +
                                "throughput_messages_average = :throughput_messages_average, " +
                                "updated_at = :updated_at WHERE bus_id = :bus_id AND name = :name")
                                .bind("name", channel.name())
                                .bind("bus_id", busId)
                                .bind("capacity", channel.capacity())
                                .bind("watermark", channel.watermark())
                                .bind("errors_total", channel.errors().total())
                                .bind("errors_average", channel.errors().average())
                                .bind("throughput_bytes_total", channel.throughputBytes().total())
                                .bind("throughput_bytes_average", channel.throughputBytes().average())
                                .bind("throughput_messages_total", channel.throughputMessages().total())
                                .bind("throughput_messages_average", channel.throughputMessages().average())
                                .bind("updated_at", now)
                                .execute()
                );
            }
        }
    }

    public Optional<List<Tap>> findAllTaps() {
        List<Tap> taps = nzyme.getDatabase().withHandle(handle -> handle.createQuery("SELECT * FROM taps;")
                .mapTo(Tap.class)
                .list());

        return taps == null || taps.isEmpty() ? Optional.empty() : Optional.of(taps);
    }

    public Optional<Tap> findTap(String tapName) {
        Tap tap = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM taps WHERE name = :name")
                        .bind("name", tapName)
                        .mapTo(Tap.class)
                        .first()
        );

        return tap == null ? Optional.empty() : Optional.of(tap);
    }

    public Optional<List<Bus>> findBusesOfTap(String tapName) {
        List<Bus> buses = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM tap_buses WHERE tap_name = :tap_name AND updated_at > :last_seen")
                        .bind("tap_name", tapName)
                        .bind("last_seen", DateTime.now().minusHours(1))
                        .mapTo(Bus.class)
                        .list()
        );

        return buses == null || buses.isEmpty() ? Optional.empty() : Optional.of(buses);
    }

    public Optional<List<Channel>> findChannelsOfBus(long busId) {
        List<Channel> channels = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM bus_channels WHERE bus_id = :bus_id AND updated_at > :last_seen")
                        .bind("bus_id", busId)
                        .bind("last_seen", DateTime.now().minusHours(1))
                        .mapTo(Channel.class)
                        .list()
        );

        return channels == null || channels.isEmpty() ? Optional.empty() : Optional.of(channels);
    }

}
