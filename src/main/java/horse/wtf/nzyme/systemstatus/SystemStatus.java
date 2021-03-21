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

package horse.wtf.nzyme.systemstatus;

import com.google.common.collect.Sets;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.Set;

public class SystemStatus {

    private static final Logger LOG = LogManager.getLogger(SystemStatus.class);

    // Make sure to document every new status here: https://go.nzyme.org/system-status-explained
    public enum TYPE {
        RUNNING,
        SHUTTING_DOWN,
        TRAINING
    }

    public enum HEALTH {
        GREEN,
        RED
    }

    private Set<TYPE> currentStatus;

    public HEALTH decideHealth(NzymeLeader nzyme) {
        for (Dot11Probe probe : nzyme.getProbes()) {
            if (!probe.isInLoop() || !probe.isActive()) {
                return HEALTH.RED;
            }
        }

        return HEALTH.GREEN;
    }

    public SystemStatus() {
        this.currentStatus = Sets.newHashSet();
    }

    public boolean isInStatus(TYPE status) {
        return currentStatus.contains(status);
    }

    public void setStatus(TYPE status) {
        if (!currentStatus.contains(status)) {
            currentStatus.add(status);
            LOG.info("Set system status [{}].", status);
        } else {
            LOG.warn("Tried to set status [{}] but it was already set.", status);
        }
    }

    public void unsetStatus(TYPE status) {
        if (currentStatus.contains(status)) {
            currentStatus.remove(status);
            LOG.info("Unset system status [{}].", status);
        } else {
            LOG.warn("Tried to unset status [{}] but it was not set.", status);
        }
    }

}
