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

package horse.wtf.nzyme.dot11.handlers;

import com.google.common.collect.Maps;
import horse.wtf.nzyme.dot11.networks.Networks;
import horse.wtf.nzyme.dot11.networks.SignalDelta;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.systemstatus.SystemStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public abstract class Dot11FrameHandler<T> {

    private static final Logger LOG = LogManager.getLogger(Dot11FrameHandler.class);

    protected Dot11Probe probe;

    protected Dot11FrameHandler(Dot11Probe probe) {
        this.probe = probe;
    }

    private void tick() {
        probe.getStatistics().tickType(getName());
    }

    public void handle(T frame) {
        tick();

        doHandle(frame);
    }

    protected abstract void doHandle(T frame);
    public abstract String getName();

}
