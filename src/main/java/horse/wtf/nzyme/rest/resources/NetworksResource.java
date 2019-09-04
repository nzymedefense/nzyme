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
import com.google.common.collect.Maps;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.Dot11SecurityConfiguration;
import horse.wtf.nzyme.dot11.networks.BSSID;
import horse.wtf.nzyme.dot11.networks.Channel;
import horse.wtf.nzyme.dot11.networks.SSID;
import horse.wtf.nzyme.dot11.networks.beaconrate.AverageBeaconRate;
import horse.wtf.nzyme.rest.authentication.Secured;
import horse.wtf.nzyme.rest.responses.networks.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/api/networks")
@Produces(MediaType.APPLICATION_JSON)
@Secured
public class NetworksResource {

    private static final Logger LOG = LogManager.getLogger(NetworksResource.class);

    public static final String BEACON_RATE_AVERAGE_QUERY = "SELECT date_trunc('minute', created_at) AS bucket, AVG(beacon_rate) AS avg_beacon_rate " +
            "FROM beacon_rate_history " +
            "WHERE bssid = ? AND ssid = ? AND created_at > (current_timestamp at time zone 'UTC' - interval '1 day') " +
            "GROUP BY bucket " +
            "ORDER BY bucket ASC";

    @Inject
    private Nzyme nzyme;

    @GET
    @Path("/bssids")
    public Response bssids() {
        List<BSSIDResponse> bssids = Lists.newArrayList();

        for (BSSID bssid : nzyme.getNetworks().getBSSIDs().values()) {
            List<String> ssids = Lists.newArrayList();
            List<String> securityMechanisms = Lists.newArrayList();
            List<String> fingerprints = Lists.newArrayList();

            for (SSID ssid : bssid.ssids().values()) {
                ssids.add(ssid.nameSafe());

                for (Dot11SecurityConfiguration sec : ssid.getSecurity()) {
                    if (!securityMechanisms.contains(sec.wpaMode().toString())) {
                        securityMechanisms.add(sec.wpaMode().toString());
                    }
                }

                for (Channel channel : ssid.channels().values()) {
                    for (String fingerprint : channel.fingerprints()) {
                        if (!fingerprints.contains(fingerprint)) {
                            fingerprints.add(fingerprint);
                        }
                    }
                }
            }

            bssids.add(BSSIDResponse.create(
                    bssid.bssid(),
                    bssid.bestRecentSignalQuality(),
                    ssids,
                    bssid.oui(),
                    securityMechanisms,
                    fingerprints.size(),
                    bssid.isWPS(),
                    bssid.getLastSeen()
            ));
        }


        return Response.ok(BSSIDsResponse.create(bssids.size(), bssids)).build();
    }

    @GET
    @Path("/bssids/{bssid}/ssids/{ssid}")
    public Response ssid(@PathParam("bssid") @NotNull String bssid,
                         @PathParam("ssid") @NotNull String ssid,
                         @QueryParam("include_history") @DefaultValue("false") boolean includeHistory) {
        bssid = bssid.toLowerCase();

        if (nzyme.getNetworks().getBSSIDs().containsKey(bssid)) {
            // Get a copy of the BSSID.
            BSSID b = Maps.newHashMap(nzyme.getNetworks().getBSSIDs()).get(bssid);

            if (b.ssids().containsKey(ssid)) {
                SSID s = b.ssids().get(ssid);

                // Security.
                List<SSIDSecurityResponse> security = Lists.newArrayList();
                for (Dot11SecurityConfiguration sec : s.getSecurity()) {
                    security.add(SSIDSecurityResponse.create(
                            sec.wpaMode(), sec.keyManagementModes(), sec.encryptionModes(), sec.asString()
                    ));
                }

                // Fingerprints.
                List<String> fingerprints = Lists.newArrayList();
                for (Channel channel : s.channels().values()) {
                    for (String fingerprint : channel.fingerprints()) {
                        if (!fingerprints.contains(fingerprint)) {
                            fingerprints.add(fingerprint);
                        }
                    }
                }

                // Channels.
                Map<Integer, ChannelResponse> channels = Maps.newTreeMap();
                for (Channel c : s.channels().values()) {
                    channels.put(c.channelNumber(), ChannelResponse.create(
                            c.channelNumber(),
                            b.bssid(),
                            s.nameSafe(),
                            c.totalFrames().get(),
                            fingerprints,
                            c.getSignalStrengthTable().getZScoreDistributionHistogram()
                    ));
                }

                return Response.ok(SSIDResponse.create(
                        security,
                        b.bssid(),
                        s.isHumanReadable(),
                        s.nameSafe(),
                        channels,
                        s.beaconRate(),
                        includeHistory ? buildBeaconRateHistory(b, s) : null
                )).build();
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

    private final AverageBeaconRate createEmptyAverageBeaconRate(DateTime at) {
        return AverageBeaconRate.create(0.0F, at);
    }

    public List<AverageBeaconRate> buildBeaconRateHistory(BSSID b, SSID s) {
        DateTime yesterday = DateTime.now().minusDays(1);

        List<AverageBeaconRate> beaconRateHistory = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery(BEACON_RATE_AVERAGE_QUERY)
                        .bind(0, b.bssid())
                        .bind(1, s.name())
                        .mapTo(AverageBeaconRate.class)
                        .list()
        );

        // Always have charts go from now to -24h.
        if (!beaconRateHistory.isEmpty()) {
            beaconRateHistory.set(0, createEmptyAverageBeaconRate(yesterday));
        } else {
            beaconRateHistory.add(createEmptyAverageBeaconRate(yesterday));
        }
        beaconRateHistory.add(createEmptyAverageBeaconRate(DateTime.now()));

        return beaconRateHistory;
    }

}
