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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.configuration.Dot11BSSIDDefinition;
import horse.wtf.nzyme.configuration.Dot11NetworkDefinition;
import horse.wtf.nzyme.configuration.ExpectedSignalStrength;
import horse.wtf.nzyme.dot11.Dot11SecurityConfiguration;
import horse.wtf.nzyme.dot11.networks.BSSID;
import horse.wtf.nzyme.dot11.networks.Channel;
import horse.wtf.nzyme.dot11.networks.SSID;
import horse.wtf.nzyme.dot11.networks.beaconrate.AverageBeaconRate;
import horse.wtf.nzyme.dot11.networks.signalstrength.SignalIndexHistogramHistoryDBEntry;
import horse.wtf.nzyme.dot11.networks.signalstrength.SignalStrengthTable;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public static final String HISTOGRAM_HISTORY_QUERY = "SELECT histogram, created_at FROM sigidx_histogram_history " +
            "WHERE bssid = ? AND ssid = ? AND channel = ? AND created_at > (current_timestamp at time zone 'UTC' - interval <lookback>) " +
            "ORDER BY created_at ASC";

    @Inject
    private Nzyme nzyme;

    @Inject
    private ObjectMapper om;

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
                    bssid.averageRecentSignalStrength(),
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
                         @QueryParam("include_history") @DefaultValue("false") boolean includeHistory,
                         @QueryParam("history_seconds") @DefaultValue("10800") int historySeconds) {
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

                // Channels.
                Map<Integer, ChannelResponse> channels = Maps.newTreeMap();
                for (Channel c : s.channels().values()) {
                    channels.put(c.channelNumber(), ChannelResponse.create(
                            c.channelNumber(),
                            b.bssid(),
                            s.nameSafe(),
                            c.totalFrames().get(),
                            c.fingerprints(),
                            c.signalStrengthTable().getSignalDistributionHistogram(),
                            SignalStrengthTable.RETENTION_MINUTES,
                            includeHistory ? buildSignalIndexHistogramHistory(b, s, c, historySeconds) : null
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

                return Response.ok(SSIDResponse.create(
                        security,
                        b.bssid(),
                        s.isHumanReadable(),
                        s.nameSafe(),
                        channels,
                        fingerprints,
                        s.beaconRate(),
                        includeHistory ? buildBeaconRateHistory(b, s) : null,
                        findBeaconRateThresholdOfNetwork(b, s).orElse(null),
                        findExpectedSignalStrengthOfNetwork(b, s).orElse(null),
                        findNetworkDefinition(b, s).isPresent()
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

    private AverageBeaconRate createEmptyAverageBeaconRate(DateTime at) {
        return AverageBeaconRate.create(0.0F, at);
    }

    private List<AverageBeaconRate> buildBeaconRateHistory(BSSID b, SSID s) {
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
    
    private Optional<Dot11NetworkDefinition> findNetworkDefinition(BSSID b, SSID s) {
        for (Dot11NetworkDefinition dot11Network : nzyme.getConfiguration().dot11Networks()) {
            if (dot11Network.allBSSIDAddresses().contains(b.bssid()) && dot11Network.ssid().equals(s.name())) {
                return Optional.of(dot11Network);
            }
        }

        return Optional.empty();
    }
    
    private Optional<Dot11BSSIDDefinition> findBSSIDDefinition(BSSID b, SSID s) {
        Optional<Dot11NetworkDefinition> network = findNetworkDefinition(b, s);
        if (network.isPresent()) {
            for (Dot11BSSIDDefinition bssid : network.get().bssids()) {
                if (bssid.address().equals(b.bssid())) {
                    return Optional.of(bssid);
                }
            }
        }

        return Optional.empty();
    }

    private Optional<Integer> findBeaconRateThresholdOfNetwork(BSSID b, SSID s) {
        return findNetworkDefinition(b, s).map(Dot11NetworkDefinition::beaconRate);
    }

    private Optional<ExpectedSignalStrengthResponse> findExpectedSignalStrengthOfNetwork(BSSID b, SSID s) {
        Optional<Dot11BSSIDDefinition> bssid = findBSSIDDefinition(b, s);
        if (bssid.isPresent()) {
            ExpectedSignalStrength expected = bssid.get().expectedSignalStrength();
            return Optional.of(ExpectedSignalStrengthResponse.create(expected.from(), expected.to()));
        } else {
            return Optional.empty();
        }
    }

    // TODO better use an AutoValue object to return here lol
    private Map<String, List> buildSignalIndexHistogramHistory(BSSID b, SSID s, Channel c, int seconds) {
        List<SignalIndexHistogramHistoryDBEntry> values = nzyme.getDatabase().withHandle(handle ->
                handle.createQuery(HISTOGRAM_HISTORY_QUERY)
                        .bind(0, b.bssid())
                        .bind(1, s.name())
                        .bind(2, c.channelNumber())
                        .define("lookback", "'" + seconds + " seconds'") // TODO this is fucked
                        .mapTo(SignalIndexHistogramHistoryDBEntry.class)
                        .list()
        );

        // Transform the histogram string blobs from the database to structured data.
        List<List<Long>> z = Lists.newArrayList();
        List<DateTime> y = Lists.newArrayList();
        for (SignalIndexHistogramHistoryDBEntry value : values) {
            try {
                List<Long> entries = new ArrayList<>();
                Map<Integer, Long> tempReduced = Maps.newHashMap();
                Map<Integer, Long> histogram = om.readValue(value.histogram(), new TypeReference<Map<Integer, Long>>(){});

                for (Map.Entry<Integer, Long> x : histogram.entrySet()) {
                    tempReduced.put(x.getKey(), x.getValue());
                }

                for(int cnt = -100; cnt < 0; cnt++) {
                    entries.add(tempReduced.getOrDefault(cnt, 0L));
                }

                z.add(entries);
                y.add(value.createdAt().withSecondOfMinute(0));
            } catch (Exception e) {
                LOG.error("Could not parse histogram blob to structured data for BSSID [{}].", b, e);
            }
        }

        // X Axis.
        List<Integer> x = Lists.newArrayList();
        for(int cnt = -100; cnt < 0; cnt++) {
            x.add(cnt);
        }

        Map<String, List> coords = Maps.newHashMap();
        coords.put("z", z);
        coords.put("x", x);
        coords.put("y", y);

        return coords;
    }

}
