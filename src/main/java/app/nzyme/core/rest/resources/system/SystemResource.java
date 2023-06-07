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

package app.nzyme.core.rest.resources.system;

import app.nzyme.core.NzymeNode;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.core.MemoryRegistry;
import app.nzyme.plugin.rest.security.RESTSecured;
import app.nzyme.core.rest.responses.system.VersionResponse;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/system")
@RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
@Produces(MediaType.APPLICATION_JSON)
public class SystemResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/status")
    public Response getStatus() {
        return Response.ok().build();
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
