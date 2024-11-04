package app.nzyme.core.database;

import app.nzyme.core.bluetooth.db.BluetoothDeviceEntryMapper;
import app.nzyme.core.bluetooth.db.BluetoothDeviceSummaryMapper;
import app.nzyme.core.configuration.node.NodeConfiguration;
import app.nzyme.core.context.db.MacAddressContextEntryMapper;
import app.nzyme.core.context.db.MacAddressTransparentContextEntryMapper;
import app.nzyme.core.crypto.database.TLSKeyAndCertificateEntryMapper;
import app.nzyme.core.crypto.database.TLSWildcardKeyAndCertificateEntryMapper;
import app.nzyme.core.database.generic.NumberBucketAggregationResultMapper;
import app.nzyme.core.detection.alerts.db.DetectionAlertAttributeEntryMapper;
import app.nzyme.core.detection.alerts.db.DetectionAlertEntryMapper;
import app.nzyme.core.detection.alerts.db.DetectionAlertTimelineEntryMapper;
import app.nzyme.core.distributed.database.NodeEntryMapper;
import app.nzyme.core.distributed.database.metrics.GaugeHistogramBucketMapper;
import app.nzyme.core.distributed.database.metrics.TimerSnapshotMapper;
import app.nzyme.core.distributed.messaging.postgres.PostgresMessageEntryMapper;
import app.nzyme.core.distributed.tasksqueue.postgres.PostgresTasksQueueEntryMapper;
import app.nzyme.core.dot11.db.*;
import app.nzyme.core.dot11.db.monitoring.*;
import app.nzyme.core.dot11.db.monitoring.probereq.MonitoredProbeRequestEntryMapper;
import app.nzyme.core.dot11.tracks.db.TrackDetectorConfigMapper;
import app.nzyme.core.ethernet.dns.db.*;
import app.nzyme.core.ethernet.socks.db.SocksTunnelEntryMapper;
import app.nzyme.core.ethernet.ssh.db.SSHSessionEntryMapper;
import app.nzyme.core.ethernet.tcp.db.TcpSessionEntryMapper;
import app.nzyme.core.events.db.EventActionEntryMapper;
import app.nzyme.core.events.db.EventEntryMapper;
import app.nzyme.core.events.db.SubscriptionEntryMapper;
import app.nzyme.core.floorplans.db.TenantLocationEntryMapper;
import app.nzyme.core.floorplans.db.TenantLocationFloorEntryMapper;
import app.nzyme.core.monitoring.TimerEntryMapper;
import app.nzyme.core.monitoring.health.db.IndicatorStatusMapper;
import app.nzyme.core.registry.RegistryEntryMapper;
import app.nzyme.core.security.authentication.db.OrganizationEntryMapper;
import app.nzyme.core.security.authentication.db.TapPermissionEntryMapper;
import app.nzyme.core.security.authentication.db.TenantEntryMapper;
import app.nzyme.core.security.authentication.db.UserEntryMapper;
import app.nzyme.core.security.sessions.db.SessionEntryMapper;
import app.nzyme.core.security.sessions.db.SessionEntryWithUserDetailsMapper;
import app.nzyme.core.shared.db.GenericIntegerHistogramEntryMapper;
import app.nzyme.core.shared.db.TapBasedSignalStrengthResultMapper;
import app.nzyme.core.taps.db.metrics.Dot11FrequencyAndChannelWidthEntryMapper;
import app.nzyme.core.taps.db.metrics.TapMetricsTimerMapper;
import app.nzyme.plugin.Database;
import app.nzyme.core.crypto.database.PGPKeyFingerprintMapper;
import app.nzyme.core.taps.db.*;
import app.nzyme.core.taps.db.metrics.TapMetricsAggregationMapper;
import app.nzyme.core.taps.db.metrics.TapMetricsGaugeMapper;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.ConnectionException;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.jodatime2.JodaTimePlugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.joda.time.DateTime;

import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;

public class DatabaseImpl implements Database {

    private static final Logger LOG = LogManager.getLogger(DatabaseImpl.class);

    private final NodeConfiguration configuration;

    private Jdbi jdbi;

    public DatabaseImpl(NodeConfiguration configuration) {
        this.configuration = configuration;
    }

