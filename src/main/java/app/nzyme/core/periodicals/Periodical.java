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
        LOG.debug("Running periodical [{}].", getName());
        Stopwatch timer = Stopwatch.createStarted();

        try {
            execute();
        } catch(Exception e) {
            LOG.error("Error during execution of periodical [{}].", getName(), e);
        }

        LOG.debug("Periodical [{}] finished in <{} ms>.", getName(), timer.elapsed(TimeUnit.MILLISECONDS));
    }

}
