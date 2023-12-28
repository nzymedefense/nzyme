package app.nzyme.core.rest.resources.dot11;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.context.db.MacAddressContextEntry;
import app.nzyme.core.detection.alerts.DetectionType;
import app.nzyme.core.detection.alerts.db.DetectionAlertEntry;
import app.nzyme.core.dot11.Dot11;
import app.nzyme.core.dot11.db.Dot11SecuritySuiteJson;
import app.nzyme.core.dot11.db.monitoring.*;
import app.nzyme.core.rest.TapDataHandlingResource;
import app.nzyme.core.rest.authentication.AuthenticatedUser;
import app.nzyme.core.rest.requests.*;
import app.nzyme.core.rest.responses.dot11.Dot11MacAddressContextResponse;
import app.nzyme.core.rest.responses.dot11.Dot11MacAddressResponse;
import app.nzyme.core.rest.responses.dot11.SSIDSimilarityResponse;
import app.nzyme.core.rest.responses.dot11.monitoring.*;
import app.nzyme.core.rest.responses.dot11.monitoring.configimport.*;
import app.nzyme.core.util.Tools;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import info.debatty.java.stringsimilarity.JaroWinkler;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Path("/api/dot11/monitoring")
@Produces(MediaType.APPLICATION_JSON)
public class Dot11MonitoredNetworksResource extends TapDataHandlingResource {

