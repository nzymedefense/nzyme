package app.nzyme.core.rest.resources.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.Dot11;
import app.nzyme.core.dot11.db.monitoring.*;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.requests.*;
import app.nzyme.core.rest.responses.dot11.monitoring.*;
import app.nzyme.core.util.Tools;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/dot11/monitoring")
@Produces(MediaType.APPLICATION_JSON)
public class Dot11MonitoredNetworksResource extends TapDataHandlingResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids")
    public Response findAll(@Context SecurityContext sc, @Valid CreateDot11MonitoredNetworkRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        List<MonitoredSSIDDetailsResponse> ssids = Lists.newArrayList();
        for (MonitoredSSID ssid : nzyme.getDot11().findAllMonitoredSSIDs(
                authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId())) {
            boolean isAlerted = false;
            ssids.add(MonitoredSSIDDetailsResponse.create(
                    ssid.uuid(),
                    ssid.ssid(),
                    ssid.organizationId(),
                    ssid.tenantId(),
                    null,
                    null,
                    null,
                    ssid.createdAt(),
                    ssid.updatedAt(),
                    isAlerted
            ));
        }

        return Response.ok(MonitoredSSIDListResponse.create(ssids)).build();
    }

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{uuid}")
    public Response findOne(@Context SecurityContext sc, @PathParam("uuid") UUID uuid) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> allAccessibleTapUUIDs = parseAndValidateTapIds(authenticatedUser, nzyme, "*");

        Optional<MonitoredSSID> result = nzyme.getDot11()
                .findMonitoredSSID(uuid, authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId());

        if (result.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        MonitoredSSID ssid = result.get();
        boolean isAlerted = false;

        // Find all monitored BSSIDs.
        List<MonitoredBSSIDDetailsResponse> bssids = Lists.newArrayList();
        for (MonitoredBSSID bssid : nzyme.getDot11().findMonitoredBSSIDsOfSSID(ssid.id())) {
            List<MonitoredFingerprintResponse> fingerprints = Lists.newArrayList();
            for (MonitoredFingerprint fp : nzyme.getDot11().findMonitoredFingerprintsOfMonitoredBSSID(bssid.id())) {
                fingerprints.add(MonitoredFingerprintResponse.create(fp.uuid(), fp.fingerprint()));
            }

            boolean isOnline = nzyme.getDot11().bssidExist(bssid.bssid(), 15, allAccessibleTapUUIDs);
            boolean bssidIsAlerted = true;

            bssids.add(MonitoredBSSIDDetailsResponse.create(
                    ssid.uuid(),
                    bssid.uuid(),
                    bssid.bssid(),
                    nzyme.getOUIManager().lookupMac(bssid.bssid()),
                    isOnline,
                    bssidIsAlerted,
                    fingerprints
            ));
        }

        // Monitored channels.
        List<MonitoredChannelResponse> channels = Lists.newArrayList();
        for (MonitoredChannel channel : nzyme.getDot11().findMonitoredChannelsOfMonitoredNetwork(ssid.id())) {
            int channelNumber = Dot11.frequencyToChannel((int) channel.frequency());
            channels.add(MonitoredChannelResponse.create(channel.uuid(), channel.frequency(), channelNumber));
        }

        // Monitored security suites.
        List<MonitoredSecuritySuiteResponse> securitySuites = Lists.newArrayList();
        for (MonitoredSecuritySuite suite : nzyme.getDot11().findMonitoredSecuritySuitesOfMonitoredNetwork(ssid.id())) {
            securitySuites.add(MonitoredSecuritySuiteResponse.create(suite.uuid(), suite.securitySuite()));
        }

        return Response.ok(MonitoredSSIDDetailsResponse.create(
                ssid.uuid(),
                ssid.ssid(),
                ssid.organizationId(),
                ssid.tenantId(),
                bssids,
                channels,
                securitySuites,
                ssid.createdAt(),
                ssid.updatedAt(),
                isAlerted
        )).build();
    }

    @POST
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids")
    public Response createMonitoredSSID(@Context SecurityContext sc, @Valid CreateDot11MonitoredNetworkRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        nzyme.getDot11().createMonitoredSSID(
                req.ssid(),
                authenticatedUser.getOrganizationId(),
                authenticatedUser.getTenantId()
        );

        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{uuid}/bssids")
    public Response createMonitoredBSSID(@Context SecurityContext sc,
                                         @PathParam("uuid") UUID ssidUUID,
                                         @Valid CreateDot11MonitoredBSSIDRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<MonitoredSSID> ssid = nzyme.getDot11()
                .findMonitoredSSID(ssidUUID, authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId());

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!Tools.isValidMacAddress(req.bssid())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        nzyme.getDot11().createMonitoredBSSID(ssid.get().id(), req.bssid());

        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{ssid_uuid}/bssids/show/{bssid_uuid}")
    public Response deleteMonitoredBSSID(@Context SecurityContext sc,
                                         @PathParam("ssid_uuid") UUID ssidUUID,
                                         @PathParam("bssid_uuid") UUID bssidUUID) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<Long> bssidId = nzyme.getDot11().findMonitoredBSSIDId(ssidUUID, bssidUUID,
                authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId());

        if (bssidId.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().deleteMonitoredBSSID(bssidId.get());

        return Response.ok().build();
    }

    @POST
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{ssid_uuid}/bssids/show/{bssid_uuid}/fingerprints")
    public Response createMonitoredBSSIDFingerprint(@Context SecurityContext sc,
                                                    @PathParam("ssid_uuid") UUID ssidUUID,
                                                    @PathParam("bssid_uuid") UUID bssidUUID,
                                                    @Valid CreateDot11MonitoredBSSIDFingerprintRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<Long> bssidId = nzyme.getDot11().findMonitoredBSSIDId(ssidUUID, bssidUUID,
                authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId());

        if (bssidId.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (req.fingerprint().length() != 64) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        nzyme.getDot11().createdMonitoredBSSIDFingerprint(bssidId.get(), req.fingerprint());

        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{ssid_uuid}/bssids/show/{bssid_uuid}/fingerprints/show/{fingerprint_uuid}")
    public Response deleteMonitoredBSSIDFingerprint(@Context SecurityContext sc,
                                                    @PathParam("ssid_uuid") UUID ssidUUID,
                                                    @PathParam("bssid_uuid") UUID bssidUUID,
                                                    @PathParam("fingerprint_uuid") UUID fingerprintUUID) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<Long> bssidId = nzyme.getDot11().findMonitoredBSSIDId(ssidUUID, bssidUUID,
                authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId());

        if (bssidId.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().deleteMonitoredBSSIDFingerprint(bssidId.get(), fingerprintUUID);

        return Response.ok().build();
    }

    @POST
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{uuid}/channels")
    public Response createMonitoredSSIDChannel(@Context SecurityContext sc,
                                               @PathParam("uuid") UUID ssidUUID,
                                               @Valid CreateDot11MonitoredChannelRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<MonitoredSSID> ssid = nzyme.getDot11()
                .findMonitoredSSID(ssidUUID, authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId());

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (Dot11.frequencyToChannel((int) req.frequency()) == -1) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        List<Long> existingChannels = Lists.newArrayList();
        for (MonitoredChannel channel : nzyme.getDot11().findMonitoredChannelsOfMonitoredNetwork(ssid.get().id())) {
            existingChannels.add(channel.frequency());
        }
        if (existingChannels.contains(req.frequency())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        nzyme.getDot11().createMonitoredChannel(ssid.get().id(), req.frequency());

        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{ssid_uuid}/channels/show/{channel_uuid}")
    public Response deleteMonitoredSSIDChannel(@Context SecurityContext sc,
                                               @PathParam("ssid_uuid") UUID ssidUUID,
                                               @PathParam("channel_uuid") UUID channelUUID) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<MonitoredSSID> ssid = nzyme.getDot11()
                .findMonitoredSSID(ssidUUID, authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId());

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().deleteMonitoredChannel(ssid.get().id(), channelUUID);

        return Response.ok().build();
    }

    @POST
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{uuid}/securitysuites")
    public Response createMonitoredSSIDSecuritySuite(@Context SecurityContext sc,
                                                     @PathParam("uuid") UUID ssidUUID,
                                                     @Valid CreateDot11MonitoredSecuritySuiteRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<MonitoredSSID> ssid = nzyme.getDot11()
                .findMonitoredSSID(ssidUUID, authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId());

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // TODO we might want to tighten this down in the future.
        if (!req.suite().contains("-") || !req.suite().contains("/")) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<String> existingSuites = Lists.newArrayList();
        for (MonitoredSecuritySuite suite : nzyme.getDot11().findMonitoredSecuritySuitesOfMonitoredNetwork(ssid.get().id())) {
            existingSuites.add(suite.securitySuite());
        }
        if (existingSuites.contains(req.suite())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        nzyme.getDot11().createMonitoredSecuritySuite(ssid.get().id(), req.suite());

        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{ssid_uuid}/securitysuites/show/{suite_uuid}")
    public Response deleteMonitoredSSIDSecuritySuite(@Context SecurityContext sc,
                                                     @PathParam("ssid_uuid") UUID ssidUUID,
                                                     @PathParam("suite_uuid") UUID suiteUUID) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<MonitoredSSID> ssid = nzyme.getDot11()
                .findMonitoredSSID(ssidUUID, authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId());

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().deleteMonitoredSecuritySuite(ssid.get().id(), suiteUUID);

        return Response.ok().build();
    }

}
