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

import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.networks.BSSID;
import horse.wtf.nzyme.dot11.networks.Channel;
import horse.wtf.nzyme.dot11.networks.SSID;
import horse.wtf.nzyme.dot11.networks.beaconrate.AverageBeaconRate;
import horse.wtf.nzyme.dot11.networks.sigindex.SignalInformation;
import horse.wtf.nzyme.rest.responses.networks.BSSIDsResponse;
import horse.wtf.nzyme.rest.responses.networks.SSIDResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/api/networks")
@Produces(MediaType.APPLICATION_JSON)
public class NetworksResource {

    private static final Logger LOG = LogManager.getLogger(NetworksResource.class);

    private static final String SIGNAL_AVERAGE_QUERY = "SELECT created_at, channel, AVG(signal_index) AS avg_signal_index, " +
            "AVG(signal_index_threshold) AS avg_signal_index_threshold, AVG(signal_quality) AS avg_signal_quality, " +
            "AVG(signal_stddev) AS avg_signal_stddev, AVG(expected_delta_lower) AS avg_expected_delta_lower, " +
            "AVG(expected_delta_upper) AS avg_expected_delta_upper FROM signal_index_history " +
            "WHERE bssid = ? AND ssid = ? AND channel = ? AND created_at > DATETIME('now', '-1 day') " +
            "AND signal_index NOT NULL AND signal_quality NOT NULL " +
            "GROUP BY strftime('%Y%m%d%H%M', created_at) " +
            "ORDER BY created_at";

    public static final String BEACON_RATE_AVERAGE_QUERY = "SELECT created_at, channel, AVG(beacon_rate) AS avg_beacon_rate " +
            "FROM beacon_rate_history " +
            "WHERE bssid = ? AND ssid = ? AND channel = ? " +
            "GROUP BY strftime('%Y%m%d%H%M', created_at) " +
            "ORDER BY created_at;";

    @Inject
    private Nzyme nzyme;

    @GET
    @Path("/bssids")
    public Response bssids() {
        return Response.ok(BSSIDsResponse.create(
                nzyme.getNetworks().getBSSIDs().size(),
                nzyme.getNetworks().getBSSIDs()
        )).build();
    }

    @GET
    @Path("/bssids/{bssid}/ssids/{ssid}")
    public Response ssid(@PathParam("bssid") @NotNull String bssid, @PathParam("ssid") @NotNull String ssid) {
        bssid = bssid.toLowerCase();

        if (nzyme.getNetworks().getBSSIDs().containsKey(bssid)) {
            BSSID b = nzyme.getNetworks().getBSSIDs().get(bssid);

            if (b.ssids().containsKey(ssid)) {
                SSID s = b.ssids().get(ssid);

                // Enrich channels with signal index and quality information.
                for (Channel channel : s.channels().values()) {
                    List<SignalInformation> sigInfoHistory = nzyme.getDatabase().withHandle(handle ->
                            handle.createQuery(SIGNAL_AVERAGE_QUERY)
                                    .bind(0, b.bssid())
                                    .bind(1, s.name())
                                    .bind(2, channel.channelNumber())
                                    .mapTo(SignalInformation.class)
                                    .list()
                    );

                    channel.setSignalHistory(sigInfoHistory);
                }

                // Enrich with beacon rate history.
                for (Channel channel : s.channels().values()) {
                    List<AverageBeaconRate> beaconRateHistory = nzyme.getDatabase().withHandle(handle ->
                        handle.createQuery(BEACON_RATE_AVERAGE_QUERY)
                                .bind(0, b.bssid())
                                .bind(1, s.name())
                                .bind(2, channel.channelNumber())
                                .mapTo(AverageBeaconRate.class)
                                .list()
                    );

                    channel.setBeaconRateHistory(beaconRateHistory);
                }

                return Response.ok(SSIDResponse.create(s)).build();
            } else {
                LOG.debug("Could not find requested SSID [{}] on BSSID [{}].", ssid, bssid);
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } else {
            LOG.debug("Could not find requested BSSID [{}].", bssid);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Path(("/fingerprints/reset"))
    public Response resetFingerprints() {
        for (BSSID bssid : nzyme.getNetworks().getBSSIDs().values()) {
            for (SSID ssid : bssid.ssids().values()) {
                for (Channel channel : ssid.channels().values()) {
                    channel.fingerprints().clear();
                }
            }
        }

        return Response.ok().build();
    }

}
