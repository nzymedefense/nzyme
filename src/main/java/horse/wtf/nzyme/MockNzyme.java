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

package horse.wtf.nzyme;

import com.codahale.metrics.MetricRegistry;
import horse.wtf.nzyme.alerts.AlertsService;
import horse.wtf.nzyme.configuration.Configuration;
import horse.wtf.nzyme.statistics.Statistics;

public class MockNzyme implements Nzyme {

    @Override
    public void initialize() {

    }

    @Override
    public Statistics getStatistics() {
        return null;
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    @Override
    public MetricRegistry getMetrics() {
        return null;
    }

    @Override
    public AlertsService getAlertsService() {
        return null;
    }

}
