package horse.wtf.nzyme;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    // TODO send timestamp of frame not when we processed it
    // TODO implement assoc and disassoc
    // TODO Lock during channel switch? Avoid getCurrentChannel() race condition
    // TODO tests
    // TODO test deauth handler on other platform. might have same header offset issue
    // TODO why does this not run on ARM / raspberry
    // TODO allow running without root on Linux
    // TODO mvn release plugin
    // TODO mvn deb/rpm packaging
    // TODO auto-enrich SSID where possible

    /*
     * README:
     *  - explain that you'll need a second interface because you'll lose network connection. link to ALFA on Amazon
     *  - it only works if network interface not connected to a wifi. channel will not change otherwise.
     *  - explain config file. reference example config
     *  - startup, CLI parameters
     *  - examples of what to do with the data in graylog and how to set up input
     *  - examples for high-traffic environment and required graylog hardware
     *  - explain that there is a chance you miss important indicators when cycling over too many channels
     *      set low cycle time and split up channels over multiple sensors. or use multiple interfaces!
     *  - channel hopping conf on osx and linux, with sudo
     *  - beacon sampling
     *  - Raspberry Pi / ARM issues
     */

    /* unencrypted mgt frames:
    Authentication
    - De-authentication
    - Association request
    Association response
    x Re-association request
    x Re-association response
    - Probe request
    - Probe response
    - Beacon
     */

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
