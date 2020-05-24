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

package horse.wtf.nzyme.periodicals.measurements;

import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MeasurementsCleaner extends Periodical {

    private static final Logger LOG = LogManager.getLogger(MeasurementsCleaner.class);

    private final NzymeLeader nzyme;

    public MeasurementsCleaner(NzymeLeader nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        LOG.debug("Retention cleaning measurements.");

        nzyme.getDatabase().useHandle(handle -> {
            handle.execute("DELETE FROM measurements WHERE created_at < (current_timestamp at time zone 'UTC' - interval '1 day')");
        });
    }

    @Override
    public String getName() {
        return "MeasurementsCleaner";
    }

}
