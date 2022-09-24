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

package horse.wtf.nzyme.rest.resources.system;

import com.beust.jcommander.internal.Lists;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.MemoryRegistry;
import app.nzyme.plugin.RESTSecured;
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
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class SystemResource {

    @Inject
    private NzymeLeader nzyme;

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
                nzyme.getRegistry().getBool(MemoryRegistry.KEY.NEW_VERSION_AVAILABLE),
                nzyme.getConfiguration().versionchecksEnabled()
        )).build();
    }

}
