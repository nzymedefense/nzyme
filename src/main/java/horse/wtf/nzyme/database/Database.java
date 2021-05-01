package horse.wtf.nzyme.database;

import horse.wtf.nzyme.alerts.service.AlertDatabaseEntryMapper;
import horse.wtf.nzyme.bandits.database.BanditIdentifierMapper;
import horse.wtf.nzyme.bandits.database.BanditMapper;
import horse.wtf.nzyme.bandits.database.ContactMapper;
import horse.wtf.nzyme.configuration.leader.LeaderConfiguration;
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

    public static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss.")
            .appendFractionOfSecond(0, 6)
            .toFormatter().withZoneUTC();

    public static final DateTimeFormatter DATE_TIME_FORMATTER_WITH_ZONE = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss.SSSZ")
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
                .registerRowMapper(new SentrySSIDMapper());

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

    public <R, X extends Exception> R withHandle(HandleCallback<R, X> callback) throws X {
        return jdbi.withHandle(callback);
    }

    public <X extends Exception> void useHandle(final HandleConsumer<X> callback) throws X {
        jdbi.useHandle(callback);
    }

}
