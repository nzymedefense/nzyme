package app.nzyme.core.timelines.resolvers.dot11.bssid;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.shared.db.TapBasedSignalStrengthResult;
import app.nzyme.core.timelines.resolvers.ResolverResult;
import app.nzyme.core.util.TimeRange;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;

import java.util.*;

public class Dot11BSSIDStrongestTapResolver {

    private final NzymeNode nzyme;

    public Dot11BSSIDStrongestTapResolver(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }


    public Optional<ResolverResult> resolve(String bssid, List<UUID> taps) {
        DateTime now = DateTime.now();

        Optional<TapBasedSignalStrengthResult> currentStrongest = nzyme.getDot11().findBSSIDSignalStrengthPerTap(
                    bssid, TimeRange.create(now.minusMinutes(5), now, false), taps
                ).stream()
                .max(Comparator.comparingDouble(TapBasedSignalStrengthResult::signalStrength));

        if (currentStrongest.isEmpty()) {
            return Optional.empty();
        }

        Optional<TapBasedSignalStrengthResult> previousStrongest = nzyme.getDot11().findBSSIDSignalStrengthPerTap(
                        bssid, TimeRange.create(now.minusMinutes(5*2), now.minusMinutes(5), false), taps
                ).stream()
                .max(Comparator.comparingDouble(TapBasedSignalStrengthResult::signalStrength));

        Map<String, Object> resultPayload = Maps.newHashMap();

        if  (previousStrongest.isEmpty() || !currentStrongest.get().tapUuid().equals(previousStrongest.get().tapUuid())) {
            resultPayload.put("strongest_tap_uuid", currentStrongest.get().tapUuid());
            resultPayload.put("strongest_tap_name", currentStrongest.get().tapName());
            return Optional.of(ResolverResult.create(resultPayload));
        }

        return Optional.empty();
    }

}
