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

package horse.wtf.nzyme.rest.resources.system;

import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableList;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.rest.responses.metrics.MetricResponse;
import horse.wtf.nzyme.rest.responses.metrics.MetricsListResponse;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/system/metrics")
@Produces(MediaType.APPLICATION_JSON)
public class MetricsResource {

    @Inject
    private Nzyme nzyme;

    @GET
    public Response all() {
        ImmutableList.Builder<MetricResponse> x = new ImmutableList.Builder<>();

        // Timers.
        for (Map.Entry<String, Timer> timer : nzyme.getMetrics().getTimers().entrySet()) {
        }

        // Counters.

        // Meters.

        // Gauges.

        // Histograms.

        ImmutableList<MetricResponse> metrics = x.build();
        return Response.ok(MetricsListResponse.create(metrics)).build();
    }

}