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

package app.nzyme.core;

import app.nzyme.core.alerts.Alert;
import app.nzyme.core.dot11.Dot11MetaInformation;
import app.nzyme.core.dot11.frames.Dot11Frame;
import app.nzyme.core.notifications.Notification;

public interface RemoteConnector {

    void notifyUplinks(Notification notification, Dot11MetaInformation meta);
    void notifyUplinksOfAlert(Alert alert);

    void forwardFrame(Dot11Frame frame);

}
