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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PeriodicalManager {

    private static final Logger LOG = LogManager.getLogger(PeriodicalManager.class);

    private final ScheduledExecutorService executor;

    public PeriodicalManager() {
        // TODO make core pool size configurable
        this.executor = Executors.newScheduledThreadPool(5,
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("periodicals-%d")
                        .build()
        );
    }

    public void scheduleAtFixedRate(Periodical periodical, long initialDelay, long period, TimeUnit timeUnit) {
        LOG.info("Scheduling [{}] for every <{} {}> with <{} {}> initial delay.",
                periodical.getName(), period, timeUnit, initialDelay, timeUnit);

        executor.scheduleAtFixedRate(periodical, initialDelay, period, timeUnit);
    }

}
