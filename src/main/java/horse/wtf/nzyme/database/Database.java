package horse.wtf.nzyme.database;

import horse.wtf.nzyme.alerts.service.AlertDatabaseEntryMapper;
import horse.wtf.nzyme.bandits.database.BanditIdentifierMapper;
import horse.wtf.nzyme.bandits.database.BanditMapper;
import horse.wtf.nzyme.bandits.database.ContactMapper;
import horse.wtf.nzyme.configuration.leader.LeaderConfiguration;
import horse.wtf.nzyme.dot11.deauth.db.DeauthenticationMonitorRecordingMapper;
import horse.wtf.nzyme.dot11.networks.beaconrate.BeaconRateMapper;
import horse.wtf.nzyme.dot11.networks.sentry.db.SentrySSIDMapper;
import horse.wtf.nzyme.dot11.networks.signalstrength.SignalIndexHistogramHistoryDBEntryMapper;
import horse.wtf.nzyme.measurements.mappers.MeasurementMapper;
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

public class Database {

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

    private final LeaderConfiguration configuration;

    private Jdbi jdbi;

    public Database(LeaderConfiguration configuration) {
        this.configuration = configuration;
    }

    public void initializeAndMigrate() throws LiquibaseException {
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
                .registerRowMapper(new DeauthenticationMonitorRecordingMapper());

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
