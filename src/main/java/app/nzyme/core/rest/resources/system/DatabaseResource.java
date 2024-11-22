package app.nzyme.core.rest.resources.system;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.DataCategory;
import app.nzyme.core.database.DataTableInformation;
import app.nzyme.core.database.DatabaseImpl;
import app.nzyme.core.database.tasks.GlobalPurgeCategoryTask;
import app.nzyme.core.database.tasks.OrganizationPurgeCategoryTask;
import app.nzyme.core.database.tasks.TenantPurgeCategoryTask;
import app.nzyme.core.dot11.Dot11RegistryKeys;
import app.nzyme.core.ethernet.EthernetRegistryKeys;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.responses.bluetooth.BluetoothRegistryKeys;
import app.nzyme.core.rest.responses.system.database.*;
import app.nzyme.core.security.authentication.db.OrganizationEntry;
import app.nzyme.core.security.authentication.db.TenantEntry;
import app.nzyme.core.util.Tools;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/system/database")
@Produces(MediaType.APPLICATION_JSON)
public class DatabaseResource extends UserAuthenticatedResource {

    private static final Logger LOG = LogManager.getLogger(DatabaseResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    @RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
    @Path("/sizes/global")
    public Response globalDatabaseSizes() {
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
                        org.uuid(),
                        org.name(),
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

    @POST
    @RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
    @Path("/purge/category/{category}")
    public Response purgeGlobalCategory(@PathParam("category") String categoryParam) {
        DataCategory category;
        try {
            category = DataCategory.valueOf(categoryParam.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        LOG.info("Submitting tasks to globally purge data category [{}] on API request.", category);
        nzyme.getTasksQueue().publish(new GlobalPurgeCategoryTask(category));
        return Response.status(Response.Status.ACCEPTED).build();
    }

    @POST
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/purge/organization/{organization_id}/category/{category}")
    public Response purgeOrganizationCategory(@Context SecurityContext sc,
                                              @PathParam("category") String categoryParam,
                                              @PathParam("organization_id") UUID organizationId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (authenticatedUser.getOrganizationId() != null
                && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        DataCategory category;
        try {
            category = DataCategory.valueOf(categoryParam.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        LOG.info("Submitting tasks to purge data category [{}] of organization [{}] on API request.",
                category, organizationId);

        nzyme.getTasksQueue().publish(new OrganizationPurgeCategoryTask(category, organizationId));
        return Response.status(Response.Status.ACCEPTED).build();
    }

    @POST
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/purge/organization/{organization_id}/tenant/{tenant_id}/category/{category}")
    public Response purgeTenantCategory(@Context SecurityContext sc,
                                              @PathParam("category") String categoryParam,
                                              @PathParam("organization_id") UUID organizationId,
                                              @PathParam("tenant_id") UUID tenantId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        DataCategory category;
        try {
            category = DataCategory.valueOf(categoryParam.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        LOG.info("Submitting tasks to purge data category [{}] of tenant [{}] on API request.",
                category, tenantId);

        // TODO set purge cutoff at task creation


        nzyme.getTasksQueue().publish(new TenantPurgeCategoryTask(category, organizationId, tenantId));
        return Response.status(Response.Status.ACCEPTED).build();
    }

    private long countDataCategoryRows(DataCategory category,
                                       @Nullable UUID organizationId,
                                       @Nullable UUID tenantId) {
        List<DataTableInformation> tables = ((DatabaseImpl) nzyme.getDatabase()).getTablesOfDataCategory(category);

        long rows = 0;
        for (DataTableInformation table : tables) {
            List<UUID> tapUuids = Tools.getTapUuids(nzyme, organizationId, tenantId);

            if (tapUuids.isEmpty()) {
                continue;
            }

            rows += nzyme.getDatabase().withHandle(handle ->
                    handle.createQuery(table.getRowCountQuery())
                            .bindList("taps", tapUuids)
                            .mapTo(Long.class)
                            .one()
            );
        }

        return rows;
    }

    public long calculateDataCategorySize(DataCategory category) {
        List<DataTableInformation> tables = ((DatabaseImpl) nzyme.getDatabase()).getTablesOfDataCategory(category);

        long size = 0;
        DatabaseImpl database = (DatabaseImpl) nzyme.getDatabase();
        for (DataTableInformation table : tables) {
            size += database.getTableSize(table.getTableName());
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
