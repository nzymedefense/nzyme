package app.nzyme.core.timelines.llm;

import app.nzyme.core.timelines.db.TimelineEventEntry;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.joda.JodaModule;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.*;
import java.util.stream.Collectors;

public class TimelineEventTextConverter {

    // These are not included in event details at all.
    private static final Set<String> EXCLUDED_KEYS = Set.of(
            "strongest_tap_uuid",
            "previous_tap_uuid",
            "known_fingerprints",
            "known_ssids"
    );

    // These are truncated to save tokens.
    private static final Set<String> HASH_KEYS = Set.of(
            "new_fingerprints",
            "disappeared_fingerprints"
    );

    private static String truncateHashes(String value) {
        return value.replaceAll("\\b([0-9a-f]{8})[0-9a-f]{8,}\\b", "$1...");
    }

    private static final ObjectMapper OM = JsonMapper.builder()
            .addModule(new JodaModule())
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    public static String eventToText(List<TimelineEventEntry> events) {
        StringBuilder sb = new StringBuilder();
        List<TimelineEventEntry> sorted = events.stream()
                .sorted(Comparator.comparing(e -> e.timestamp().toInstant()))
                .toList();

        int i = 0;
        while (i < sorted.size()) {
            TimelineEventEntry event = sorted.get(i);

            if (event.eventType().equals("DOT11_BSSID_FINGERPRINT_DIFF")) {
                i++;
                continue;
            }

            if (event.eventType().equals("DOT11_BSSID_STRONGEST_TAP")) {
                List<TimelineEventEntry> tapRun = new ArrayList<>();
                while (i < sorted.size() &&
                        sorted.get(i).eventType().equals("DOT11_BSSID_STRONGEST_TAP")) {
                    tapRun.add(sorted.get(i));
                    i++;
                }
                sb.append(summarizeTapChanges(tapRun)).append("\n");
            } else {
                sb.append(formatEvent(event)).append("\n");
                i++;
            }
        }

        return sb.toString().trim();
    }

    public static String computeLifecycleSummary(List<TimelineEventEntry> events,
                                                 DateTime rangeStart,
                                                 DateTime rangeEnd) {
        if (events.isEmpty()) {
            return "No activity observed in time range.";
        }

        List<TimelineEventEntry> sorted = events.stream()
                .sorted(Comparator.comparing(e -> e.timestamp().toInstant()))
                .toList();

        DateTime firstSeen = sorted.get(0).timestamp();
        long rangeDays = new Duration(rangeStart, rangeEnd).getStandardDays();
        long daysPresent = new Duration(firstSeen, rangeEnd).getStandardDays();

        // Collect absence periods from GONE events
        List<TimelineEventEntry> goneEvents = sorted.stream()
                .filter(e -> e.eventType().equals("GONE"))
                .toList();

        List<Long> absenceMinutes = new ArrayList<>();
        for (TimelineEventEntry gone : goneEvents) {
            Map<String, Object> details = parseDetails(gone);
            Object minutesRaw = details.get("minutes");
            boolean ongoing = Boolean.TRUE.equals(details.get("ongoing"));
            if (!ongoing && minutesRaw instanceof Number) {
                absenceMinutes.add(((Number) minutesRaw).longValue());
            }
        }

        StringBuilder sb = new StringBuilder();

        // First seen relative to range
        long daysAgo = new Duration(firstSeen, rangeEnd).getStandardDays();
        sb.append("First seen ").append(daysAgo).append(" day(s) ago. ");
        sb.append("Present for ").append(daysPresent).append(" of ")
                .append(rangeDays).append(" days in range.");

        if (!absenceMinutes.isEmpty()) {
            long min = absenceMinutes.stream().mapToLong(Long::longValue).min().orElse(0);
            long max = absenceMinutes.stream().mapToLong(Long::longValue).max().orElse(0);
            long avg = (long) absenceMinutes.stream().mapToLong(Long::longValue).average().orElse(0);

            sb.append("\nAbsence periods: ").append(absenceMinutes.size()).append(" total");
            if (min == max) {
                sb.append(", all ").append(min).append(" minutes.");
            } else {
                sb.append(", ranging from ").append(min).append(" to ").append(max)
                        .append(" minutes, average ").append(avg).append(" minutes.");
            }
        } else {
            sb.append("\nNo absence periods recorded.");
        }

        return sb.toString();
    }

