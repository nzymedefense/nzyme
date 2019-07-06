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

package horse.wtf.nzyme.periodicals.alerting.sigindex;

import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

public class SignalIndexCleaner extends Periodical {

    private static final Logger LOG = LogManager.getLogger(SignalIndexCleaner.class);

    private final Nzyme nzyme;

    public SignalIndexCleaner(Nzyme nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        DateTime limit = new DateTime().minusDays(1);
        LOG.debug("Deleting all signal index values older than <{}>.", limit);

        nzyme.getDatabase().useHandle(handle -> {
            handle.execute("DELETE FROM signal_index_history WHERE created_at < ?", limit);
        });
    }

    @Override
    public String getName() {
        return "SignalIndexCleaner";
    }
}
