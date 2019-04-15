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

package horse.wtf.nzyme.periodicals;

import horse.wtf.nzyme.Nzyme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class OUIUpdater extends Periodical {

    private static final Logger LOG = LogManager.getLogger(OUIUpdater.class);

    private final Nzyme nzyme;

    public OUIUpdater(Nzyme nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        try {
            this.nzyme.getOUIManager().fetchAndUpdate();
        } catch (IOException e) {
            LOG.error("Could not fetch and update OUI list.", e);
        }
    }

    @Override
    public String getName() {
        return "OUIUpdater";
    }

}
