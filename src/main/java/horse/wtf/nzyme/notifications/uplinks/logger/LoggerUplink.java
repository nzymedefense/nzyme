/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package horse.wtf.nzyme.notifications.uplinks.logger;

import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.notifications.Uplink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class LoggerUplink implements Uplink {

    private static final Logger LOG = LogManager.getLogger(Dot11Probe.class);

    @Override
    public void notify(Notification notification, @Nullable Dot11MetaInformation meta) {
        LOG.info(notification);
    }

    @Override
    public void notifyOfAlert(Alert alert) {
        LOG.warn("ALERT: [{}] - {}", alert.getUUID(), alert.getMessage());
    }

}
