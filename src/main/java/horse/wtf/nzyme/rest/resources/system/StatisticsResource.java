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

package horse.wtf.nzyme.rest.resources.system;

import com.google.common.collect.Maps;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.rest.responses.statistics.ChannelStatisticsResponse;
import horse.wtf.nzyme.rest.responses.statistics.StatisticsResponse;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Path("/api/system/statistics")
@Produces(MediaType.APPLICATION_JSON)
public class StatisticsResource {

    @Inject
    private Nzyme nzyme;

    @GET
    @Path("global")
    public Response global() {
        Map<String, Long> frameTypes = Maps.newHashMap();
        for (Map.Entry<String, AtomicLong> x : nzyme.getStatistics().getFrameTypes().entrySet()) {
            frameTypes.put(x.getKey(), x.getValue().get());
        }

        Map<Integer, ChannelStatisticsResponse> channelStatistics = Maps.newHashMap();
        for (Map.Entry<Integer, AtomicLong> x : nzyme.getStatistics().getChannelCounts().entrySet()) {
            Long malformed = nzyme.getStatistics().getChannelMalformedCounts().containsKey(x.getKey())
                    ? nzyme.getStatistics().getChannelMalformedCounts().get(x.getKey()).get() : 0L;
            channelStatistics.put(x.getKey(), ChannelStatisticsResponse.create(x.getValue().get(), malformed));
        }

        return Response.ok(
                StatisticsResponse.create(
                        nzyme.getStatistics().getFrameCount().get(),
                        nzyme.getStatistics().getMalformedCount().get(),
                        frameTypes,
                        channelStatistics,
                        nzyme.getStatistics().
                        getProbingDevices().keySet(),
                        nzyme.getStatistics().getBeaconedNetworks().keySet(),
                        nzyme.getStatistics().getAccessPoints().keySet()
                )
        ).build();
    }

}
