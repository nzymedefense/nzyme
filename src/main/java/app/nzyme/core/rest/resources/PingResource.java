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

package app.nzyme.core.rest.resources;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.responses.system.PingResponse;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/ping")
@Produces(MediaType.APPLICATION_JSON)
public class PingResource {

    /*
     * DANGER: This resource is entirely unauthenticated. Be careful with what you expose.
     */

    @Inject
    private NzymeNode nzyme;

    @GET
    public Response ping() {
        return Response.ok(PingResponse.create(
                nzyme.getAuthenticationService().countSuperAdministrators() == 0
        )).build();
    }

}
