package app.nzyme.core.rest.resources.dot11;

import app.nzyme.core.dot11.monitoring.Dot11BanditDescription;
import app.nzyme.core.dot11.monitoring.Dot11Bandits;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.responses.dot11.monitoring.BuiltinBanditDetailsResponse;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;

@Path("/api/dot11/bandits")
@Produces(MediaType.APPLICATION_JSON)
public class BanditsResource extends UserAuthenticatedResource {

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/builtin")
    public Response findAllBuiltIn() {
        List<BuiltinBanditDetailsResponse> bandits = Lists.newArrayList();

        for (Dot11BanditDescription bandit : Dot11Bandits.BUILT_IN) {
            bandits.add(BuiltinBanditDetailsResponse.create(
                    bandit.id(),
                    bandit.name(),
                    bandit.description(),
                    bandit.fingerprints() == null ? Collections.emptyList() : bandit.fingerprints()
            ));
        }

        return Response.ok(bandits).build();
    }

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/builtin/show/{id}")
    public Response findOneBuiltIn(@PathParam("id") @NotEmpty String id) {
        Dot11BanditDescription bandit = null;
        for (Dot11BanditDescription b : Dot11Bandits.BUILT_IN) {
            if (b.id().equals(id)) {
                bandit = b;
                break;
            }
        }

        if (bandit == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(BuiltinBanditDetailsResponse.create(
                bandit.id(),
                bandit.name(),
                bandit.description(),
                bandit.fingerprints() == null ? Collections.emptyList() : bandit.fingerprints()
        )).build();
    }

}
