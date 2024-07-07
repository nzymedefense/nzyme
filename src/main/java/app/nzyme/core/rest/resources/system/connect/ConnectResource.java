package app.nzyme.core.rest.resources.system.connect;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.connect.ConnectRegistryKeys;
import app.nzyme.core.rest.requests.ConnectConfigurationUpdateRequest;
import app.nzyme.core.rest.responses.connect.ConnectConfigurationResponse;
import app.nzyme.plugin.RegistryCryptoException;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraintValidator;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryResponse;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryValueType;
import app.nzyme.plugin.rest.configuration.EncryptedConfigurationEntryResponse;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;

@Produces(MediaType.APPLICATION_JSON)
@Path("/api/system/connect")
public class ConnectResource {

    private static final Logger LOG = LogManager.getLogger(ConnectResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    @RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
    @Path("/configuration")
    public Response configuration() {
        boolean connectEnabled = nzyme.getDatabaseCoreRegistry()
                .getValue(ConnectRegistryKeys.CONNECT_ENABLED.key())
                .filter(Boolean::parseBoolean).isPresent();

        boolean apiKeyIsSet;
        try {
            apiKeyIsSet = nzyme.getDatabaseCoreRegistry()
                    .getEncryptedValue(ConnectRegistryKeys.CONNECT_API_KEY.key())
                    .isPresent();
        } catch(RegistryCryptoException e) {
            LOG.error("Could not decrypt encrypted registry value.", e);
            return Response.serverError().build();
        }

        ConnectConfigurationResponse response = ConnectConfigurationResponse.create(
                ConfigurationEntryResponse.create(
                        ConnectRegistryKeys.CONNECT_ENABLED.key(),
                        "Connect Enabled",
                        connectEnabled,
                        ConfigurationEntryValueType.BOOLEAN,
                        ConnectRegistryKeys.CONNECT_ENABLED.defaultValue().orElse(null),
                        ConnectRegistryKeys.CONNECT_ENABLED.requiresRestart(),
                        ConnectRegistryKeys.CONNECT_ENABLED.constraints().orElse(Collections.emptyList()),
                        "nzyme-connect"
                ),
                EncryptedConfigurationEntryResponse.create(
                        ConnectRegistryKeys.CONNECT_API_KEY.key(),
                        "API Key",
                        apiKeyIsSet,
                        ConfigurationEntryValueType.STRING_ENCRYPTED,
                        ConnectRegistryKeys.CONNECT_API_KEY.requiresRestart(),
                        ConnectRegistryKeys.CONNECT_API_KEY.constraints().orElse(Collections.emptyList()),
                        "nzyme-connect"
                )
        );

        return Response.ok(response).build();
    }

    @PUT
    @RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
    @Path("/configuration")
    public Response update(ConnectConfigurationUpdateRequest ur) {
        if (ur.change().isEmpty()) {
            LOG.info("Empty configuration parameters.");
            return Response.status(422).build();
        }

        for (Map.Entry<String, Object> c : ur.change().entrySet()) {
            boolean encrypted;
            switch (c.getKey()) {
                case "connect_enabled":
                    if (!ConfigurationEntryConstraintValidator.checkConstraints(ConnectRegistryKeys.CONNECT_ENABLED, c)) {
                        return Response.status(422).build();
                    }
                    encrypted = false;
                    break;
                case "connect_api_key":
                    if (!ConfigurationEntryConstraintValidator.checkConstraints(ConnectRegistryKeys.CONNECT_API_KEY, c)) {
                        return Response.status(422).build();
                    }
                    encrypted = true;
                    break;
                default:
                    LOG.info("Unknown configuration parameter [{}].", c.getKey());
                    return Response.status(422).build();
            }

            if (encrypted) {
                try {
                    nzyme.getDatabaseCoreRegistry().setEncryptedValue(c.getKey(), c.getValue().toString());
                } catch (RegistryCryptoException e) {
                    return Response.serverError().build();
                }
            } else {
                nzyme.getDatabaseCoreRegistry().setValue(c.getKey(), c.getValue().toString());
            }
        }

        return Response.ok().build();
    }

}
