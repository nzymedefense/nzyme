package horse.wtf.nzyme;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    public static void main(String[] argv) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Thread.currentThread().setName("shutdown-hook");
            LOG.info("Shutting down.");
        }));

        try {
            Nzyme nzyme = new Nzyme(argv);
            nzyme.loop();
        } catch (Nzyme.InitializationException e) {
            LOG.error("Boot error.", e);
            Runtime.getRuntime().exit(1);
        }
    }

}
