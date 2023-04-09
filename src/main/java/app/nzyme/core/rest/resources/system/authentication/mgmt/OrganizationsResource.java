package app.nzyme.core.rest.resources.system.authentication.mgmt;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.requests.CreateOrganizationRequest;
import app.nzyme.core.rest.responses.authentication.mgmt.OrganizationDetailsResponse;
import app.nzyme.core.rest.responses.authentication.mgmt.OrganizationsListResponse;
import app.nzyme.core.security.authentication.db.OrganizationEntry;
import app.nzyme.core.security.authentication.db.TenantEntry;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
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
import java.util.Optional;

@Path("/api/system/authentication/mgmt/organizations")
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class OrganizationsResource {

    private static final Logger LOG = LogManager.getLogger(OrganizationsResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    public Response findAll() {
        List<OrganizationDetailsResponse> organizations = Lists.newArrayList();

        for (OrganizationEntry org : nzyme.getAuthenticationService().findAllOrganizations()) {
            Optional<List<TenantEntry>> tenants = nzyme.getAuthenticationService().findAllTenantsOfOrganization(org.id());

            organizations.add(OrganizationDetailsResponse.create(
                    org.id(),
                    org.name(),
                    org.description(),
                    org.createdAt(),
                    org.updatedAt(),
                    tenants.map(List::size).orElse(0)
            ));
        }

        return Response.ok(OrganizationsListResponse.create(organizations)).build();
    }

    @POST
    public Response create(CreateOrganizationRequest req) {
        if (req.name().trim().isEmpty() || req.description().trim().isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        nzyme.getAuthenticationService().createOrganization(req.name(), req.description());

        return Response.ok().build();
    }

}
