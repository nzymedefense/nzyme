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

package horse.wtf.nzyme.bandits.trackers;

import org.joda.time.DateTime;

public class Tracker {

    private final String name;

    private String version;
    private long drift;
    private DateTime lastSeen;

    public Tracker(String name, DateTime lastSeen, String version, long drift) {
        this.name = name;
        this.version = version;
        this.drift = drift;
        this.lastSeen = lastSeen;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getDrift() {
        return drift;
    }

    public void setDrift(long drift) {
        this.drift = drift;
    }

    public DateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(DateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

}
