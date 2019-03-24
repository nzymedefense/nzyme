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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.rest.responses.probes.CurrentChannelsResponse;
import horse.wtf.nzyme.rest.responses.system.ProbeResponse;
import horse.wtf.nzyme.rest.responses.system.ProbesListResponse;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/api/system/probes")
@Produces(MediaType.APPLICATION_JSON)
public class ProbesResource {

    @Inject
    private Nzyme nzyme;

    @GET
    public Response all() {
        List<ProbeResponse> response = Lists.newArrayList();
        for (Dot11Probe probe : nzyme.getProbes()) {
            response.add(ProbeResponse.create(
                    probe.getName(),
                    probe.getClass().getSimpleName(),
                    probe.getConfiguration().networkInterfaceName(),
                    probe.isInLoop(),
                    probe.getConfiguration().channels(),
                    probe.getCurrentChannel(),
                    probe.getTotalFrames()
            ));
        }

        return Response.ok(ProbesListResponse.create(response.size(), response)).build();
    }

    @GET
    @Path("/channels")
    public Response channels() {
        ImmutableMap.Builder<String, Integer> channels = new ImmutableMap.Builder<>();

        for (Dot11Probe probe : nzyme.getProbes()) {
            if (probe.getCurrentChannel() != null) {
                channels.put(probe.getName(), probe.getCurrentChannel());
            }
        }

        return Response.ok(CurrentChannelsResponse.create(channels.build())).build();
    }

}