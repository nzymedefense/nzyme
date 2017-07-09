package horse.wtf.nzyme;

import com.beust.jcommander.JCommander;
import com.github.joschi.jadconfig.JadConfig;
import com.github.joschi.jadconfig.RepositoryException;
import com.github.joschi.jadconfig.ValidationException;
import com.github.joschi.jadconfig.repositories.PropertiesRepository;
import horse.wtf.nzyme.configuration.CLIArguments;
import horse.wtf.nzyme.configuration.Configuration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    private static final int FAILURE = 1;

    public static void main(String[] argv) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Thread.currentThread().setName("shutdown-hook");
            LOG.info("Shutting down.");
        }));

        final CLIArguments cliArguments = new CLIArguments();
        final Configuration configuration = new Configuration();

        // Parse CLI arguments.
        JCommander.newBuilder()
                .addObject(cliArguments)
                .build()
                .parse(argv);

        // Parse configuration.
        try {
            new JadConfig(new PropertiesRepository(cliArguments.getConfigFilePath()), configuration).process();
        } catch (RepositoryException | ValidationException e) {
            LOG.error("Could not read config.", e);
            Runtime.getRuntime().exit(FAILURE);
        }

        // Override log level if requested.
        if(cliArguments.isDebugMode()) {
            Logging.setRootLoggerLevel(Level.DEBUG);
        }

        if(cliArguments.isTraceMode()) {
            Logging.setRootLoggerLevel(Level.TRACE);
        }

        try {
            Nzyme nzyme = new Nzyme(cliArguments, configuration);
            nzyme.loop();
        } catch (Nzyme.InitializationException e) {
            LOG.error("Boot error.", e);
            Runtime.getRuntime().exit(FAILURE);
        }
    }

}
