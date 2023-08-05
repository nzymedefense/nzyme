package app.nzyme.core.database;

import app.nzyme.core.configuration.node.NodeConfiguration;
import app.nzyme.core.crypto.database.TLSKeyAndCertificateEntryMapper;
import app.nzyme.core.crypto.database.TLSWildcardKeyAndCertificateEntryMapper;
import app.nzyme.core.detection.alerts.db.DetectionAlertAttributeEntryMapper;
import app.nzyme.core.detection.alerts.db.DetectionAlertEntryMapper;
import app.nzyme.core.distributed.database.NodeEntryMapper;
import app.nzyme.core.distributed.database.metrics.GaugeHistogramBucketMapper;
import app.nzyme.core.distributed.database.metrics.TimerSnapshotMapper;
import app.nzyme.core.distributed.messaging.postgres.PostgresMessageEntryMapper;
import app.nzyme.core.distributed.tasksqueue.postgres.PostgresTasksQueueEntryMapper;
import app.nzyme.core.dot11.db.*;
import app.nzyme.core.dot11.db.monitoring.*;
import app.nzyme.core.events.db.EventActionEntryMapper;
import app.nzyme.core.events.db.EventEntryMapper;
import app.nzyme.core.events.db.SubscriptionEntryMapper;
import app.nzyme.core.monitoring.health.db.IndicatorStatusMapper;
import app.nzyme.core.registry.RegistryEntryMapper;
import app.nzyme.core.security.authentication.db.OrganizationEntryMapper;
import app.nzyme.core.security.authentication.db.TapPermissionEntryMapper;
import app.nzyme.core.security.authentication.db.TenantEntryMapper;
import app.nzyme.core.security.authentication.db.UserEntryMapper;
import app.nzyme.core.security.sessions.db.SessionEntryMapper;
import app.nzyme.core.security.sessions.db.SessionEntryWithUserDetailsMapper;
import app.nzyme.plugin.Database;
import app.nzyme.core.crypto.database.PGPKeyFingerprintMapper;
import app.nzyme.core.ethernet.dns.db.DNSPairSummaryMapper;
import app.nzyme.core.ethernet.dns.db.DNSStatisticsBucketMapper;
import app.nzyme.core.ethernet.dns.db.DNSTrafficSummaryMapper;
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
import org.joda.time.DateTime;

import java.sql.Timestamp;

public class DatabaseImpl implements Database {

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
                .registerRowMapper(new TapMapper())
                .registerRowMapper(new BusMapper())
                .registerRowMapper(new ChannelMapper())
                .registerRowMapper(new CaptureMapper())
                .registerRowMapper(new TapMetricsGaugeMapper())
                .registerRowMapper(new TapMetricsGaugeAggregationMapper())
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
                .registerRowMapper(new SSIDAdvertisementHistogramEntryMapper())
                .registerRowMapper(new ChannelHistogramEntryMapper())
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
                .registerRowMapper(new DetectionAlertAttributeEntryMapper());

        // Run migrations against underlying JDBC connection.
        JdbcConnection connection = new JdbcConnection(jdbi.open().getConnection());
        Liquibase liquibase = null;
        try {
            liquibase.database.Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
            liquibase = new liquibase.Liquibase("db/migrations.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts(), new LabelExpression());
        } finally {
            if (liquibase != null) {
                liquibase.close();
            }
            if (!connection.isClosed()) {
                connection.close();
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
