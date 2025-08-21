package app.nzyme.core.rest.resources.system;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.database.DataCategory;
import app.nzyme.core.database.DataTableInformation;
import app.nzyme.core.database.DatabaseImpl;
import app.nzyme.core.database.DatabaseTools;
import app.nzyme.core.database.tasks.GlobalPurgeCategoryTask;
import app.nzyme.core.database.tasks.OrganizationPurgeCategoryTask;
import app.nzyme.core.database.tasks.TenantPurgeCategoryTask;
import app.nzyme.core.dot11.Dot11RegistryKeys;
import app.nzyme.core.ethernet.EthernetRegistryKeys;
import app.nzyme.core.gnss.GNSSRegistryKeys;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.requests.SetDatabaseCategoryRetentionTimeRequest;
import app.nzyme.core.rest.responses.bluetooth.BluetoothRegistryKeys;
import app.nzyme.core.rest.responses.system.database.*;
import app.nzyme.core.security.authentication.db.OrganizationEntry;
import app.nzyme.core.security.authentication.db.TenantEntry;
import app.nzyme.core.uav.UavRegistryKeys;
import app.nzyme.core.util.Tools;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
                            DatabaseTools.getDataCategoryRetentionTimeDays(nzyme, category, org.uuid(), tenant.uuid())
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

    @GET
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/sizes/organization/{organization_id}")
    public Response organizationDatabaseSizes(@Context SecurityContext sc,
                                              @PathParam("organization_id") UUID organizationId) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (authenticatedUser.getOrganizationId() != null
                && !authenticatedUser.getOrganizationId().equals(organizationId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        Optional<OrganizationEntry> org = nzyme.getAuthenticationService().findOrganization(organizationId);

        if (org.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Map<String, DataCategorySizesResponse> orgSizes = Maps.newHashMap();
        for (DataCategory category : DataCategory.values()) {
            orgSizes.put(category.name(), DataCategorySizesResponse.create(
                    null, countDataCategoryRows(category, org.get().uuid(), null)
            ));
        }

        // Tenants.
        List<TenantDataCategoriesResponse> tenants = Lists.newArrayList();
        for (TenantEntry tenant : nzyme.getAuthenticationService().findAllTenantsOfOrganization(org.get().uuid())) {
            Map<String, DataCategorySizesAndConfigurationResponse> tenantSizes = Maps.newHashMap();

            for (DataCategory category : DataCategory.values()) {
                tenantSizes.put(category.name(), DataCategorySizesAndConfigurationResponse.create(
                        null,
                        countDataCategoryRows(category, org.get().uuid(), tenant.uuid()),
                        DatabaseTools.getDataCategoryRetentionTimeDays(nzyme, category, org.get().uuid(), tenant.uuid())
                ));
            }

            tenants.add(TenantDataCategoriesResponse.create(
                    tenant.uuid(),
                    tenant.name(),
                    org.get().uuid(),
                    org.get().name(),
                    tenantSizes
            ));
        }

        return Response.ok(
                OrganizationDataCategoriesResponse.create(org.get().uuid(), org.get().name(), orgSizes, tenants)
        ).build();
    }

    @GET
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/sizes/organization/{organization_id}/tenants/{tenant_id}")
    public Response tenantDatabaseSizes(@Context SecurityContext sc,
                                        @PathParam("organization_id") UUID organizationId,
                                        @PathParam("tenant_id") UUID tenantId){
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        Optional<TenantEntry> tenant = nzyme.getAuthenticationService().findTenant(tenantId);
        Optional<OrganizationEntry> org = nzyme.getAuthenticationService().findOrganization(organizationId);

        if (tenant.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Map<String, DataCategorySizesAndConfigurationResponse> tenantSizes = Maps.newHashMap();
        for (DataCategory category : DataCategory.values()) {
            tenantSizes.put(category.name(), DataCategorySizesAndConfigurationResponse.create(
                    null,
                    countDataCategoryRows(category, org.get().uuid(), tenant.get().uuid()),
                    DatabaseTools.getDataCategoryRetentionTimeDays(nzyme, category, org.get().uuid(), tenant.get().uuid())
            ));
        }

        return Response.ok(TenantDataCategoriesResponse.create(
                tenant.get().uuid(), tenant.get().name(), org.get().uuid(), org.get().name(), tenantSizes
        )).build();
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
        nzyme.getTasksQueue().publish(new GlobalPurgeCategoryTask(category, DateTime.now()));
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

        nzyme.getTasksQueue().publish(new OrganizationPurgeCategoryTask(category, organizationId, DateTime.now()));
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

        nzyme.getTasksQueue().publish(new TenantPurgeCategoryTask(category, organizationId, tenantId, DateTime.now()));
        return Response.status(Response.Status.ACCEPTED).build();
    }

    @PUT
    @RESTSecured(PermissionLevel.ORGADMINISTRATOR)
    @Path("/configuration/organization/{organization_id}/tenant/{tenant_id}/category/{category}/retention")
    public Response setCategoryRetentionTime(@Context SecurityContext sc,
                                             @Valid SetDatabaseCategoryRetentionTimeRequest req,
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

        LOG.info("Setting retention time of data category [{}] of tenant [{}] to [{}] on API request.",
                category, tenantId, req.retentionTimeDays());

        String key = null;
        switch (category) {
            case DOT11 -> key = Dot11RegistryKeys.DOT11_RETENTION_TIME_DAYS.key();
            case BLUETOOTH -> key = BluetoothRegistryKeys.BLUETOOTH_RETENTION_TIME_DAYS.key();
            case ETHERNET_L4 -> key = EthernetRegistryKeys.L4_RETENTION_TIME_DAYS.key();
            case ETHERNET_DNS -> key = EthernetRegistryKeys.DNS_RETENTION_TIME_DAYS.key();
            case UAV -> key = UavRegistryKeys.UAV_RETENTION_TIME_DAYS.key();
            case GNSS -> key = GNSSRegistryKeys.GNSS_RETENTION_TIME_DAYS.key();
        }

        nzyme.getDatabaseCoreRegistry()
                .setValue(key, String.valueOf(req.retentionTimeDays()), organizationId, tenantId);

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

}
