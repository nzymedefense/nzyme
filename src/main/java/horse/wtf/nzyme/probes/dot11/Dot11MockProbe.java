/*
 *  This file is part of Nzyme.
 *
 *  Nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.probes.dot11;

import horse.wtf.nzyme.statistics.Statistics;

public class Dot11MockProbe extends Dot11Probe {

    public Dot11MockProbe(Dot11ProbeConfiguration configuration, Statistics statistics) {
        super(configuration, statistics);
    }

    @Override
    public Runnable loop() throws Dot11ProbeInitializationException {
        return () -> { /* noop */ };
    }

    @Override
    public boolean isInLoop() {
        return false;
    }

}
