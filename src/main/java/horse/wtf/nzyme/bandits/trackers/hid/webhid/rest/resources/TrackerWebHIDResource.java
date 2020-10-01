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
