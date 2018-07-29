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

package horse.wtf.nzyme.rest.resources;

import com.google.common.collect.Lists;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.rest.responses.alerts.AlertDetailsResponse;
import horse.wtf.nzyme.rest.responses.alerts.AlertsListResponse;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/alerts")
@Produces(MediaType.APPLICATION_JSON)
public class AlertsResource {

    @Inject
    private Nzyme nzyme;

    @GET
    @Path("/active")
    public Response active(@QueryParam("limit") int limit) {
        if(limit == 0) {
            limit = 25;
        }

        Map<UUID, Alert> alerts = nzyme.getAlertsService().getActiveAlerts();

        List<AlertDetailsResponse> details = Lists.newArrayList();
        int i = 0;
        for (Map.Entry<UUID, Alert> entry : alerts.entrySet()) {
            if (i == limit) {
                break;
            }
            i++;

            Alert alert = entry.getValue();
            details.add(AlertDetailsResponse.create(
                    alert.getSubsystem(),
                    alert.getType(),
                    entry.getKey(),
                    alert.getMessage(),
                    alert.getFields(),
                    alert.getFirstSeen(),
                    alert.getLastSeen(),
                    alert.getFrameCount()
            ));
        }

        return Response.ok(AlertsListResponse.create(alerts.size(), details)).build();
    }

}
