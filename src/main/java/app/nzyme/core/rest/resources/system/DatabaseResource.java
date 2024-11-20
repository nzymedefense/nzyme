package app.nzyme.core.rest.resources.system;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.DataCategory;
import app.nzyme.core.database.DatabaseImpl;
import app.nzyme.core.dot11.Dot11RegistryKeys;
import app.nzyme.core.ethernet.EthernetRegistryKeys;
import app.nzyme.core.rest.requests.RetentionTimeConfigurationUpdateRequest;
import app.nzyme.core.rest.responses.bluetooth.BluetoothRegistryKeys;
import app.nzyme.core.rest.responses.system.database.*;
import app.nzyme.core.security.authentication.db.OrganizationEntry;
import app.nzyme.core.security.authentication.db.TenantEntry;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraintValidator;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
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
        // Global/Total database size.
        Map<String, DataCategorySizesResponse> globalSizes = Maps.newHashMap();
        for (DataCategory category : DataCategory.values()) {
            globalSizes.put(category.name(), DataCategorySizesResponse.create(
                    calculateDataCategorySize(category),
                    countDataCategoryRows(category, null, null))
            );
        }

        // Organizations.
        List<OrganizationDataCategoriesResponse> organizations = Lists.newArrayList();
        for (OrganizationEntry org : nzyme.getAuthenticationService().findAllOrganizations()) {
            Map<String, DataCategorySizesResponse> orgSizes = Maps.newHashMap();
            for (DataCategory category : DataCategory.values()) {
                orgSizes.put(category.name(), DataCategorySizesResponse.create(
                        null, countDataCategoryRows(category, org.uuid(), null)
                ));
            }

            // Tenants.
            List<TenantDataCategoriesResponse> tenants = Lists.newArrayList();
            for (TenantEntry tenant : nzyme.getAuthenticationService().findAllTenantsOfOrganization(org.uuid())) {
                Map<String, DataCategorySizesAndConfigurationResponse> tenantSizes = Maps.newHashMap();

                for (DataCategory category : DataCategory.values()) {
                    tenantSizes.put(category.name(), DataCategorySizesAndConfigurationResponse.create(
                            null,
                            countDataCategoryRows(category, org.uuid(), tenant.uuid()),
                            getDataCategoryRetentionTimeDays(category, org.uuid(), tenant.uuid())
                    ));
                }

                tenants.add(TenantDataCategoriesResponse.create(
                        tenant.uuid(),
                        tenant.name(),
                        tenantSizes
                ));
            }

            organizations.add(OrganizationDataCategoriesResponse.create(
                    org.uuid(),
                    org.name(),
                    orgSizes,
                    tenants
            ));
        }


        return Response.ok(GlobalDatabaseCategoriesResponse.create(globalSizes, organizations)).build();
    }

    // TODO for org

    // TODO for tenant

    private long countDataCategoryRows(DataCategory category,
                                       @Nullable UUID organizationId,
                                       @Nullable UUID tenantId) {
        throw new RuntimeException("Not implemented.");
    }


    public long calculateDataCategorySize(DataCategory category) {
        List<String> tableNames = Lists.newArrayList();

        switch (category) {
            case DOT11 -> {
                tableNames.add("dot11_bssids");
                tableNames.add("dot11_channels");
                tableNames.add("dot11_fingerprints");
                tableNames.add("dot11_ssids");
                tableNames.add("dot11_ssid_settings");
                tableNames.add("dot11_infrastructure_types");
                tableNames.add("dot11_bssid_clients");
                tableNames.add("dot11_rates");
                tableNames.add("dot11_clients");
                tableNames.add("dot11_client_probereq_ssids");
                tableNames.add("dot11_channel_histograms");
                tableNames.add("dot11_disco_activity");
                tableNames.add("dot11_disco_activity_receivers");
                tableNames.add("dot11_known_clients");
                tableNames.add("dot11_known_networks");
            }
            case BLUETOOTH -> {
                tableNames.add("bluetooth_devices");
            }
            case ETHERNET_L4 -> {
                tableNames.add("l4_sessions");
                tableNames.add("ssh_sessions");
                tableNames.add("socks_tunnels");
            }
            case ETHERNET_DNS -> {
                tableNames.add("dns_log");
                tableNames.add("dns_entropy_log");
                tableNames.add("dns_pairs");
                tableNames.add("dns_statistics");
            }
        }

        long size = 0;
        DatabaseImpl database = (DatabaseImpl) nzyme.getDatabase();
        for (String table : tableNames) {
            size += database.getTableSize(table);
        }

        return size;
    }


    private int getDataCategoryRetentionTimeDays(DataCategory category,
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
