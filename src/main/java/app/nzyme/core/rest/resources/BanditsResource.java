/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package app.nzyme.core.rest.resources;

import app.nzyme.plugin.rest.security.RESTSecured;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import app.nzyme.core.NzymeNode;
import app.nzyme.core.bandits.Bandit;
import app.nzyme.core.bandits.Contact;
import app.nzyme.core.bandits.engine.ContactRecordAggregation;
import app.nzyme.core.bandits.engine.ContactRecorder;
import app.nzyme.core.bandits.engine.ContactRecorderHistogramEntry;
import app.nzyme.core.bandits.identifiers.BanditIdentifier;
import app.nzyme.core.bandits.identifiers.BanditIdentifierFactory;
import app.nzyme.core.bandits.trackers.Tracker;
import app.nzyme.core.bandits.trackers.TrackerManager;
import app.nzyme.core.rest.requests.CreateBanditIdentifierRequest;
import app.nzyme.core.rest.requests.CreateBanditRequest;
import app.nzyme.core.rest.requests.UpdateBanditRequest;
import app.nzyme.core.rest.responses.bandits.*;
import app.nzyme.core.rest.responses.bandits.identifiers.IdentifierTypesResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/api/bandits")
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class BanditsResource {

    private static final Logger LOG = LogManager.getLogger(BanditsResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    public Response findAll() {
        List<BanditResponse> bandits = Lists.newArrayList();

        for (Bandit x : nzyme.getContactManager().getBandits().values()) {
            if (x.databaseId() == null) {
                LOG.error("Uninitialized bandit in BanditIdentifier. Skipping.");
                continue;
            }

            List<ContactResponse> contacts = buildContactsResponse(x, false);

            bandits.add(BanditResponse.create(
                    x.uuid(),
                    x.databaseId(),
                    x.name(),
                    x.description(),
                    x.createdAt(),
                    x.updatedAt(),
                    x.readOnly(),
                    findLastContact(contacts),
                    anyActiveContact(contacts),
                    trackedBy(x),
                    buildIdentifiersResponse(x),
                    contacts
            ));
        }

        return Response.ok(BanditsListResponse.create(
                bandits,
                bandits.size()
        )).build();
    }

    @GET
    @Path("/show/{uuid}")
    public Response findOne(@PathParam("uuid") String id) {
        if (Strings.isNullOrEmpty(id)) {
            LOG.warn("Bandit ID was null or empty.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch(IllegalArgumentException e) {
            LOG.warn("Invalid Bandit UUID", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Bandit bandit = nzyme.getContactManager().getBandits().get(uuid);
        if (bandit == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<ContactResponse> contacts = buildContactsResponse(bandit, true);

        return Response.ok(BanditResponse.create(
                bandit.uuid(),
                bandit.databaseId(),
                bandit.name(),
                bandit.description(),
                bandit.createdAt(),
                bandit.updatedAt(),
                bandit.readOnly(),
                findLastContact(contacts),
                anyActiveContact(contacts),
                trackedBy(bandit),
                buildIdentifiersResponse(bandit),
                contacts
        )).build();
    }

    @POST
    public Response create(CreateBanditRequest request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (Strings.isNullOrEmpty(request.name()) || Strings.isNullOrEmpty(request.description())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        nzyme.getContactManager().registerBandit(Bandit.create(
                null,
                UUID.randomUUID(),
                request.name(),
                request.description(),
                false,
                null,
                null,
                null
        ));

        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/show/{uuid}")
    public Response delete(@PathParam("uuid") String id) {
        if (Strings.isNullOrEmpty(id)) {
            LOG.warn("Bandit ID was null or empty.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // Is this bandit currently tracked by anyone?
        for (Tracker tracker : nzyme.getTrackerManager().getTrackers().values()) {
            if (tracker.getTrackingMode() != null && tracker.getTrackingMode().equals(id)) {
                LOG.error("Cannot delete bandit that is currently actively tracked by trackers.");
                return Response.status(401).build();
            }
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch(IllegalArgumentException e) {
            LOG.warn("Invalid Bandit UUID", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Optional<Bandit> optionalBandit = nzyme.getContactManager().findBanditByUUID(uuid);
        if (!optionalBandit.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (optionalBandit.get().readOnly()) {
            LOG.warn("Bandit [{}] is read only.", uuid);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        nzyme.getContactManager().removeBandit(uuid);

        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/show/{uuid}")
    public Response update(@PathParam("uuid") String id, UpdateBanditRequest request) {
        if (Strings.isNullOrEmpty(id)) {
            LOG.warn("Bandit ID was null or empty.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch(IllegalArgumentException e) {
            LOG.warn("Invalid Bandit UUID", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (Strings.isNullOrEmpty(request.name()) || Strings.isNullOrEmpty(request.description())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Optional<Bandit> optionalBandit = nzyme.getContactManager().findBanditByUUID(uuid);
        if (!optionalBandit.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (optionalBandit.get().readOnly()) {
            LOG.warn("Bandit [{}] is read only.", uuid);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        nzyme.getContactManager().updateBandit(uuid, request.description(), request.name());

        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/identifiers/types")
    public Response findAllIdentifierTypes() {
        List<String> types = Lists.newArrayList();
        for (BanditIdentifier.TYPE type : BanditIdentifier.TYPE.values()) {
            types.add(type.toString());
        }

        return Response.ok(IdentifierTypesResponse.create(types.size(), types)).build();
    }

    @POST
    @Path("/show/{banditUUID}/identifiers")
    public Response createIdentifier(@PathParam("banditUUID") String banditUUID, CreateBanditIdentifierRequest request) {
        if (Strings.isNullOrEmpty(banditUUID)) {
            LOG.warn("Bandit ID was null or empty.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // Is this bandit currently tracked?
        for (Tracker tracker : nzyme.getTrackerManager().getTrackers().values()) {
            if (tracker.getTrackingMode() != null && tracker.getTrackingMode().equals(banditUUID)) {
                LOG.error("Cannot modify bandit that is currently actively tracked by trackers.");
                return Response.status(401).build();
            }
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(banditUUID);
        } catch(IllegalArgumentException e) {
            LOG.warn("Invalid Bandit UUID", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Optional<Bandit> bandit = nzyme.getContactManager().findBanditByUUID(uuid);
        if (!bandit.isPresent()) {
            LOG.warn("Bandit with UUID <{}> not found.", banditUUID);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (bandit.get().readOnly()) {
            LOG.warn("Bandit [{}] is read only.", uuid);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        BanditIdentifier.TYPE type;
        try {
            type = BanditIdentifier.TYPE.valueOf(request.type());
        }catch (IllegalArgumentException e) {
            LOG.warn("Invalid identifier type.", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        BanditIdentifier identifier;
        try {
            identifier = BanditIdentifierFactory.create(type, request.configuration(), null, null);
        } catch (BanditIdentifierFactory.NoSerializerException | BanditIdentifierFactory.MappingException e) {
            LOG.error("Could not create bandit identifier object.", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        nzyme.getContactManager().registerIdentifier(bandit.get(), identifier);
        return Response.ok().build();
    }

    @DELETE
    @Path("/show/{banditUUID}/identifiers/{identifierUUID}")
    public Response deleteIdentifier(@PathParam("banditUUID") String bUUID, @PathParam("identifierUUID") String iUUID) {
        if (Strings.isNullOrEmpty(bUUID) || Strings.isNullOrEmpty(iUUID)) {
            LOG.warn("UUID was null or empty.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // Is this bandit currently tracked?
        for (Tracker tracker : nzyme.getTrackerManager().getTrackers().values()) {
            if (tracker.getTrackingMode() != null && tracker.getTrackingMode().equals(bUUID)) {
                LOG.error("Cannot modify bandit that is currently actively tracked by trackers.");
                return Response.status(401).build();
            }
        }

        UUID banditUUID;
        UUID identifierUUID;
        try {
            banditUUID = UUID.fromString(bUUID);
            identifierUUID = UUID.fromString(iUUID);
        } catch(IllegalArgumentException e) {
            LOG.warn("Invalid UUID", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Optional<Bandit> optionalBandit = nzyme.getContactManager().findBanditByUUID(banditUUID);
        if (!optionalBandit.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (optionalBandit.get().readOnly()) {
            LOG.warn("Bandit [{}] is read only.", banditUUID);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        nzyme.getContactManager().removeIdentifier(identifierUUID);

        return Response.ok().build();
    }

    @GET
    @Path("/show/{banditUUID}/contacts/{contactUUID}")
    public Response findContactOfBandit(@PathParam("banditUUID") String bUUID,
                                        @PathParam("contactUUID") String cUUID,
                                        @QueryParam("detailed_ssids") String detailedSSIDsQ,
                                        @QueryParam("detailed_bssids") String detailedBSSIDsQ) {
        if (Strings.isNullOrEmpty(bUUID) || Strings.isNullOrEmpty(cUUID)) {
            LOG.warn("UUID was null or empty.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        List<String> detailedSSIDs;
        if (detailedSSIDsQ != null) {
            detailedSSIDs = Lists.newArrayList(Splitter.on(",").split(detailedSSIDsQ));
        } else {
            detailedSSIDs = Collections.emptyList();
        }

        List<String> detailedBSSIDs;
        if (detailedBSSIDsQ != null) {
            detailedBSSIDs = Lists.newArrayList(Splitter.on(",").split(detailedBSSIDsQ));
        } else {
            detailedBSSIDs = Collections.emptyList();
        }

        UUID banditUUID;
        UUID contactUUID;
        try {
            banditUUID = UUID.fromString(bUUID);
            contactUUID = UUID.fromString(cUUID);
        } catch(IllegalArgumentException e) {
            LOG.error("Invalid UUID.", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Optional<Bandit> oBandit = nzyme.getContactManager().findBanditByUUID(banditUUID);
        if (oBandit.isEmpty()) {
            LOG.error("Bandit not found.");
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Bandit bandit = oBandit.get();

        Optional<Contact> oContact = nzyme.getContactManager().findContactOfBandit(bandit, contactUUID);
        if (oContact.isEmpty()) {
            LOG.error("Contact not found.");
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Contact contact = oContact.get();

        Optional<List<ContactRecordAggregation>> ssids = nzyme.getContactManager().findRecordValuesOfContact(contact.uuid(), ContactRecorder.RECORD_TYPE.SSID);
        Optional<List<ContactRecordAggregation>> bssids = nzyme.getContactManager().findRecordValuesOfContact(contact.uuid(), ContactRecorder.RECORD_TYPE.BSSID);

        Optional<Map<String, List<ContactRecorderHistogramEntry>>> ssidRawHistograms = nzyme.getContactManager().findRecordingHistogramsOfContact(contact.uuid(), detailedSSIDs, ContactRecorder.RECORD_TYPE.SSID);
        Optional<Map<String, List<ContactRecorderHistogramEntry>>> bssidRawHistograms = nzyme.getContactManager().findRecordingHistogramsOfContact(contact.uuid(), detailedBSSIDs, ContactRecorder.RECORD_TYPE.BSSID);

        return Response.ok(ContactDetailsResponse.create(
                contact.uuid(),
                contact.frameCount(),
                contact.firstSeen(),
                contact.lastSeen(),
                contact.isActive(),
                contact.lastSignal(),
                bandit.uuid().toString(),
                bandit.name(),
                contact.sourceRole().toString(),
                contact.sourceName(),
                ssids.orElse(Collections.emptyList()),
                bssids.orElse(Collections.emptyList()),
                buildContactRecordHistogram(ssidRawHistograms, ContactRecorder.VALUE_TYPE.FRAME_COUNT).orElse(Maps.newHashMap()),
                buildContactRecordHistogram(bssidRawHistograms, ContactRecorder.VALUE_TYPE.FRAME_COUNT).orElse(Maps.newHashMap()),
                buildContactRecordHistogram(ssidRawHistograms, ContactRecorder.VALUE_TYPE.SIGNAL_STRENGTH).orElse(Maps.newHashMap()),
                buildContactRecordHistogram(bssidRawHistograms, ContactRecorder.VALUE_TYPE.SIGNAL_STRENGTH).orElse(Maps.newHashMap())
        )).build();
    }

    private Optional<Map<String, Map<String, Long>>> buildContactRecordHistogram(Optional<Map<String, List<ContactRecorderHistogramEntry>>> x, ContactRecorder.VALUE_TYPE valueType) {
        if (x.isEmpty()) {
            return Optional.empty();
        }
        
        Map<String, Map<String, Long>> result = Maps.newHashMap();

        for (Map.Entry<String, List<ContactRecorderHistogramEntry>> ssid : x.get().entrySet()) {
            Map<String, Long> ssidResult = Maps.newHashMap();

            for (ContactRecorderHistogramEntry value : ssid.getValue()) {
                long selectedValue;

                switch(valueType) {
                    case FRAME_COUNT:
                        selectedValue = value.frameCount();
                        break;
                    case SIGNAL_STRENGTH:
                        selectedValue = value.signalStrength();
                        break;
                    default:
                        throw new RuntimeException("Value type not implemented for histogram translation.");
                }

                ssidResult.put(value.createdAt().toString(), selectedValue);
            }

            result.put(ssid.getKey(), ssidResult);
        }


        return Optional.of(result);
    }

    private List<BanditIdentifierResponse> buildIdentifiersResponse(Bandit bandit) {
        ImmutableList.Builder<BanditIdentifierResponse> response = new ImmutableList.Builder<>();

        if (bandit.identifiers() != null) {
            for (BanditIdentifier identifier : bandit.identifiers()) {
                response.add(BanditIdentifierResponse.create(
                        identifier.configuration(),
                        identifier.descriptor().type(),
                        identifier.descriptor().description(),
                        identifier.descriptor().matches(),
                        identifier.getDatabaseID(),
                        identifier.getUuid()
                ));
            }
        }

        return response.build();
    }

    private List<ContactResponse> buildContactsResponse(Bandit bandit, boolean includeContactRecords) {
        ImmutableList.Builder<ContactResponse> response = new ImmutableList.Builder<>();

        for (Contact contact : nzyme.getContactManager().findContactsOfBandit(bandit)) {

            Optional<List<ContactRecordAggregation>> ssids;
            Optional<List<ContactRecordAggregation>> bssids;
            if (includeContactRecords) {
                ssids = nzyme.getContactManager().findRecordValuesOfContact(contact.uuid(), ContactRecorder.RECORD_TYPE.SSID);
                bssids = nzyme.getContactManager().findRecordValuesOfContact(contact.uuid(), ContactRecorder.RECORD_TYPE.BSSID);
            } else {
                ssids = Optional.empty();
                bssids = Optional.empty();
            }

            response.add(ContactResponse.create(
                    contact.uuid(),
                    contact.frameCount(),
                    contact.firstSeen(),
                    contact.lastSeen(),
                    contact.isActive(),
                    contact.lastSignal(),
                    bandit.uuid().toString(),
                    bandit.name(),
                    contact.sourceRole().toString(),
                    contact.sourceName(),
                    ssids.orElse(Collections.emptyList()),
                    bssids.orElse(Collections.emptyList())
            ));
        }

        return response.build();
    }

    private DateTime findLastContact(List<ContactResponse> contacts) {
        if (contacts.isEmpty()) {
            return null;
        }

        DateTime last = contacts.get(0).lastSeen();

        for (ContactResponse contact : contacts) {
            if (contact.lastSeen().isAfter(last)) {
                last = contact.lastSeen();
            }
        }

        return last;
    }

    private boolean anyActiveContact(List<ContactResponse> contacts) {
        for (ContactResponse contact : contacts) {
            if (contact.isActive()) {
                return true;
            }
        }

        return false;
    }

    private List<UUID> trackedBy(Bandit bandit) {
        List<UUID> trackedBy = Lists.newArrayList();

        for (Tracker tracker : nzyme.getTrackerManager().getTrackers().values()) {
            if (tracker.getLastSeen().isBefore(DateTime.now().minusSeconds(TrackerManager.DARK_TIMEOUT_SECONDS))) {
                // Tracker is dark.
                continue;
            } else {
                // Is there an outstanding request to cancel tracking?
                if (nzyme.getGroundStation().trackerHasPendingCancelTrackingRequest(tracker.getName())) {
                    continue;
                }

                if (tracker.getTrackingMode().equals(bandit.uuid().toString())) {
                    trackedBy.add(UUID.fromString(tracker.getTrackingMode()));
                }
            }
        }

        return trackedBy;
    }


}
