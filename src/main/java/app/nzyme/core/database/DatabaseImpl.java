package app.nzyme.core.database;

import app.nzyme.core.configuration.node.NodeConfiguration;
import app.nzyme.plugin.Database;
import app.nzyme.core.alerts.service.AlertDatabaseEntryMapper;
import app.nzyme.core.bandits.database.*;
import app.nzyme.core.configuration.db.BaseConfigurationMapper;
import app.nzyme.core.crypto.database.PGPKeyFingerprintMapper;
import app.nzyme.core.dot11.deauth.db.DeauthenticationMonitorRecordingMapper;
import app.nzyme.core.dot11.networks.beaconrate.BeaconRateMapper;
import app.nzyme.core.dot11.networks.sentry.db.SentrySSIDMapper;
import app.nzyme.core.dot11.networks.signalstrength.SignalIndexHistogramHistoryDBEntryMapper;
import app.nzyme.core.ethernet.dns.db.DNSPairSummaryMapper;
import app.nzyme.core.ethernet.dns.db.DNSStatisticsBucketMapper;
import app.nzyme.core.ethernet.dns.db.DNSTrafficSummaryMapper;
import app.nzyme.core.events.db.EventRecordMapper;
import app.nzyme.core.measurements.mappers.MeasurementMapper;
import app.nzyme.core.reporting.db.ExecutionLogEntryMapper;
import app.nzyme.core.reporting.db.ScheduledReportEntryMapper;
import app.nzyme.core.taps.db.*;
import app.nzyme.core.taps.db.metrics.TapMetricsGaugeAggregationMapper;
import app.nzyme.core.taps.db.metrics.TapMetricsGaugeMapper;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jodatime2.JodaTimePlugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

public class DatabaseImpl implements Database {

    public static final DateTimeFormatter DATABASE_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .appendOptional( // Parse milliseconds only if they exist. (they are omitted in DB if at 0)
                    new DateTimeFormatterBuilder()
                            .appendLiteral(".")
                            .appendFractionOfSecond(0, 6).toParser()
            )
            .toFormatter().withZoneUTC();

    public static final DateTimeFormatter DEAUTH_MONITOR_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ssZ")
            .toFormatter();

    public static final DateTimeFormatter BUCKET_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .toFormatter().withZoneUTC();

    private final NodeConfiguration configuration;

    private Jdbi jdbi;

    public DatabaseImpl(NodeConfiguration configuration) {
        this.configuration = configuration;
    }

    public void initializeAndMigrate() throws LiquibaseException {
        // TODO use reflection here at some point.
        this.jdbi = Jdbi.create("jdbc:" + configuration.databasePath())
                .installPlugin(new PostgresPlugin())
                .installPlugin(new JodaTimePlugin())
                .registerRowMapper(new MeasurementMapper())
                .registerRowMapper(new BeaconRateMapper())
                .registerRowMapper(new SignalIndexHistogramHistoryDBEntryMapper())
                .registerRowMapper(new AlertDatabaseEntryMapper())
                .registerRowMapper(new BanditMapper())
                .registerRowMapper(new BanditIdentifierMapper())
                .registerRowMapper(new ContactMapper())
                .registerRowMapper(new SentrySSIDMapper())
                .registerRowMapper(new DeauthenticationMonitorRecordingMapper())
                .registerRowMapper(new EventRecordMapper())
                .registerRowMapper(new ScheduledReportEntryMapper())
                .registerRowMapper(new ExecutionLogEntryMapper())
                .registerRowMapper(new ContactRecordMapper())
                .registerRowMapper(new ContactRecordValueAggregationMapper())
                .registerRowMapper(new ContactRecorderHistogramEntryMapper())
                .registerRowMapper(new TapMapper())
                .registerRowMapper(new BaseConfigurationMapper())
                .registerRowMapper(new BusMapper())
                .registerRowMapper(new ChannelMapper())
                .registerRowMapper(new CaptureMapper())
                .registerRowMapper(new TapMetricsGaugeMapper())
                .registerRowMapper(new TapMetricsGaugeAggregationMapper())
                .registerRowMapper(new DNSStatisticsBucketMapper())
                .registerRowMapper(new DNSTrafficSummaryMapper())
                .registerRowMapper(new DNSPairSummaryMapper())
                .registerRowMapper(new PGPKeyFingerprintMapper());

        // Run migrations against underlying JDBC connection.
        JdbcConnection connection = new JdbcConnection(jdbi.open().getConnection());
        try {
            liquibase.database.Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
            Liquibase liquibase = new liquibase.Liquibase("db/migrations.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts(), new LabelExpression());
        } finally {
            connection.close();
        }
    }

    public long getTotalSize() {
        return withHandle(handle -> {
            return handle.createQuery("SELECT pg_database_size(current_database())")
                    .mapTo(Long.class)
                    .first();
        });
    }

    public <R, X extends Exception> R withHandle(HandleCallback<R, X> callback) throws X {
        return jdbi.withHandle(callback);
    }

    public <X extends Exception> void useHandle(final HandleConsumer<X> callback) throws X {
        jdbi.useHandle(callback);
    }

}
