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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
    public Response findAllBandits() {
        List<BanditResponse> bandits = Lists.newArrayList();

        for (Bandit x : nzyme.getContactIdentifier().getBandits().values()) {
            if (x.databaseId() == null) {
                LOG.error("Uninitialized bandit in BanditIdentifier. Skipping.");
                continue;
            }

            List<BanditIdentifierResponse> identifiers = Lists.newArrayList();

            if (x.identifiers() != null) {
                for (BanditIdentifier identifier : x.identifiers()) {
                    identifiers.add(BanditIdentifierResponse.create(
                            identifier.configuration(),
                            identifier.descriptor().type(),
                            identifier.descriptor().description(),
                            identifier.descriptor().matches()
                    ));
                }
            }

            bandits.add(BanditResponse.create(
                    x.uuid(),
                    x.databaseId(),
                    x.name(),
                    x.description(),
                    x.createdAt(),
                    x.updatedAt(),
                    identifiers
            ));
        }

        return Response.ok(BanditsListResponse.create(
                bandits,
                bandits.size()
        )).build();
    }

    @POST
    public Response createBandit(CreateBanditRequest request) {
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

}
