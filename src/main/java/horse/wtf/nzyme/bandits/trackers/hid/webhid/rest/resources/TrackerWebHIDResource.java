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

package horse.wtf.nzyme.bandits.trackers.hid.webhid.rest.resources;

import horse.wtf.nzyme.NzymeTracker;
import horse.wtf.nzyme.bandits.trackers.hid.webhid.WebHID;
import horse.wtf.nzyme.bandits.trackers.hid.webhid.rest.responses.StateResponse;
import horse.wtf.nzyme.rest.authentication.Secured;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/state")
@Secured
@Produces(MediaType.APPLICATION_JSON)
public class TrackerWebHIDResource {

    @Inject
    private NzymeTracker nzyme;

    @Inject
    private WebHID hid;

    @GET
    public Response getStatus() {
        String track;
        long frameCount;
        DateTime lastContact;
        Long banditSignal;
        if (nzyme.getBanditManager().hasActiveTrack() && nzyme.getBanditManager().getTrackSummary() != null) {
            track = nzyme.getBanditManager().getTrackSummary().track().toString().substring(0,6);
            frameCount = nzyme.getBanditManager().getTrackSummary().frameCount();
            lastContact = nzyme.getBanditManager().getTrackSummary().lastContact();
            banditSignal = (long) nzyme.getBanditManager().getTrackSummary().lastSignal();
        } else {
            track = null;
            frameCount = 0;
            lastContact = null;
            banditSignal = null;
        }

        return Response.ok(StateResponse.create(
                DateTime.now(),
                nzyme.getStateWatchdog().getStates(),
                hid.getLeaderRSSI(),
                hid.isTrackerDeviceLive(),
                hid.isAllProbesLive(),
                hid.allMonitorChannels(),
                hid.getChannelDesignationStatus(),
                nzyme.getBanditManager().isCurrentlyTracking(),
                nzyme.getBanditManager().isCurrentlyTracking() ? nzyme.getBanditManager().getCurrentlyTrackedBandit().uuid().toString().substring(0,6) : null,
                track,
                frameCount,
                lastContact,
                banditSignal,
                hid.getEvents()
        )).build();
    }

}
