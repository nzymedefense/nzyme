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

import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

public abstract class Periodical implements Runnable {

    private static final Logger LOG = LogManager.getLogger(Periodical.class);

    protected abstract void execute();
    public abstract String getName();

    @Override
    public void run() {
        LOG.info("Running periodical [{}].", getName());
        Stopwatch timer = Stopwatch.createStarted();

        try {
            execute();
        } catch(Exception e) {
            LOG.error("Error during execution of periodical [{}].", getName(), e);
        }

        LOG.info("Periodical [{}] finished in <{} ms>.", getName(), timer.elapsed(TimeUnit.MILLISECONDS));
    }

}
