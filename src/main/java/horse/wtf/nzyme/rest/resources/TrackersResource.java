/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.rest.resources;

import com.google.common.collect.Lists;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.bandits.BanditHashCalculator;
import horse.wtf.nzyme.bandits.trackers.Tracker;
import horse.wtf.nzyme.bandits.trackers.TrackerManager;
import horse.wtf.nzyme.bandits.trackers.TrackerState;
import horse.wtf.nzyme.rest.authentication.Secured;
import horse.wtf.nzyme.rest.requests.BanditTrackRequest;
import horse.wtf.nzyme.rest.responses.trackers.TrackerResponse;
import horse.wtf.nzyme.rest.responses.trackers.TrackersListResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/api/trackers")
@Secured
@Produces(MediaType.APPLICATION_JSON)
public class TrackersResource {

    private static final Logger LOG = LogManager.getLogger(TrackersResource.class);

    @Inject
    private NzymeLeader nzyme;

    @GET
    public Response findAll() {
        List<TrackerResponse> trackers = Lists.newArrayList();
        for (Tracker tracker : nzyme.getTrackerManager().getTrackers().values()) {
            trackers.add(TrackerResponse.create(
                    tracker.getName(),
                    tracker.getVersion(),
                    tracker.getDrift(),
                    tracker.getLastSeen(),
                    tracker.getBanditHash(),
                    tracker.getBanditCount(),
                    decideTrackerState(tracker),
                    tracker.getTrackingMode(),
                    nzyme.getGroundStation().trackerHasPendingTrackingRequest(tracker.getName()),
                    tracker.getRssi()
            ));
        }

        return Response.ok(TrackersListResponse.create(
                nzyme.getGroundStation() != null,
                trackers.size(),
                trackers
        )).build();
    }

    @GET
    @Path("/show/{name}")
    public Response findTracker(@PathParam("name") String name) {
        Map<String, Tracker> trackers = nzyme.getTrackerManager().getTrackers();
        if (trackers.containsKey(name)) {
            Tracker tracker = trackers.get(name);

            return Response.ok(TrackerResponse.create(
                tracker.getName(),
                tracker.getVersion(),
                tracker.getDrift(),
                tracker.getLastSeen(),
                tracker.getBanditHash(),
                tracker.getBanditCount(),
                decideTrackerState(tracker),
                tracker.getTrackingMode(),
                nzyme.getGroundStation().trackerHasPendingTrackingRequest(tracker.getName()),
                tracker.getRssi()
            )).build();
        } else {
            LOG.info("Tracker [{}] not found.", name);
            return Response.status(404).build();
        }
    }

    @POST
    @Path("/show/{name}/command/start_track_request")
    public Response issueStartTrackRequest(@PathParam("name") String name, BanditTrackRequest trackRequest) {
        // Check if tracker exists.
        if (!nzyme.getTrackerManager().getTrackers().containsKey(name)) {
            return Response.status(404).build();
        }

        nzyme.getGroundStation().startTrackRequest(name, trackRequest.banditUUID());

        return Response.accepted().build();
    }

    @POST
    @Path("/show/{name}/command/cancel_track_request")
    public Response issueCancelTrackRequest(@PathParam("name") String name, BanditTrackRequest trackRequest) {
        // Check if tracker exists.
        if (!nzyme.getTrackerManager().getTrackers().containsKey(name)) {
            return Response.status(404).build();
        }

        nzyme.getGroundStation().cancelTrackRequest(name, trackRequest.banditUUID());

        return Response.accepted().build();
    }

    private TrackerState decideTrackerState(Tracker tracker) {
        if (tracker.getLastSeen().isBefore(DateTime.now().minusSeconds(TrackerManager.DARK_TIMEOUT_SECONDS))) {
            return TrackerState.DARK;
        } else {
            if (tracker.getBanditHash().equals(BanditHashCalculator.calculate(nzyme.getContactIdentifier().getBanditList()))) {
                return TrackerState.ONLINE;
            } else {
                return TrackerState.OUT_OF_SYNC;
            }
        }
    }

}
