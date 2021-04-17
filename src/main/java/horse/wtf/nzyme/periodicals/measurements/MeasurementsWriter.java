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

package horse.wtf.nzyme.periodicals.measurements;

import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.measurements.MeasurementType;
import horse.wtf.nzyme.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MeasurementsWriter extends Periodical  {

    private static final Logger LOG = LogManager.getLogger(MeasurementsWriter.class);

    private final NzymeLeader nzyme;

    public MeasurementsWriter(NzymeLeader nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        LOG.debug("Updating measurements.");

        nzyme.getDatabase().useHandle(handle -> {
            handle.execute("INSERT INTO measurements(measurement_type, measurement_value, created_at) VALUES(?, ?, current_timestamp at time zone 'UTC')",
                    MeasurementType.DOT11_ACCESS_POINT_COUNT, nzyme.getNetworks().getBSSIDs().keySet().size());

            handle.execute("INSERT INTO measurements(measurement_type, measurement_value, created_at) VALUES(?, ?, current_timestamp at time zone 'UTC')",
                    MeasurementType.DOT11_CLIENT_COUNT, nzyme.getClients().getClients().size());

            handle.execute("INSERT INTO measurements(measurement_type, measurement_value, created_at) VALUES(?, ?, current_timestamp at time zone 'UTC')",
                    MeasurementType.DOT11_FRAME_COUNT, nzyme.getFrameProcessor().getRecentFrameCount());
        });
    }

    @Override
    public String getName() {
        return "MeasurementsWriter";
    }

}
