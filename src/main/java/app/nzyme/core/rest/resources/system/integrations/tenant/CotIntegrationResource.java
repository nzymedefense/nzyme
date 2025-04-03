package app.nzyme.core.rest.resources.system.integrations.tenant;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.crypto.Crypto;
import app.nzyme.core.integrations.tenant.cot.transports.CotTransportType;
import app.nzyme.core.integrations.tenant.cot.db.CotOutputEntry;
import app.nzyme.core.quota.QuotaType;
import app.nzyme.core.rest.UserAuthenticatedResource;
import app.nzyme.core.rest.responses.integrations.tenant.cot.CotOutputDetailsResponse;
import app.nzyme.core.rest.responses.integrations.tenant.cot.CotOutputListResponse;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/system/authentication/mgmt/organizations/show/{organizationId}/tenants/show/{tenantId}/integrations/cot")
@RESTSecured(PermissionLevel.ORGADMINISTRATOR)
@Produces(MediaType.APPLICATION_JSON)
public class CotIntegrationResource extends UserAuthenticatedResource  {

    private static final Logger LOG = LogManager.getLogger(CotIntegrationResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    public Response all(@Context SecurityContext sc,
                        @PathParam("organizationId") UUID organizationId,
                        @PathParam("tenantId") UUID tenantId,
                        @QueryParam("limit") int limit,
                        @QueryParam("offset") int offset) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        long count = nzyme.getCotService().countAllOutputsOfTenant(organizationId, tenantId);
        List<CotOutputDetailsResponse> outputs = Lists.newArrayList();
        for (CotOutputEntry e : nzyme.getCotService().findAllOutputsOfTenant(organizationId, tenantId, limit, offset)) {
            outputs.add(CotOutputDetailsResponse.create(
                    e.uuid(),
                    e.organizationId(),
                    e.tenantId(),
                    e.connectionType(),
                    e.name(),
                    e.description(),
                    e.leafTypeTap(),
                    e.address(),
                    e.port(),
                    e.status(),
                    e.sentMessages(),
                    e.sentBytes(),
                    e.updatedAt(),
                    e.createdAt()
            ));
        }

        return Response.ok(CotOutputListResponse.create(count, outputs)).build();
    }

