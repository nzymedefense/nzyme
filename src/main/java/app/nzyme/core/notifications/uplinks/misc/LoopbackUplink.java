package app.nzyme.core.notifications.uplinks.misc;

import app.nzyme.core.alerts.Alert;
import app.nzyme.core.dot11.Dot11MetaInformation;
import app.nzyme.core.notifications.Notification;
import app.nzyme.core.notifications.Uplink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

/*
 * Used for handler unit testing and has no real use-case for anything else.
 */
public class LoopbackUplink implements Uplink {

    private static final Logger LOG = LogManager.getLogger(LoopbackUplink.class);

    private Notification lastNotification;
    private Dot11MetaInformation lastMeta;
    private Alert lastAlert;

    @Override
    public void notify(Notification notification, @Nullable Dot11MetaInformation meta) {
        this.lastNotification = notification;
        this.lastMeta = meta;
    }

    @Override
    public void notifyOfAlert(Alert alert) {
        LOG.info("Alert received.");
        this.lastAlert = alert;
    }

    public Notification getLastNotification() {
        return lastNotification;
    }

    public Dot11MetaInformation getLastMeta() {
        return lastMeta;
    }

    public Alert getLastAlert() {
        return lastAlert;
    }

    public void clear() {
        LOG.info("Cleaning.");
        lastNotification = null;
        lastMeta = null;
        lastAlert = null;
    }

}
