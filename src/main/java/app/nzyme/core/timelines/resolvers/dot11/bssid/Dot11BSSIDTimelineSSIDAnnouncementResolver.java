package app.nzyme.core.timelines.resolvers.dot11.bssid;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.db.SSIDChannelDetails;
import app.nzyme.core.timelines.resolvers.ResolverResult;
import app.nzyme.core.util.TimeRange;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;

import java.util.*;
import java.util.stream.Collectors;

public class Dot11BSSIDTimelineSSIDAnnouncementResolver {

    private final NzymeNode nzyme;

    public Dot11BSSIDTimelineSSIDAnnouncementResolver(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public Optional<ResolverResult> resolve(String bssid, List<UUID> taps) {
        DateTime now = DateTime.now();

        Set<String> currentSSIDNames = nzyme.getDot11().findSSIDsOfBSSID(
                TimeRange.create(now.minusMinutes(5 ), now, false),
                bssid,
                taps
        ).stream().map(SSIDChannelDetails::ssid).collect(Collectors.toSet());

        Set<String> previousSSIDNames = nzyme.getDot11().findSSIDsOfBSSID(
                TimeRange.create(now.minusMinutes(5*2), now.minusMinutes(5), false),
                bssid,
                taps
        ).stream().map(SSIDChannelDetails::ssid).collect(Collectors.toSet());

        // BSSID was not seen in both windows. Not comparing.
        if (currentSSIDNames.isEmpty() || previousSSIDNames.isEmpty()) {
            return Optional.empty();
        }

        Set<String> newSSIDs = Sets.difference(currentSSIDNames, previousSSIDNames);
        Set<String> disappearedSSIDs = Sets.difference(previousSSIDNames, currentSSIDNames);

        if (newSSIDs.isEmpty() &&  disappearedSSIDs.isEmpty()) {
            return Optional.empty();
        }

        Map<String, Object> resultPayload = Maps.newHashMap();
        if (!newSSIDs.isEmpty()) {
            resultPayload.put("new_ssids", newSSIDs);
        }

        if (!disappearedSSIDs.isEmpty()) {
            resultPayload.put("disappeared_ssids", disappearedSSIDs);
        }

        return Optional.of(ResolverResult.create(resultPayload));
    }

}
