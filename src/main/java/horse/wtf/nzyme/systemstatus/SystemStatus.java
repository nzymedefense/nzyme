/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.systemstatus;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class SystemStatus {

    private static final Logger LOG = LogManager.getLogger(SystemStatus.class);

    // Make sure to document every new status here: https://go.nzyme.org/system-status-explained
    public enum TYPE {
        RUNNING,
        SHUTTING_DOWN,
        TRAINING
    }

    private Set<TYPE> currentStatus;

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
