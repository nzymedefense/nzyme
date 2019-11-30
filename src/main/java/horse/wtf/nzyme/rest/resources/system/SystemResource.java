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

import com.beust.jcommander.internal.Lists;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.Registry;
import horse.wtf.nzyme.rest.authentication.Secured;
import horse.wtf.nzyme.rest.responses.system.SystemStatusResponse;
import horse.wtf.nzyme.rest.responses.system.SystemStatusStateResponse;
import horse.wtf.nzyme.rest.responses.system.VersionResponse;
import horse.wtf.nzyme.systemstatus.SystemStatus;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/api/system")
@Secured
@Produces(MediaType.APPLICATION_JSON)
public class SystemResource {

    @Inject
    private Nzyme nzyme;

    @GET
    @Path("/status")
    public Response getStatus() {
        List<SystemStatusStateResponse> states = Lists.newArrayList();

        for (SystemStatus.TYPE type : SystemStatus.TYPE.values()) {
            states.add(SystemStatusStateResponse.create(
                    type,
                    nzyme.getSystemStatus().isInStatus(type)
            ));
        }

        return Response.ok(SystemStatusResponse.create(states)).build();
    }

    @GET
    @Path("/version")
    public Response getVersion() {
        return Response.ok(VersionResponse.create(
                nzyme.getVersion().getVersionString(),
                nzyme.getRegistry().getBool(Registry.KEY.NEW_VERSION_AVAILABLE),
                nzyme.getConfiguration().versionchecksEnabled()
        )).build();
    }

}
