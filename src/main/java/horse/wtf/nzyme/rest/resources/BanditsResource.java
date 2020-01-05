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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifier;
import horse.wtf.nzyme.rest.authentication.Secured;
import horse.wtf.nzyme.rest.requests.CreateBanditRequest;
import horse.wtf.nzyme.rest.responses.bandits.BanditIdentifierResponse;
import horse.wtf.nzyme.rest.responses.bandits.BanditResponse;
import horse.wtf.nzyme.rest.responses.bandits.BanditsListResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Path("/api/bandits")
@Secured
@Produces(MediaType.APPLICATION_JSON)
public class BanditsResource {

    private static final Logger LOG = LogManager.getLogger(BanditsResource.class);

    @Inject
    private Nzyme nzyme;

    @GET
    public Response findAll() {
        List<BanditResponse> bandits = Lists.newArrayList();

        for (Bandit x : nzyme.getContactIdentifier().getBandits().values()) {
            if (x.databaseId() == null) {
                LOG.error("Uninitialized bandit in BanditIdentifier. Skipping.");
                continue;
            }

            bandits.add(BanditResponse.create(
                    x.uuid(),
                    x.databaseId(),
                    x.name(),
                    x.description(),
                    x.createdAt(),
                    x.updatedAt(),
                    buildIdentifiersResponse(x)
            ));
        }

        return Response.ok(BanditsListResponse.create(
                bandits,
                bandits.size()
        )).build();
    }

    @GET
    @Path("/show/{uuid}")
    public Response findOne(@PathParam("uuid") String id) {
        if (Strings.isNullOrEmpty(id)) {
            LOG.warn("Bandit ID was null or empty.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }


        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch(IllegalArgumentException e) {
            LOG.warn("Invalid Bandit UUID", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Bandit bandit = nzyme.getContactIdentifier().getBandits().get(uuid);
        if (bandit == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(BanditResponse.create(
                bandit.uuid(),
                bandit.databaseId(),
                bandit.name(),
                bandit.description(),
                bandit.createdAt(),
                bandit.updatedAt(),
                buildIdentifiersResponse(bandit)
        )).build();
    }

    @POST
    public Response create(CreateBanditRequest request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (Strings.isNullOrEmpty(request.name()) || Strings.isNullOrEmpty(request.description())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        nzyme.getContactIdentifier().registerBandit(Bandit.create(
                null,
                UUID.randomUUID(),
                request.name(),
                request.description(),
                null,
                null,
                null
        ));

        return Response.status(Response.Status.CREATED).build();
    }

    private List<BanditIdentifierResponse> buildIdentifiersResponse(Bandit bandit) {
        ImmutableList.Builder<BanditIdentifierResponse> response = new ImmutableList.Builder<>();

        if (bandit.identifiers() != null) {
            for (BanditIdentifier identifier : bandit.identifiers()) {
                response.add(BanditIdentifierResponse.create(
                        identifier.configuration(),
                        identifier.descriptor().type(),
                        identifier.descriptor().description(),
                        identifier.descriptor().matches()
                ));
            }
        }

        return response.build();
    }

}
