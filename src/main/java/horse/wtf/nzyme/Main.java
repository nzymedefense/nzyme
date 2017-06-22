package horse.wtf.nzyme;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    // TODO fix broken strings
    // TODO aggregate beacons?
    // TODO send timestamp of frame not when we processed it
    // TODO implement assoc and disassoc
    // TODO signal strength
    // TODO test deauth handler on other platform. might have same header offset issue
    // TODO proper return codes
    // TODO list channels with activity
    // TODO make channel hop command configurable

    /*
     * README:
     *  - explain that you'll need a second interface because you'll lose network connection. link to ALFA on Amazon
     *  - it only works if network interface not connected to a wifi. channel will not change otherwise.
     *  - explain sudo
     *  - explain config file. reference example config
     *  - startup, CLI parameters
     */

    /* unencrypted mgt frames:
    Authentication
    - De-authentication
    Association request
    Association response
    Re-association request
    Re-association response
    - Probe request
    Probe response
    - Beacon
     */

    public static void main(String[] argv) {
        try {
            Nzyme nzyme = new Nzyme(argv, "nzyme-lab-1");
            nzyme.loop();
        } catch (Nzyme.InitializationException e) {
            LOG.error("Boot error.", e);
        }
    }

}
