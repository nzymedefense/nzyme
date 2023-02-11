/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package app.nzyme.core.periodicals;

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
                        .setUncaughtExceptionHandler((thread, throwable) -> LOG.error("Uncaught exception in a periodical!", throwable))
                        .build()
        );
    }

    public void scheduleAtFixedRate(Periodical periodical, long initialDelay, long period, TimeUnit timeUnit) {
        LOG.info("Scheduling [{}] for every <{} {}> with <{} {}> initial delay.",
                periodical.getName(), period, timeUnit, initialDelay, timeUnit);

        executor.scheduleAtFixedRate(periodical, initialDelay, period, timeUnit);
    }

}
