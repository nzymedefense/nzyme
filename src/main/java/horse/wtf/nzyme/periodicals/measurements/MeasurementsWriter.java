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

import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.measurements.MeasurementType;
import horse.wtf.nzyme.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MeasurementsWriter extends Periodical  {

    private static final Logger LOG = LogManager.getLogger(MeasurementsWriter.class);

    private final Nzyme nzyme;

    public MeasurementsWriter(Nzyme nzyme) {
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
                    MeasurementType.DOT11_FRAME_COUNT, nzyme.getStatistics().getRecentFrameCount());
        });
    }

    @Override
    public String getName() {
        return "MeasurementsWriter";
    }

}
