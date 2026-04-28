package app.nzyme.core.timelines;

import app.nzyme.core.timelines.db.TimelineEventEntry;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.joda.JodaModule;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TimelineEventTextConverter {

    // These are not included in event details at all.
    private static final Set<String> EXCLUDED_KEYS = Set.of(
            "strongest_tap_uuid",
            "known_fingerprints",
            "known_ssids"
    );

    // These are truncated to save tokens.
    private static final Set<String> HASH_KEYS = Set.of(
            "new_fingerprints",
            "disappeared_fingerprints"
    );

    private static String truncateHashes(String value) {
        // Matches 16+ character hex strings (fingerprint hashes)
        return value.replaceAll("\\b([0-9a-f]{8})[0-9a-f]{8,}\\b", "$1...");
    }

    private static ObjectMapper OM = JsonMapper.builder()
            .addModule(new JodaModule())
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    public static String eventToText(List<TimelineEventEntry> events) {
        StringBuilder sb = new StringBuilder();

        for (TimelineEventEntry event : events) {
            Map<String, Object> details = Collections.emptyMap();
            if (event.eventDetails() != null) {
                details = OM.readValue(event.eventDetails(), new TypeReference<>() {});
            }


            String detailStr = details.entrySet().stream()
                    .filter(e -> !EXCLUDED_KEYS.contains(e.getKey()))
                    .map(e -> e.getKey() + ": " +
                            (HASH_KEYS.contains(e.getKey()) ? truncateHashes(e.getValue().toString()) : e.getValue()))
                    .collect(Collectors.joining(", "));

            sb.append(event.timestamp().toString())
                    .append(" | ").append(event.eventType())
                    .append(" | ").append(detailStr)
                    .append("\n");
        }

        return sb.toString().trim();
    }

}