    @GET
    @Path("/show/{outputId}")
    public Response one(@Context SecurityContext sc,
                        @PathParam("organizationId") UUID organizationId,
                        @PathParam("tenantId") UUID tenantId,
                        @PathParam("outputId") UUID outputId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<CotOutputEntry> output = nzyme.getCotService().findOutput(organizationId, tenantId, outputId);

        if (output.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        CotOutputEntry o = output.get();

        return Response.ok(
                CotOutputDetailsResponse.create(
                        o.uuid(),
                        o.organizationId(),
                        o.tenantId(),
                        o.connectionType(),
                        o.name(),
                        o.description(),
                        o.leafTypeTap(),
                        o.address(),
                        o.port(),
                        o.status(),
                        o.sentMessages(),
                        o.sentBytes(),
                        o.updatedAt(),
                        o.createdAt()
                )
        ).build();
    }

    @POST
    public Response create(@Context SecurityContext sc,
                           @PathParam("organizationId") UUID organizationId,
                           @PathParam("tenantId") UUID tenantId,
                           @NotBlank @FormDataParam("connection_type") String connectionType,
                           @NotBlank @FormDataParam("name") String name,
                           @Nullable @FormDataParam("description") String description,
                           @NotBlank @FormDataParam("tap_leaf_type") String leafTypeTap,
                           @NotBlank @FormDataParam("address") String address,
                           @Min(1) @Max(65535) @FormDataParam("port") int port,
                           @Nullable @FormDataParam("certificate") InputStream certificate,
                           @Nullable @FormDataParam("certificate_passphrase") String certificatePassphrase) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if there is room in the quota.
        if (!nzyme.getQuotaService().isTenantQuotaAvailable(organizationId, tenantId, QuotaType.INTEGRATIONS_COT)) {
            return Response.status(422).build();
        }

        CotTransportType transportType;
        try {
            transportType = CotTransportType.valueOf(connectionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        byte[] encryptedCertificate;
        byte[] encryptedCertificatePassphrase;
        try {
            if (certificate == null) {
                encryptedCertificate = null;
                encryptedCertificatePassphrase = null;
            } else {
                if (certificatePassphrase == null || certificatePassphrase.trim().isEmpty()) {
                    encryptedCertificatePassphrase = null;
                } else {
                    encryptedCertificatePassphrase = nzyme.getCrypto()
                            .encryptWithClusterKey(certificatePassphrase.getBytes(Charsets.UTF_8));
                }
                encryptedCertificate = nzyme.getCrypto().encryptWithClusterKey(certificate.readAllBytes());
            }
        } catch (IOException | Crypto.CryptoOperationException e) {
            LOG.error("Could not encrypt CoT client certificate.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        nzyme.getCotService().createOutput(
                organizationId,
                tenantId,
                transportType,
                name,
                description,
                leafTypeTap,
                address,
                port,
                encryptedCertificate,
                encryptedCertificatePassphrase
        );

        return Response.ok(Response.Status.CREATED).build();
    }


    @POST
    @Path("/show/{outputId}/update")
    public Response edit(@Context SecurityContext sc,
                         @PathParam("organizationId") UUID organizationId,
                         @PathParam("tenantId") UUID tenantId,
                         @PathParam("outputId") UUID outputId,
                         @NotBlank @FormDataParam("connection_type") String connectionType,
                         @NotBlank @FormDataParam("name") String name,
                         @Nullable @FormDataParam("description") String description,
                         @NotBlank @FormDataParam("tap_leaf_type") String leafTypeTap,
                         @NotBlank @FormDataParam("address") String address,
                         @Min(1) @Max(65535) @FormDataParam("port") int port) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<CotOutputEntry> output = nzyme.getCotService().findOutput(organizationId, tenantId, outputId);

        if (output.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        CotTransportType transportType;
        try {
            transportType = CotTransportType.valueOf(connectionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        nzyme.getCotService().updateOutput(
                output.get().id(),
                transportType,
                name,
                description,
                leafTypeTap,
                address,
                port
        );

        return Response.ok().build();
    }


    @POST
    @Path("/show/{outputId}/certificate/update")
    public Response editCertificate(@Context SecurityContext sc,
                                    @PathParam("organizationId") UUID organizationId,
                                    @PathParam("tenantId") UUID tenantId,
                                    @PathParam("outputId") UUID outputId,
                                    @Nullable @FormDataParam("certificate") InputStream certificate,
                                    @Nullable @FormDataParam("certificate_passphrase") String certificatePassphrase) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<CotOutputEntry> output = nzyme.getCotService().findOutput(organizationId, tenantId, outputId);

        if (output.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        CotTransportType transportType;
        try {
            transportType = CotTransportType.valueOf(output.get().connectionType());
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (transportType != CotTransportType.TCP_X509) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        byte[] encryptedCertificate;
        byte[] encryptedCertificatePassphrase;
        try {
            if (certificate == null) {
                encryptedCertificate = null;
                encryptedCertificatePassphrase = null;
            } else {
                if (certificatePassphrase == null || certificatePassphrase.trim().isEmpty()) {
                    encryptedCertificatePassphrase = null;
                } else {
                    encryptedCertificatePassphrase = nzyme.getCrypto()
                            .encryptWithClusterKey(certificatePassphrase.getBytes(Charsets.UTF_8));
                }
                encryptedCertificate = nzyme.getCrypto().encryptWithClusterKey(certificate.readAllBytes());
            }
        } catch (IOException | Crypto.CryptoOperationException e) {
            LOG.error("Could not encrypt CoT client certificate.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        nzyme.getCotService().updateOutputCertificate(
                output.get().id(),
                encryptedCertificate,
                encryptedCertificatePassphrase
        );

        return Response.ok().build();
    }

    @DELETE
    @Path("/show/{outputId}")
    public Response delete(@Context SecurityContext sc,
                           @PathParam("organizationId") UUID organizationId,
                           @PathParam("tenantId") UUID tenantId,
                           @PathParam("outputId") UUID outputId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<CotOutputEntry> output = nzyme.getCotService().findOutput(organizationId, tenantId, outputId);

        if (output.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getCotService().deleteOutput(output.get().id());

        return Response.ok().build();
    }

    @PUT
    @Path("/show/{outputId}/pause")
    public Response pause(@Context SecurityContext sc,
                          @PathParam("organizationId") UUID organizationId,
                          @PathParam("tenantId") UUID tenantId,
                          @PathParam("outputId") UUID outputId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<CotOutputEntry> output = nzyme.getCotService().findOutput(organizationId, tenantId, outputId);

        if (output.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getCotService().pauseOutput(output.get().id());

        return Response.ok().build();
    }

    @PUT
    @Path("/show/{outputId}/start")
    public Response start(@Context SecurityContext sc,
                          @PathParam("organizationId") UUID organizationId,
                          @PathParam("tenantId") UUID tenantId,
                          @PathParam("outputId") UUID outputId) {
        if (!passedTenantDataAccessible(sc, organizationId, tenantId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<CotOutputEntry> output = nzyme.getCotService().findOutput(organizationId, tenantId, outputId);

        if (output.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getCotService().startOutput(output.get().id());

        return Response.ok().build();
    }


}
