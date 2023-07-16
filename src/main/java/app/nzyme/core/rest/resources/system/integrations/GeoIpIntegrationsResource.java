package app.nzyme.core.rest.resources.system.integrations;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.integrations.geoip.GeoIpRegistryKeys;
import app.nzyme.core.integrations.geoip.ipinfo.IpInfoFreeGeoIpAdapter;
import app.nzyme.core.rest.requests.ActivateGeoIpProviderRequest;
import app.nzyme.core.rest.requests.IpInfoFreeConfigurationUpdateRequest;
import app.nzyme.core.rest.responses.integrations.geoip.GeoIpSummaryResponse;
import app.nzyme.core.rest.responses.integrations.geoip.providers.ipinfofree.IpInfoFreeConfigurationResponse;
import app.nzyme.plugin.RegistryCryptoException;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraintValidator;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryValueType;
import app.nzyme.plugin.rest.configuration.EncryptedConfigurationEntryResponse;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import jakarta.ws.rs.PUT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;

@Path("/api/system/integrations/geoip")
@RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
@Produces(MediaType.APPLICATION_JSON)
public class GeoIpIntegrationsResource {

    private static final Logger LOG = LogManager.getLogger(GeoIpIntegrationsResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    public Response summary() {
        String adapterName = nzyme.getDatabaseCoreRegistry()
                .getValue(GeoIpRegistryKeys.GEOIP_PROVIDER_NAME.key())
                .orElse("noop");

        return Response.ok(GeoIpSummaryResponse.create(adapterName)).build();
    }

    @PUT
    @Path("/providers/active")
    public Response activateProvider(ActivateGeoIpProviderRequest req){
        nzyme.getDatabaseCoreRegistry().setValue(GeoIpRegistryKeys.GEOIP_PROVIDER_NAME.key(), req.providerName());

        return Response.ok().build();
    }

    @GET
    @Path("/providers/ipinfofree/configuration")
    public Response ipInfoFreeConfiguration() {
        boolean tokenIsSet;
        try {
            tokenIsSet = nzyme.getDatabaseCoreRegistry()
                    .getEncryptedValue(IpInfoFreeGeoIpAdapter.REGISTRY_KEY_TOKEN.key())
                    .isPresent();
        } catch(RegistryCryptoException e) {
            LOG.error("Could not decrypt encrypted registry value.", e);
            return Response.serverError().build();
        }

        IpInfoFreeConfigurationResponse response = IpInfoFreeConfigurationResponse.create(
                EncryptedConfigurationEntryResponse.create(
                        IpInfoFreeGeoIpAdapter.REGISTRY_KEY_TOKEN.key(),
                        "API Token",
                        tokenIsSet,
                        ConfigurationEntryValueType.STRING_ENCRYPTED,
                        IpInfoFreeGeoIpAdapter.REGISTRY_KEY_TOKEN.requiresRestart(),
                        IpInfoFreeGeoIpAdapter.REGISTRY_KEY_TOKEN.constraints().orElse(Collections.emptyList()),
                        "ipinfofree-config"
                )
        );

        return Response.ok(response).build();
    }

    @PUT
    @Path("/providers/ipinfofree/configuration")
    public Response ipInfoFreeConfiguration(IpInfoFreeConfigurationUpdateRequest ur) {
        if (ur.change().isEmpty()) {
            LOG.info("Empty configuration parameters.");
            return Response.status(422).build();
        }

        for (Map.Entry<String, Object> c : ur.change().entrySet()) {
            switch (c.getKey()) {
                case "geoipprov_ipinfo_api_key":
                    if (!ConfigurationEntryConstraintValidator.checkConstraints(IpInfoFreeGeoIpAdapter.REGISTRY_KEY_TOKEN, c)) {
                        return Response.status(422).build();
                    }

                    try {
                        nzyme.getDatabaseCoreRegistry().setEncryptedValue(c.getKey(), c.getValue().toString());
                    } catch (RegistryCryptoException e) {
                        return Response.serverError().build();
                    }

                    break;
                default:
                    LOG.info("Unknown configuration parameter [{}].", c.getKey());
                    return Response.status(422).build();
            }
        }

        return Response.ok().build();
    }

}
