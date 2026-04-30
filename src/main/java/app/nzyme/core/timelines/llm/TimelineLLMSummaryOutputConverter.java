package app.nzyme.core.timelines.llm;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TimelineLLMSummaryOutputConverter {

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record BSSIDAnalysis(
            String deviceType,
            String mobility,
            String schedule,
            String advertisedSsids,
            String lifecycle,
            String threatClassification,
            String confidence,
            List<String> threatIndicators,
            String recommendedAction
    ) {}

    public static BSSIDAnalysis parse(String raw) {
        // Strip everything after ### END OF OUTPUT ###
        int end = raw.indexOf("### END OF OUTPUT ###");
        String text = end >= 0 ? raw.substring(0, end).trim() : raw.trim();

        Map<String, String> fields = new LinkedHashMap<>();
        List<String> indicators = new ArrayList<>();
        String[] lines = text.split("\n");
        boolean inIndicators = false;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("- ") && inIndicators) {
                indicators.add(line.substring(2).trim());
                continue;
            }

            inIndicators = false;

            int colon = line.indexOf(":");
            if (colon > 0) {
                String key = line.substring(0, colon).trim();
                String value = line.substring(colon + 1).trim();
                fields.put(key, value);
                if (key.equals("Threat Indicators")) {
                    inIndicators = true;
                }
            }
        }

        return new BSSIDAnalysis(
                fields.getOrDefault("Device Type", ""),
                fields.getOrDefault("Mobility", ""),
                fields.getOrDefault("Schedule", ""),
                fields.getOrDefault("Advertised SSIDs", ""),
                fields.getOrDefault("Lifecycle", ""),
                fields.getOrDefault("Threat Classification", ""),
                fields.getOrDefault("Confidence", ""),
                indicators,
                fields.getOrDefault("Recommended Action", "")
        );
    }

}