    public void initialize() throws LiquibaseException {
        // TODO use reflection here at some point.
        this.jdbi = Jdbi.create("jdbc:" + configuration.databasePath())
                .installPlugin(new PostgresPlugin())
                .installPlugin(new JodaTimePlugin())
                .registerRowMapper(new TapMapper())
                .registerRowMapper(new BusMapper())
                .registerRowMapper(new ChannelMapper())
                .registerRowMapper(new CaptureMapper())
                .registerRowMapper(new TapMetricsGaugeMapper())
                .registerRowMapper(new TapMetricsAggregationMapper())
                .registerRowMapper(new DNSStatisticsBucketMapper())
                .registerRowMapper(new DNSTrafficSummaryMapper())
                .registerRowMapper(new DNSPairSummaryMapper())
                .registerRowMapper(new PGPKeyFingerprintMapper())
                .registerRowMapper(new NodeEntryMapper())
                .registerRowMapper(new GaugeHistogramBucketMapper())
                .registerRowMapper(new TimerSnapshotMapper())
                .registerRowMapper(new IndicatorStatusMapper())
                .registerRowMapper(new TLSKeyAndCertificateEntryMapper())
                .registerRowMapper(new TLSWildcardKeyAndCertificateEntryMapper())
                .registerRowMapper(new PostgresMessageEntryMapper())
                .registerRowMapper(new PostgresTasksQueueEntryMapper())
                .registerRowMapper(new OrganizationEntryMapper())
                .registerRowMapper(new TenantEntryMapper())
                .registerRowMapper(new UserEntryMapper())
                .registerRowMapper(new TapPermissionEntryMapper())
                .registerRowMapper(new SessionEntryMapper())
                .registerRowMapper(new SessionEntryWithUserDetailsMapper())
                .registerRowMapper(new RegistryEntryMapper())
                .registerRowMapper(new EventEntryMapper())
                .registerRowMapper(new EventActionEntryMapper())
                .registerRowMapper(new SubscriptionEntryMapper())
                .registerRowMapper(new BSSIDSummaryMapper())
                .registerRowMapper(new SSIDChannelDetailsMapper())
                .registerRowMapper(new BSSIDAndSSIDCountHistogramEntryMapper())
                .registerRowMapper(new SSIDDetailsMapper())
                .registerRowMapper(new Dot11AdvertisementHistogramEntryMapper())
                .registerRowMapper(new SignalTrackHistogramEntryMapper())
                .registerRowMapper(new ActiveChannelMapper())
                .registerRowMapper(new ConnectedClientDetailsMapper())
                .registerRowMapper(new DisconnectedClientDetailsMapper())
                .registerRowMapper(new ClientHistogramEntryMapper())
                .registerRowMapper(new ClientActivityHistogramEntryMapper())
                .registerRowMapper(new FirstLastSeenTupleMapper())
                .registerRowMapper(new MonitoredSSIDMapper())
                .registerRowMapper(new MonitoredBSSIDMapper())
                .registerRowMapper(new MonitoredFingerprintMapper())
                .registerRowMapper(new MonitoredChannelMapper())
                .registerRowMapper(new MonitoredSecuritySuiteMapper())
                .registerRowMapper(new BSSIDWithTapMapper())
                .registerRowMapper(new DetectionAlertEntryMapper())
                .registerRowMapper(new DetectionAlertAttributeEntryMapper())
                .registerRowMapper(new DetectionAlertTimelineEntryMapper())
                .registerRowMapper(new TrackDetectorConfigMapper())
                .registerRowMapper(new CustomBanditDescriptionMapper())
                .registerRowMapper(new DiscoHistogramEntryMapper())
                .registerRowMapper(new CustomBanditDescriptionMapper())
                .registerRowMapper(new BSSIDFrameCountMapper())
                .registerRowMapper(new BSSIDPairFrameCountMapper())
                .registerRowMapper(new MacAddressContextEntryMapper())
                .registerRowMapper(new TapBasedSignalStrengthResultMapper())
                .registerRowMapper(new RestrictedSSIDSubstringMapper())
                .registerRowMapper(new ClientSignalStrengthResultMapper())
                .registerRowMapper(new TenantLocationEntryMapper())
                .registerRowMapper(new TenantLocationFloorEntryMapper())
                .registerRowMapper(new TapBasedSignalStrengthResultHistogramEntryMapper())
                .registerRowMapper(new TapMetricsTimerMapper())
                .registerRowMapper(new Dot11FrequencyAndChannelWidthEntryMapper())
                .registerRowMapper(new TcpSessionEntryMapper())
                .registerRowMapper(new DNSEntropyLogEntryMapper())
                .registerRowMapper(new DNSLogEntryMapper())
                .registerRowMapper(new SocksTunnelEntryMapper())
                .registerRowMapper(new SSHSessionEntryMapper())
                .registerRowMapper(new NumberBucketAggregationResultMapper())
                .registerRowMapper(new TimerEntryMapper())
                .registerRowMapper(new BluetoothDeviceEntryMapper())
                .registerRowMapper(new BluetoothDeviceSummaryMapper())
                .registerRowMapper(new GenericIntegerHistogramEntryMapper())
                .registerRowMapper(new MonitoredProbeRequestEntryMapper())
                .registerRowMapper(new MacAddressTransparentContextEntryMapper())
                .registerRowMapper(new SSIDWithOrganizationAndTenantMapper())
                .registerRowMapper(new Dot11KnownNetworkMapper())
                .registerRowMapper(new Dot11KnownClientMapper());

        if (configuration.slowQueryLogThreshold().isPresent()) {
            LOG.info("Slow query log enabled with threshold <{}ms>.", configuration.slowQueryLogThreshold().get());

            this.jdbi.setSqlLogger(new SqlLogger() {
                @Override
                public void logAfterExecution(StatementContext context) {
                    if (context.getElapsedTime(ChronoUnit.MILLIS) > configuration.slowQueryLogThreshold().get()) {
                        LOG.info("Slow query: <{}ms> [{}]",
                                context.getElapsedTime(ChronoUnit.MILLIS), context.getParsedSql().getSql());
                    }
                }
            });
        }

        // Try to establish connection, retry if connection fails.
        JdbcConnection connection;
        while (true) {
            try {
                connection = new JdbcConnection(jdbi.open().getConnection());
                break;
            } catch (ConnectionException e) {
                LOG.warn("Could not connect to PostgreSQL. Retrying.", e);

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        Liquibase liquibase = null;
        try {
            liquibase.database.Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
            liquibase = new liquibase.Liquibase("db/migrations.xml", new ClassLoaderResourceAccessor(), database);

            if (!liquibase.listUnrunChangeSets(new Contexts(), new LabelExpression()).isEmpty()) {
                throw new RuntimeException("There are un-run database changesets. Please run migrations.");
            }
        } finally {
            if (liquibase != null) {
                liquibase.close();
            }
            if (!connection.isClosed()) {
                connection.close();
            }
        }
    }

    public void migrate() throws LiquibaseException {
        Jdbi migrationJdbi = Jdbi.create("jdbc:" + configuration.databasePath())
                .installPlugin(new PostgresPlugin());

        JdbcConnection migrationConnection;

        // Try to establish connection, retry if connection fails.
        while (true) {
            try {
                migrationConnection = new JdbcConnection(migrationJdbi.open().getConnection());
                break;
            } catch (ConnectionException e) {
                LOG.warn("Could not connect to PostgreSQL. Retrying.", e);

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        Liquibase liquibase = null;
        try {
            LOG.info("Running database migrations.");
            liquibase.database.Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(migrationConnection);
            liquibase = new liquibase.Liquibase(
                    "db/migrations.xml", new ClassLoaderResourceAccessor(), database
            );
            liquibase.update(new Contexts(), new LabelExpression());
            LOG.info("All database migrations complete.");
        } finally {
            if (liquibase != null) {
                liquibase.close();
            }
            if (!migrationConnection.isClosed()) {
                migrationConnection.close();
            }
        }
    }

    public long getTotalSize() {
        return withHandle(handle ->
                handle.createQuery("SELECT pg_database_size(current_database())")
                .mapTo(Long.class)
                .one());
    }

    public long getTableSize(String tableName) {
        return withHandle(handle ->
                handle.createQuery("SELECT pg_total_relation_size(:table)")
                        .bind("table", tableName)
                        .mapTo(Long.class)
                        .one());
    }

    @Override
    public DateTime getDatabaseClock() {
        Timestamp now = withHandle(handle ->
                handle.createQuery("SELECT NOW()")
                        .mapTo(Timestamp.class)
                        .one()
        );

        return new DateTime(now);
    }

    public <R, X extends Exception> R withHandle(HandleCallback<R, X> callback) throws X {
        return jdbi.withHandle(callback);
    }

    public <X extends Exception> void useHandle(final HandleConsumer<X> callback) throws X {
        jdbi.useHandle(callback);
    }

}
