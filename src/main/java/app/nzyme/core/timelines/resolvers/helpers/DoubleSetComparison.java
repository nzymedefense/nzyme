package app.nzyme.core.timelines.resolvers.helpers;

import app.nzyme.core.timelines.resolvers.ResolverResult;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DoubleSetComparison {

    public static Optional<ResolverResult> compare(Optional<Set<Double>> previous,
                                                   Set<Double> current,
                                                   String appearedKey,
                                                   String disappearedKey,
                                                   String currentKey) {
        if (previous.isEmpty()) {
            Map<String, Object> payload = Maps.newHashMap();
            payload.put(appearedKey, current);
            payload.put(currentKey, current);
            return Optional.of(ResolverResult.create(payload));
        }

        Set<Double> appeared = Sets.difference(current, previous.get());
        Set<Double> disappeared = Sets.difference(previous.get(), current);

        if (appeared.isEmpty() && disappeared.isEmpty()) {
            return Optional.empty();
        }

        Map<String, Object> payload = Maps.newHashMap();
        if (!appeared.isEmpty()) {
            payload.put(appearedKey, appeared);
        }
        if (!disappeared.isEmpty()) {
            payload.put(disappearedKey, disappeared);
        }

        payload.put(currentKey, current);

        return Optional.of(ResolverResult.create(payload));
    }

}
