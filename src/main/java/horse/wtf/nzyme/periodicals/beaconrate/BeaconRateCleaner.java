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

package horse.wtf.nzyme.periodicals.beaconrate;

import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.database.Database;
import horse.wtf.nzyme.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

public class BeaconRateCleaner extends Periodical {

    private static final Logger LOG = LogManager.getLogger(BeaconRateCleaner.class);

    private final Database database;

    public BeaconRateCleaner(Nzyme nzyme) {
        this.database = nzyme.getDatabase();
    }

    @Override
    protected void execute() {
        try {
            LOG.debug("Retention cleaning beacon rate values.");

            database.useHandle(handle -> {
                handle.execute("DELETE FROM beacon_rate_history WHERE created_at < DATETIME('now', '-1 day')");
            });
        } catch(Exception e) {
            LOG.error("Could not retention clean beacon rate information.", e);
        }
    }

    @Override
    public String getName() {
        return "BeaconRateCleaner";
    }

}