    public static String computeScheduleSummary(List<TimelineEventEntry> events,
                                                DateTime rangeEnd) {
        if (events.isEmpty()) {
            return "No activity observed in time range.";
        }

        List<TimelineEventEntry> sorted = events.stream()
                .sorted(Comparator.comparing(e -> e.timestamp().toInstant()))
                .toList();

        // Collect GONE events — each represents one absence period
        List<TimelineEventEntry> goneEvents = sorted.stream()
                .filter(e -> e.eventType().equals("GONE"))
                .toList();

        // Build activity periods between GONE events
        List<ActivityPeriod> periods = new ArrayList<>();
        DateTime periodStart = sorted.get(0).timestamp();

        for (TimelineEventEntry gone : goneEvents) {
            Map<String, Object> details = parseDetails(gone);

            // Period ends when device disappears
            DateTime gapStart = gone.timestamp();

            // Only add the period if it has meaningful duration
            if (new Duration(periodStart, gapStart).getStandardMinutes() > 0) {
                periods.add(new ActivityPeriod(periodStart, gapStart));
            }

            // Next period starts when device reappears
            Object gapEndRaw = details.get("gap_end");
            boolean ongoing = Boolean.TRUE.equals(details.get("ongoing"));

            if (!ongoing && gapEndRaw != null) {
                try {
                    periodStart = DateTime.parse(gapEndRaw.toString());
                } catch (Exception e) {
                    // If we can't parse gap_end, skip to next gone event
                    periodStart = gapStart;
                }
            }
        }

        // Add final period from last reappearance to end of range
        if (periodStart.isBefore(rangeEnd)) {
            periods.add(new ActivityPeriod(periodStart, rangeEnd));
        }

        if (periods.isEmpty()) {
            return "No activity periods could be determined.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(periods.size()).append(" active period(s):\n");

        for (ActivityPeriod period : periods) {
            long hours = period.duration().getStandardHours();
            long minutes = period.duration().getStandardMinutes() % 60;

            String duration;
            if (hours > 0) {
                duration = hours + "h " + minutes + "m";
            } else {
                duration = minutes + "m";
            }

            // Format without seconds for readability
            String start = period.start().toString("yyyy-MM-dd'T'HH:mmZ");
            String end = period.end().toString("yyyy-MM-dd'T'HH:mmZ");

            sb.append("- ").append(start)
                    .append(" to ").append(end)
                    .append(" (").append(duration).append(")\n");
        }

        return sb.toString().trim();
    }

    private record ActivityPeriod(DateTime start, DateTime end) {
        public Duration duration() {
            return new Duration(start, end);
        }
    }

    private static String summarizeTapChanges(List<TimelineEventEntry> tapEvents) {
        if (tapEvents.size() == 1) {
            return formatEvent(tapEvents.get(0));
        }

        Map<String, DoubleSummaryStatistics> tapRssi = new LinkedHashMap<>();

        for (TimelineEventEntry e : tapEvents) {
            Map<String, Object> d = parseDetails(e);
            String tapName = (String) d.getOrDefault("strongest_tap_name", "unknown");
            double rssi = ((Number) d.getOrDefault("strongest_tap_rssi", 0.0)).doubleValue();
            tapRssi.computeIfAbsent(tapName, k -> new DoubleSummaryStatistics());
            tapRssi.get(tapName).accept(rssi);
        }

        String first = tapEvents.get(0).timestamp().toString();
        String last = tapEvents.get(tapEvents.size() - 1).timestamp().toString();

        String tapSummary = tapRssi.entrySet().stream()
                .map(e -> e.getKey() + " (rssi: " +
                        String.format("%.1f", e.getValue().getAverage()) + " avg)")
                .collect(Collectors.joining(", "));

        return first + " to " + last +
                " | DOT11_BSSID_STRONGEST_TAP | " + tapEvents.size() +
                " changes between: " + tapSummary;
    }

    private static String formatEvent(TimelineEventEntry event) {
        Map<String, Object> details = parseDetails(event);

        String detailStr = details.entrySet().stream()
                .filter(e -> !EXCLUDED_KEYS.contains(e.getKey()))
                .map(e -> e.getKey() + ": " +
                        (HASH_KEYS.contains(e.getKey())
                                ? truncateHashes(e.getValue().toString())
                                : formatValue(e.getValue())))
                .collect(Collectors.joining(", "));

        return event.timestamp().toString()
                + " | " + event.eventType()
                + " | " + detailStr;
    }

    private static String formatValue(Object value) {
        if (value instanceof Double || value instanceof Float) {
            return String.format("%.1f", ((Number) value).doubleValue());
        }
        return value.toString();
    }

    private static Map<String, Object> parseDetails(TimelineEventEntry event) {
        if (event.eventDetails() == null) return Collections.emptyMap();
        try {
            return OM.readValue(event.eventDetails(), new TypeReference<>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

}