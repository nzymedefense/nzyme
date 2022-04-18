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

import com.google.common.collect.Lists;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.dot11.probes.Dot11SenderProbe;
import horse.wtf.nzyme.rest.authentication.RESTSecured;
import horse.wtf.nzyme.rest.responses.system.ProbeResponse;
import horse.wtf.nzyme.rest.responses.system.ProbesListResponse;
import horse.wtf.nzyme.rest.responses.system.TrapResponse;
import horse.wtf.nzyme.rest.responses.system.TrapsListResponse;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/api/system/probes")
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class ProbesResource {

    @Inject
    private NzymeLeader nzyme;

    @GET
    @RESTSecured
    public Response all() {
        List<ProbeResponse> response = Lists.newArrayList();
        for (Dot11Probe probe : nzyme.getProbes()) {
            response.add(ProbeResponse.create(
                    probe.getName(),
                    probe.getClass().getSimpleName(),
                    probe.getConfiguration().networkInterfaceName(),
                    probe.isInLoop(),
                    probe.isActive(),
                    probe.getConfiguration().channels(),
                    probe.getCurrentChannel(),
                    probe.getTotalFrames()
            ));
        }

        return Response.ok(ProbesListResponse.create(response.size(), response)).build();
    }

    @GET
    @Path("/traps")
    public Response traps() {
        List<TrapResponse> traps = Lists.newArrayList();
        for (Dot11Probe probe : nzyme.getProbes()) {
            if (probe instanceof Dot11SenderProbe) {

                Dot11SenderProbe sender = (Dot11SenderProbe) probe;

                traps.add(TrapResponse.create(
                        ProbeResponse.create(
                                probe.getName(),
                                probe.getClass().getSimpleName(),
                                probe.getConfiguration().networkInterfaceName(),
                                probe.isInLoop(),
                                probe.isActive(),
                                probe.getConfiguration().channels(),
                                probe.getCurrentChannel(),
                                probe.getTotalFrames()
                        ),
                        sender.getTrap().getType().toString(),
                        sender.getTrap().getDescription()
                ));
            }
        }

        return Response.ok(TrapsListResponse.create(traps)).build();
    }

}