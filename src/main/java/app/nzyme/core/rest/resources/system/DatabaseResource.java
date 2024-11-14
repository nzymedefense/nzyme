package app.nzyme.core.rest.resources.system;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.DataCategory;
import app.nzyme.core.database.DatabaseImpl;
import app.nzyme.core.dot11.Dot11RegistryKeys;
import app.nzyme.core.ethernet.EthernetRegistryKeys;
import app.nzyme.core.rest.requests.RetentionTimeConfigurationUpdateRequest;
import app.nzyme.core.rest.responses.bluetooth.BluetoothRegistryKeys;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraintValidator;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.UUID;

@Path("/api/system/database")
@Produces(MediaType.APPLICATION_JSON)
public class DatabaseResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
    @Path("/retention/settings/global")
    public Response globalRetentionSettings() {
        DatabaseImpl db = (DatabaseImpl) nzyme.getDatabase();

        // GlobalDatabaseCategoriesResponse
    }


    private long getDataCategoryRetentionTimeDays(DataCategory category,
                                                  @NotNull UUID organizationId,
                                                  @NotNull UUID tenantId) {
        String key = null;
        String defaultValue = null;
        switch (category) {
            case DOT11 -> {
                key = Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.key();
                defaultValue = Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.defaultValue().orElse("MISSING");
            }
            case BLUETOOTH -> {
                key = BluetoothRegistryKeys.BLUETOOTH_RETENTION_TIME_DAYS.key();
                defaultValue = BluetoothRegistryKeys.BLUETOOTH_RETENTION_TIME_DAYS.defaultValue().orElse("MISSING");

            }
            case ETHERNET_L4 -> {
                key = EthernetRegistryKeys.L4_RETENTION_TIME_DAYS.key();
                defaultValue = EthernetRegistryKeys.L4_RETENTION_TIME_DAYS.defaultValue().orElse("MISSING");

            }
            case ETHERNET_DNS -> {
                key = EthernetRegistryKeys.DNS_RETENTION_TIME_DAYS.key();
                defaultValue = EthernetRegistryKeys.DNS_RETENTION_TIME_DAYS.defaultValue().orElse("MISSING");

            }
        }

        return Integer.parseInt(nzyme.getDatabaseCoreRegistry()
                .getValue(key, organizationId, tenantId)
                .orElse(defaultValue));
    }

}
