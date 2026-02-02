package app.nzyme.core.dot11.db;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.sql.SQLException;
import java.util.List;

public class SecuritySettingsParser {

    private static final ObjectMapper om = new ObjectMapper();

    public static List<Dot11SecuritySuiteJson> parseSecuritySettings(java.sql.Array secArr) throws SQLException {
        List<Dot11SecuritySuiteJson> securitySuites = Lists.newArrayList();

        if (secArr == null) {
            return securitySuites;
        }

        Object[] elems = (Object[]) secArr.getArray();
        for (Object elem : elems) {
            if (elem == null) continue;

            final String json;
            if (elem instanceof org.postgresql.util.PGobject pg) {
                json = pg.getValue();
            } else {
                json = elem.toString();
            }

            try {
                JsonNode root = om.readTree(json);

                JsonNode suites = root.get("suites");
                if (suites == null || !suites.isObject()) {
                    continue;
                }

                Dot11SecuritySuiteJson suite =
                        Dot11SecuritySuiteJson.builder()
                                .groupCipher(suites.path("group_cipher").asText(null))
                                .pairwiseCiphers(joinTextArray(suites.get("pairwise_ciphers")))
                                .keyManagementModes(joinTextArray(suites.get("key_management_modes")))
                                .pmfMode(root.path("pmf").asText(null))
                                .build();

                securitySuites.add(suite);

            } catch (Exception e) {
                throw new RuntimeException("Failed to parse security_settings JSON", e);
            }
        }

        return securitySuites;
    }

    private static String joinTextArray(JsonNode arrayNode) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return null;
        }

        List<String> values = Lists.newArrayList();
        for (JsonNode n : arrayNode) {
            if (n.isTextual()) {
                values.add(n.asText());
            }
        }

        return values.isEmpty() ? null : Joiner.on(',').join(values);
    }
}