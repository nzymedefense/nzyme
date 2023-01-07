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

package app.nzyme.core.periodicals.measurements;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MeasurementsCleaner extends Periodical {

    private static final Logger LOG = LogManager.getLogger(MeasurementsCleaner.class);

    private final NzymeNode nzyme;

    public MeasurementsCleaner(NzymeNode nzyme) {
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