    private static final Logger LOG = LogManager.getLogger(Dot11MonitoredNetworksResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids")
    public Response findAll(@Context SecurityContext sc) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        List<MonitoredSSIDSummaryResponse> ssids = Lists.newArrayList();
        for (MonitoredSSID ssid : nzyme.getDot11().findAllMonitoredSSIDs(
                authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId())) {

            boolean isAlerted = false;
            for (DetectionAlertEntry alert : nzyme.getDetectionAlertService()
                    .findAllActiveAlertsOfMonitoredNetwork(ssid.uuid())) {
                DetectionType detectionType;
                try {
                    detectionType = DetectionType.valueOf(alert.detectionType());
                } catch(IllegalArgumentException e) {
                    LOG.error("Invalid detection type [{}]. Skipping.", alert.detectionType());
                    continue;
                }

                switch (detectionType) {
                    case DOT11_MONITOR_BSSID:
                        if (!ssid.enabledUnexpectedBSSID()) {
                            continue;
                        }
                        isAlerted = true;
                        break;
                    case DOT11_MONITOR_CHANNEL:
                        if (!ssid.enabledUnexpectedChannel()) {
                            continue;
                        }
                        isAlerted = true;
                        break;
                    case DOT11_MONITOR_SECURITY_SUITE:
                        if (!ssid.enabledUnexpectedSecuritySuites()) {
                            continue;
                        }
                        isAlerted = true;
                        break;
                    case DOT11_MONITOR_FINGERPRINT:
                        if (!ssid.enabledUnexpectedFingerprint()) {
                            continue;
                        }
                        isAlerted = true;
                        break;
                    case DOT11_MONITOR_SIGNAL_TRACK:
                        if (!ssid.enabledUnexpectedSignalTracks()) {
                            continue;
                        }
                        isAlerted = true;
                        break;
                    case DOT11_MONITOR_DISCO_ANOMALIES:
                        isAlerted = true;
                        break;
                    case DOT11_MONITOR_SIMILAR_LOOKING_SSID:
                        if (!ssid.enabledSimilarLookingSSID()) {
                            continue;
                        }
                        isAlerted = true;
                        break;
                    case DOT11_MONITOR_SSID_SUBSTRING:
                        if (!ssid.enabledSSIDSubstring()) {
                            continue;
                        }
                        isAlerted = true;
                        break;
                }

                if (isAlerted) {
                    break;
                }
            }

            ssids.add(MonitoredSSIDSummaryResponse.create(
                    ssid.uuid(),
                    ssid.isEnabled(),
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

        Optional<MonitoredSSID> result = nzyme.getDot11().findMonitoredSSID(uuid);

        if (result.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        MonitoredSSID ssid = result.get();

        if (!passedMonitoredNetworkAccessible(authenticatedUser, ssid)){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Find all monitored BSSIDs.
        List<MonitoredBSSIDDetailsResponse> bssids = Lists.newArrayList();
        for (MonitoredBSSID bssid : nzyme.getDot11().findMonitoredBSSIDsOfMonitoredNetwork(ssid.id())) {
            List<MonitoredFingerprintResponse> fingerprints = Lists.newArrayList();
            for (MonitoredFingerprint fp : nzyme.getDot11().findMonitoredFingerprintsOfMonitoredBSSID(bssid.id())) {
                fingerprints.add(MonitoredFingerprintResponse.create(fp.uuid(), fp.fingerprint()));
            }

            boolean isOnline = nzyme.getDot11().bssidExist(bssid.bssid(), 15, allAccessibleTapUUIDs);

            Optional<MacAddressContextEntry> bssidContext = nzyme.getContextService().findMacAddressContext(
                    bssid.bssid(),
                    authenticatedUser.getOrganizationId(),
                    authenticatedUser.getTenantId()
            );

            bssids.add(MonitoredBSSIDDetailsResponse.create(
                    ssid.uuid(),
                    bssid.uuid(),
                    Dot11MacAddressResponse.create(
                            bssid.bssid(),
                            nzyme.getOUIManager().lookupMac(bssid.bssid()),
                            bssidContext.map(macAddressContextEntry ->
                                            Dot11MacAddressContextResponse.create(
                                                    macAddressContextEntry.name(),
                                                    macAddressContextEntry.description()
                                            ))
                                    .orElse(null)
                    ),
                    isOnline,
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

        boolean isAlerted = false;
        boolean bssidAlerted = false;
        boolean channelAlerted = false;
        boolean securitySuitesAlerted = false;
        boolean fingerprintAlerted = false;
        boolean signalTracksAlerted = false;
        boolean discoAnomaliesAlerted = false;
        boolean similarSSIDAlerted = false;
        boolean restrictedSSIDSubstringAlerted = false;
        for (DetectionAlertEntry alert : nzyme.getDetectionAlertService()
                .findAllActiveAlertsOfMonitoredNetwork(ssid.uuid())) {
            DetectionType detectionType;
            try {
                detectionType = DetectionType.valueOf(alert.detectionType());
            } catch(IllegalArgumentException e) {
                LOG.error("Invalid detection type [{}]. Skipping.", alert.detectionType());
                continue;
            }

            switch (detectionType) {
                case DOT11_MONITOR_BSSID:
                    if (!ssid.enabledUnexpectedBSSID()) {
                        continue;
                    }
                    bssidAlerted = true;
                    break;
                case DOT11_MONITOR_CHANNEL:
                    if (!ssid.enabledUnexpectedChannel()) {
                        continue;
                    }
                    channelAlerted = true;
                    break;
                case DOT11_MONITOR_SECURITY_SUITE:
                    if (!ssid.enabledUnexpectedSecuritySuites()) {
                        continue;
                    }
                    securitySuitesAlerted = true;
                    break;
                case DOT11_MONITOR_FINGERPRINT:
                    if (!ssid.enabledUnexpectedFingerprint()) {
                        continue;
                    }
                    fingerprintAlerted = true;
                    break;
                case DOT11_MONITOR_SIGNAL_TRACK:
                    if (!ssid.enabledUnexpectedSignalTracks()) {
                        continue;
                    }
                    signalTracksAlerted = true;
                    break;
                case DOT11_MONITOR_DISCO_ANOMALIES:
                    discoAnomaliesAlerted = true;
                    break;
                case DOT11_MONITOR_SIMILAR_LOOKING_SSID:
                    if (!ssid.enabledSimilarLookingSSID()) {
                        continue;
                    }
                    similarSSIDAlerted = true;
                    break;
                case DOT11_MONITOR_SSID_SUBSTRING:
                    if (!ssid.enabledSSIDSubstring()) {
                        continue;
                    }
                    restrictedSSIDSubstringAlerted = true;
                    break;
            }

            isAlerted = true;
        }

        return Response.ok(MonitoredSSIDDetailsResponse.create(
                ssid.uuid(),
                ssid.isEnabled(),
                ssid.ssid(),
                ssid.organizationId(),
                ssid.tenantId(),
                bssids,
                channels,
                securitySuites,
                ssid.detectionConfigSimilarLookingSSIDThreshold(),
                ssid.createdAt(),
                ssid.updatedAt(),
                isAlerted,
                bssidAlerted,
                channelAlerted,
                securitySuitesAlerted,
                fingerprintAlerted,
                signalTracksAlerted,
                discoAnomaliesAlerted,
                similarSSIDAlerted,
                restrictedSSIDSubstringAlerted,
                ssid.enabledUnexpectedBSSID(),
                ssid.enabledUnexpectedChannel(),
                ssid.enabledUnexpectedSecuritySuites(),
                ssid.enabledUnexpectedFingerprint(),
                ssid.enabledUnexpectedSignalTracks(),
                ssid.enabledSimilarLookingSSID(),
                ssid.enabledSSIDSubstring()
        )).build();
    }

    @POST
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids")
    public Response createMonitoredSSID(@Context SecurityContext sc, @Valid CreateDot11MonitoredNetworkRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        if (!authenticatedUser.isSuperAdministrator()) {
            if (authenticatedUser.isOrganizationAdministrator()) {
                // Org admin.
                if (!authenticatedUser.getOrganizationId().equals(req.organizationId())) {
                    return Response.status(Response.Status.UNAUTHORIZED).build();
                }
            } else {
                // Tenant user.
                if (!authenticatedUser.getOrganizationId().equals(req.organizationId())
                        || !authenticatedUser.getTenantId().equals(req.tenantId())) {
                    return Response.status(Response.Status.UNAUTHORIZED).build();
                }
            }
        }

        nzyme.getDot11().createMonitoredSSID(
                req.ssid(),
                req.organizationId(),
                req.tenantId()
        );

        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{uuid}")
    public Response delete(@Context SecurityContext sc, @PathParam("uuid") UUID uuid) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<MonitoredSSID> result = nzyme.getDot11().findMonitoredSSID(uuid);

        if (result.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        MonitoredSSID ssid = result.get();

        if (!passedMonitoredNetworkAccessible(authenticatedUser, ssid)){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().deleteMonitoredSSID(ssid.id());

        return Response.ok().build();
    }

    @POST
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{uuid}/bssids")
    public Response createMonitoredBSSID(@Context SecurityContext sc,
                                         @PathParam("uuid") UUID ssidUUID,
                                         @Valid CreateDot11MonitoredBSSIDRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<MonitoredSSID> ssid = nzyme.getDot11().findMonitoredSSID(ssidUUID);

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!passedMonitoredNetworkAccessible(authenticatedUser, ssid.get())){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!Tools.isValidMacAddress(req.bssid())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        List<String> existingBSSIDs = Lists.newArrayList();
        for (MonitoredBSSID monitored : nzyme.getDot11().findMonitoredBSSIDsOfMonitoredNetwork(ssid.get().id())) {
            existingBSSIDs.add(monitored.bssid());
        }

        if (existingBSSIDs.contains(req.bssid().toUpperCase())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        nzyme.getDot11().createMonitoredBSSID(ssid.get().id(), req.bssid());

        nzyme.getDot11().bumpMonitoredSSIDUpdatedAt(ssid.get().id());

        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{ssid_uuid}/bssids/show/{bssid_uuid}")
    public Response deleteMonitoredBSSID(@Context SecurityContext sc,
                                         @PathParam("ssid_uuid") UUID ssidUUID,
                                         @PathParam("bssid_uuid") UUID bssidUUID) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<MonitoredSSID> ssid = nzyme.getDot11().findMonitoredSSID(ssidUUID);

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!passedMonitoredNetworkAccessible(authenticatedUser, ssid.get())){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<Long> bssidId = nzyme.getDot11().findMonitoredBSSIDId(ssid.get().id(), bssidUUID);

        if (bssidId.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().deleteMonitoredBSSID(bssidId.get());

        nzyme.getDot11().bumpMonitoredSSIDUpdatedAt(ssid.get().id());

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

        Optional<MonitoredSSID> ssid = nzyme.getDot11().findMonitoredSSID(ssidUUID);

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!passedMonitoredNetworkAccessible(authenticatedUser, ssid.get())){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<Long> bssidId = nzyme.getDot11().findMonitoredBSSIDId(ssid.get().id(), bssidUUID);

        if (bssidId.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (req.fingerprint().length() != 64) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        List<String> existingFingerprints = Lists.newArrayList();
        for (MonitoredFingerprint fp : nzyme.getDot11().findMonitoredFingerprintsOfMonitoredBSSID(bssidId.get())) {
            existingFingerprints.add(fp.fingerprint());
        }

        if (existingFingerprints.contains(req.fingerprint())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        nzyme.getDot11().createdMonitoredBSSIDFingerprint(bssidId.get(), req.fingerprint());

        nzyme.getDot11().bumpMonitoredSSIDUpdatedAt(ssid.get().id());

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

        Optional<MonitoredSSID> ssid = nzyme.getDot11().findMonitoredSSID(ssidUUID);

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!passedMonitoredNetworkAccessible(authenticatedUser, ssid.get())){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<Long> bssidId = nzyme.getDot11().findMonitoredBSSIDId(ssid.get().id(), bssidUUID);

        if (bssidId.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().deleteMonitoredBSSIDFingerprint(bssidId.get(), fingerprintUUID);

        nzyme.getDot11().bumpMonitoredSSIDUpdatedAt(ssid.get().id());

        return Response.ok().build();
    }

    @POST
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{uuid}/channels")
    public Response createMonitoredSSIDChannel(@Context SecurityContext sc,
                                               @PathParam("uuid") UUID ssidUUID,
                                               @Valid CreateDot11MonitoredChannelRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<MonitoredSSID> ssid = nzyme.getDot11().findMonitoredSSID(ssidUUID);

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!passedMonitoredNetworkAccessible(authenticatedUser, ssid.get())){
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

        nzyme.getDot11().bumpMonitoredSSIDUpdatedAt(ssid.get().id());

        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{ssid_uuid}/channels/show/{channel_uuid}")
    public Response deleteMonitoredSSIDChannel(@Context SecurityContext sc,
                                               @PathParam("ssid_uuid") UUID ssidUUID,
                                               @PathParam("channel_uuid") UUID channelUUID) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<MonitoredSSID> ssid = nzyme.getDot11().findMonitoredSSID(ssidUUID);

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!passedMonitoredNetworkAccessible(authenticatedUser, ssid.get())){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().deleteMonitoredChannel(ssid.get().id(), channelUUID);

        nzyme.getDot11().bumpMonitoredSSIDUpdatedAt(ssid.get().id());

        return Response.ok().build();
    }

    @POST
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{uuid}/securitysuites")
    public Response createMonitoredSSIDSecuritySuite(@Context SecurityContext sc,
                                                     @PathParam("uuid") UUID ssidUUID,
                                                     @Valid CreateDot11MonitoredSecuritySuiteRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<MonitoredSSID> ssid = nzyme.getDot11().findMonitoredSSID(ssidUUID);

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!passedMonitoredNetworkAccessible(authenticatedUser, ssid.get())){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // TODO we might want to tighten this down in the future.
        if (!req.suite().equals("NONE") && (!req.suite().contains("-") || !req.suite().contains("/"))) {
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

        nzyme.getDot11().bumpMonitoredSSIDUpdatedAt(ssid.get().id());

        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{ssid_uuid}/securitysuites/show/{suite_uuid}")
    public Response deleteMonitoredSSIDSecuritySuite(@Context SecurityContext sc,
                                                     @PathParam("ssid_uuid") UUID ssidUUID,
                                                     @PathParam("suite_uuid") UUID suiteUUID) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<MonitoredSSID> ssid = nzyme.getDot11().findMonitoredSSID(ssidUUID);

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!passedMonitoredNetworkAccessible(authenticatedUser, ssid.get())){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().deleteMonitoredSecuritySuite(ssid.get().id(), suiteUUID);

        nzyme.getDot11().bumpMonitoredSSIDUpdatedAt(ssid.get().id());

        return Response.ok().build();
    }

    @PUT
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{uuid}/enable")
    public Response enableMonitoredNetwork(@Context SecurityContext sc, @PathParam("uuid") UUID ssidUUID) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<MonitoredSSID> ssid = nzyme.getDot11().findMonitoredSSID(ssidUUID);

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!passedMonitoredNetworkAccessible(authenticatedUser, ssid.get())){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().setMonitoredSSIDEnabledState(ssid.get().id(), true);

        nzyme.getDot11().bumpMonitoredSSIDUpdatedAt(ssid.get().id());

        return Response.ok().build();
    }

    @PUT
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{uuid}/disable")
    public Response disableMonitoredNetwork(@Context SecurityContext sc, @PathParam("uuid") UUID ssidUUID) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<MonitoredSSID> ssid = nzyme.getDot11().findMonitoredSSID(ssidUUID);

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!passedMonitoredNetworkAccessible(authenticatedUser, ssid.get())){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().setMonitoredSSIDEnabledState(ssid.get().id(), false);

        nzyme.getDot11().bumpMonitoredSSIDUpdatedAt(ssid.get().id());

        return Response.ok().build();
    }

    @PUT
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{uuid}/alertenabledstatus/{alert}/set/{status}")
    public Response setAlertEnabledStatus(@Context SecurityContext sc,
                                          @PathParam("uuid") UUID ssidUUID,
                                          @PathParam("alert") @NotEmpty String alert,
                                          @PathParam("status") boolean status) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<MonitoredSSID> ssid = nzyme.getDot11().findMonitoredSSID(ssidUUID);

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!passedMonitoredNetworkAccessible(authenticatedUser, ssid.get())){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Dot11.MonitorActiveStatusTypeColumn alertColumn;
        try {
            alertColumn = Dot11.MonitorActiveStatusTypeColumn.valueOf(alert.toUpperCase());
        } catch(IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        nzyme.getDot11().setMonitorAlertStatus(ssid.get().id(), alertColumn, status);

        nzyme.getDot11().bumpMonitoredSSIDUpdatedAt(ssid.get().id());

        return Response.ok().build();
    }

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{uuid}/import/data")
    public Response getImportData(@Context SecurityContext sc, @PathParam("uuid") UUID uuid) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);
        List<UUID> allAccessibleTapUUIDs = parseAndValidateTapIds(authenticatedUser, nzyme, "*");

        Optional<MonitoredSSID> ssid = nzyme.getDot11().findMonitoredSSID(uuid);

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!passedMonitoredNetworkAccessible(authenticatedUser, ssid.get())){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<MonitoredBSSID> monitoredBSSIDs = nzyme.getDot11().findMonitoredBSSIDsOfMonitoredNetwork(ssid.get().id());
        List<BSSIDImportDataResponse> bssidsResponse = Lists.newArrayList();
        for (String bssid : nzyme.getDot11().findBSSIDsAdvertisingSSID(ssid.get().ssid(), allAccessibleTapUUIDs)) {
            MonitoredBSSID monitoredBSSID = null;
            List<String> monitoredFingerprints = Lists.newArrayList();
            for (MonitoredBSSID b : monitoredBSSIDs) {
                if (b.bssid().equals(bssid)) {
                    monitoredBSSID = b;
                    monitoredFingerprints = nzyme.getDot11().findMonitoredFingerprintsOfMonitoredBSSID(b.id())
                            .stream()
                            .map(MonitoredFingerprint::fingerprint)
                            .collect(Collectors.toList());
                    break;
                }
            }

            List<FingerprintImportDataResponse> fingerprints = Lists.newArrayList();
            for (String fingerprint : nzyme.getDot11().findFingerprintsOfBSSID(bssid, allAccessibleTapUUIDs)) {
                fingerprints.add(FingerprintImportDataResponse.create(
                        fingerprint,
                        monitoredFingerprints.contains(fingerprint)
                ));
            }

            Optional<MacAddressContextEntry> ctx = nzyme.getContextService().findMacAddressContext(
                    bssid,
                    authenticatedUser.getOrganizationId(),
                    authenticatedUser.getTenantId()
            );

            bssidsResponse.add(BSSIDImportDataResponse.create(
                    Dot11MacAddressResponse.create(
                            bssid,
                            nzyme.getOUIManager().lookupMac(bssid),
                            ctx.map(c -> Dot11MacAddressContextResponse.create(c.name(), c.description()))
                                    .orElse(null)
                    ),
                    fingerprints,
                    monitoredBSSID != null
            ));
        }

        List<String> monitoredSuites = nzyme.getDot11().findMonitoredSecuritySuitesOfMonitoredNetwork(ssid.get().id())
                .stream().map(MonitoredSecuritySuite::securitySuite).collect(Collectors.toList());
        List<SecuritySuiteImportDataResponse> securitySuitesResponse = Lists.newArrayList();
        for (String ss : nzyme.getDot11().findSecuritySuitesOfSSID(ssid.get().ssid(), allAccessibleTapUUIDs)) {
            try {
                Dot11SecuritySuiteJson ssj = new ObjectMapper().readValue(ss, Dot11SecuritySuiteJson.class);
                String ssId = Dot11.securitySuitesToIdentifier(ssj);
                securitySuitesResponse.add(SecuritySuiteImportDataResponse.create(
                        ssId, monitoredSuites.contains(ssId)
                ));
            } catch(Exception e) {
                LOG.error("Could not build security suites response.", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }

        List<Long> monitoredChannels = nzyme.getDot11().findMonitoredChannelsOfMonitoredNetwork(ssid.get().id())
                .stream().map(MonitoredChannel::frequency).collect(Collectors.toList());
        List<ChannelImportDataResponse> channelsResponse = Lists.newArrayList();
        for (Long f : nzyme.getDot11().findChannelsOfSSID(ssid.get().ssid(), allAccessibleTapUUIDs)) {
            channelsResponse.add(ChannelImportDataResponse.create(f, monitoredChannels.contains(f)));
        }

        return Response.ok(MonitoredNetworkImportDataResponse.create(
                bssidsResponse, channelsResponse, securitySuitesResponse
        )).build();
    }

    @POST
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{uuid}/import/data")
    public Response writeImportData(@Context SecurityContext sc,
                                    @PathParam("uuid") UUID uuid,
                                    @Valid ImportMonitoredNetworkDataRequest req) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<MonitoredSSID> ssid = nzyme.getDot11().findMonitoredSSID(uuid);

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!passedMonitoredNetworkAccessible(authenticatedUser, ssid.get())){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Load currently monitored data.
        List<String> currentBSSIDs = nzyme.getDot11()
                .findMonitoredBSSIDsOfMonitoredNetwork(ssid.get().id())
                .stream()
                .map(MonitoredBSSID::bssid)
                .collect(Collectors.toList());

        List<Long> currentChannels = nzyme.getDot11()
                .findMonitoredChannelsOfMonitoredNetwork(ssid.get().id())
                .stream()
                .map(MonitoredChannel::frequency)
                .collect(Collectors.toList());

        List<String> currentSecSuites = nzyme.getDot11()
                .findMonitoredSecuritySuitesOfMonitoredNetwork(ssid.get().id())
                .stream()
                .map(MonitoredSecuritySuite::securitySuite)
                .collect(Collectors.toList());

        // BSSIDS.
        for (ImportMonitoredNetworkDataBSSIDRequest bssid : req.bssids()) {
            if (currentBSSIDs.contains(bssid.bssid())) {
                // Existing BSSID. Check if we need to add fingeprints.
                @SuppressWarnings("OptionalGetWithoutIsPresent")
                long bssidId = nzyme.getDot11().findMonitoredBSSIDId(ssid.get().id(), bssid.bssid()).get();

                List<String> existingFingerprints = nzyme.getDot11().findMonitoredFingerprintsOfMonitoredBSSID(bssidId)
                        .stream()
                        .map(MonitoredFingerprint::fingerprint)
                        .collect(Collectors.toList());

                for (String fingerprint : bssid.fingerprints()) {
                    if (!existingFingerprints.contains(fingerprint)) {
                        nzyme.getDot11().createdMonitoredBSSIDFingerprint(bssidId, fingerprint);
                    }
                }
            } else {
                // New BSSID.
                long bssidId = nzyme.getDot11().createMonitoredBSSID(ssid.get().id(), bssid.bssid());

                for (String fingerprint : bssid.fingerprints()) {
                    nzyme.getDot11().createdMonitoredBSSIDFingerprint(bssidId, fingerprint);
                }
            }
        }

        // Channels.
        for (Long channel : req.channels()) {
            if (!currentChannels.contains(channel)) {
                nzyme.getDot11().createMonitoredChannel(ssid.get().id(), channel);
            }
        }

        // Security Suites.
        for (String securitySuite : req.securitySuites()) {
            if (!currentSecSuites.contains(securitySuite)) {
                nzyme.getDot11().createMonitoredSecuritySuite(ssid.get().id(), securitySuite);
            }
        }

        return Response.ok(Response.Status.CREATED).build();
    }

    @GET
    @RESTSecured(value = PermissionLevel.ANY, featurePermissions = { "dot11_monitoring_manage" })
    @Path("/ssids/show/{uuid}/configuration/similarssids/simulate")
    public Response simulateSimilarSSIDs(@Context SecurityContext sc,
                                         @PathParam("uuid") UUID uuid,
                                         @QueryParam("threshold") int threshold) {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(sc);

        Optional<MonitoredSSID> ssid = nzyme.getDot11().findMonitoredSSID(uuid);

        if (ssid.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!passedMonitoredNetworkAccessible(authenticatedUser, ssid.get())){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<SSIDSimilarityResponse> similarities = Lists.newArrayList();
        List<String> monitoredSSIDNames = nzyme.getDot11()
                .findAllMonitoredSSIDs(authenticatedUser.getOrganizationId(), authenticatedUser.getTenantId())
                .stream().map(MonitoredSSID::ssid)
                .collect(Collectors.toList());

        JaroWinkler jaroWinkler = new JaroWinkler();
        for (String ssidName : nzyme.getDot11()
                .findAllSSIDNames(nzyme.getTapManager().allTapUUIDsAccessibleByUser(authenticatedUser))) {
            double similarity = jaroWinkler.similarity(ssid.get().ssid(), ssidName) * 100.0;
            boolean monitored = monitoredSSIDNames.contains(ssidName);
            boolean alerted = !monitored && similarity > threshold;

            similarities.add(SSIDSimilarityResponse.create(ssidName, similarity, monitored, alerted));
        }

        return Response.ok(similarities).build();
    }

}
