package horse.wtf.nzyme.database;

import horse.wtf.nzyme.configuration.Configuration;
import horse.wtf.nzyme.dot11.networks.beaconrate.BeaconRateMapper;
import horse.wtf.nzyme.dot11.networks.sigindex.SignalInformationMapper;
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
import org.jdbi.v3.sqlite3.SQLitePlugin;

public class Database {

    private final Configuration configuration;

    private Jdbi jdbi;

    public Database(Configuration configuration) {
        this.configuration = configuration;
    }

    public void initializeAndMigrate() throws LiquibaseException {
        this.jdbi = Jdbi.create("jdbc:sqlite:" + this.configuration.databasePath())
                .installPlugin(new SQLitePlugin())
                .installPlugin(new JodaTimePlugin())
                .registerRowMapper(new MeasurementMapper())
                .registerRowMapper(new SignalInformationMapper())
                .registerRowMapper(new BeaconRateMapper());

        // Run migrations against underlying JDBC connection.
        JdbcConnection connection = new JdbcConnection(jdbi.open().getConnection());
        try {
            liquibase.database.Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
            Liquibase liquibase = new liquibase.Liquibase("db/migrations.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts(), new LabelExpression());
        } finally {
            connection.close();
        }

        // Set busy timeout.
        useHandle(handle -> {
            handle.execute("PRAGMA busy_timeout = 10000;");
        });
    }

    public <R, X extends Exception> R withHandle(HandleCallback<R, X> callback) throws X {
        return jdbi.withHandle(callback);
    }

    public <X extends Exception> void useHandle(final HandleConsumer<X> callback) throws X {
        jdbi.useHandle(callback);
    }

}
