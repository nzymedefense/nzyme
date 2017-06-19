package horse.wtf.nzyme;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    // sudo /System/Library/PrivateFrameworks/Apple80211.framework/Versions/Current/Resources/airport --channel=2

    private static final Logger LOG = LogManager.getLogger(Main.class);

    // TODO fix broken strings
    // TODO aggregate beacons?
    // TODO send timestamp of frame not when we processed it
    // TODO implement assoc and disassoc
    // TODO signal strength
    // TODO test deauth handler on other platform. might have same header offset issue
    // TODO proper return codes
    // TODO channel usage. cycle through them? how?

    /* unencrypted mgt frames:
    Authentication
    De-authentication
    Association request
    Association response
    Re-association request
    Re-association response
    Probe request
    Probe response
    Beacon
     */

    public static void main(String[] argv) {
        LOG.info("Starting up. | (⌐■_■)–︻╦╤─ – – pew pew");

        try {
            Nzyme nzyme = new Nzyme(argv, "nzyme-lab-1");
            nzyme.loop();
        } catch (Nzyme.InitializationException e) {
            LOG.error("Boot error.", e);
        }
    }

}
