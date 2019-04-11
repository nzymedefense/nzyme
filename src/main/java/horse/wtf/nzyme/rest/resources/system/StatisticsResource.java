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
import com.google.common.collect.Sets;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.configuration.Dot11MonitorDefinition;
import horse.wtf.nzyme.rest.responses.statistics.ChannelStatisticsResponse;
import horse.wtf.nzyme.rest.responses.statistics.StatisticsResponse;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Path("/api/system/networks")
@Produces(MediaType.APPLICATION_JSON)
public class StatisticsResource {

    @Inject
    private Nzyme nzyme;

    @GET
    @Path("global")
    public Response global() {
        // Frame types.
        Map<String, Long> frameTypes = Maps.newHashMap();
        for (Map.Entry<String, AtomicLong> x : nzyme.getStatistics().getFrameTypes().entrySet()) {
            frameTypes.put(x.getKey(), x.getValue().get());
        }

        // Channels.
        Set<Integer> configuredChannels = Sets.newHashSet();
        for (Dot11MonitorDefinition monitor : nzyme.getConfiguration().getDot11Monitors()) {
            configuredChannels.addAll(monitor.channels());
        }

        Map<Integer, ChannelStatisticsResponse> channelStatistics = Maps.newHashMap();
        for (Integer channel : configuredChannels) {
            Long malformed = nzyme.getStatistics().getChannelMalformedCounts().containsKey(channel) ? nzyme.getStatistics().getChannelMalformedCounts().get(channel).get() : 0L;
            Long count = nzyme.getStatistics().getChannelCounts().containsKey(channel) ? nzyme.getStatistics().getChannelCounts().get(channel).get() : 0L;

            channelStatistics.put(channel, ChannelStatisticsResponse.create(count, malformed));
        }

        return Response.ok(
                StatisticsResponse.create(
                        nzyme.getStatistics().getFrameCount().get(),
                        nzyme.getStatistics().getMalformedCount().get(),
                        frameTypes,
                        channelStatistics,
                        nzyme.getClients().getClients().keySet(),
                        nzyme.getNetworks().getSSIDs(),
                        nzyme.getNetworks().getBSSIDs().keySet()
                )
        ).build();
    }

}
