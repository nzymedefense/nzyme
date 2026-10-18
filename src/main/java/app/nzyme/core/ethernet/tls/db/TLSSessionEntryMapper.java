package app.nzyme.core.ethernet.tls.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class TLSSessionEntryMapper implements RowMapper<TLSSessionEntry> {

    private static final ObjectMapper OM = new ObjectMapper();
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};

    @Override
    public TLSSessionEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TLSSessionEntry.builder()
                .tcpSessionKey(rs.getString("tcp_session_key"))
                .sourceMac(rs.getString("source_mac"))
                .destinationMac(rs.getString("destination_mac"))
                .sourceAddress(rs.getString("source_address"))
                .sourcePort(rs.getInt("source_port"))
                .destinationAddress(rs.getString("destination_address"))
                .destinationPort(rs.getInt("destination_port"))
                .connectionStatus(rs.getString("connection_status"))
                .bytesCount(rs.getLong("bytes_count"))
                .serverName(rs.getString("server_name"))
                .version(rs.getString("version"))
                .clientMaxVersion(rs.getString("client_max_version"))
                .cipherSuite(nullableInt(rs, "cipher_suite"))
                .alpnSelected(rs.getString("alpn_selected"))
                .alpnOffered(stringListFromJson(rs.getString("alpn_offered")))
                .sessionResumed(rs.getBoolean("session_resumed"))
                .helloRetryRequest(rs.getBoolean("hello_retry_request"))
                .encryptedClientHello(rs.getBoolean("encrypted_client_hello"))
                .clientCertificateRequested(rs.getBoolean("client_certificate_requested"))
                .serverLeafSha256(rs.getString("server_leaf_sha256"))
                .serverLeafCommonName(rs.getString("server_leaf_common_name"))
                .serverLeafIssuer(rs.getString("server_leaf_issuer"))
                .serverLeafNotAfter(nullableDateTime(rs, "server_leaf_not_after"))
                .serverChainLength(nullableInt(rs, "server_chain_length"))
                .clientLeafSha256(rs.getString("client_leaf_sha256"))
                .serverLeafSelfSigned(nullableBoolean(rs, "server_leaf_self_signed"))
                .sniMatchesCertificate(nullableBoolean(rs, "sni_matches_certificate"))
                .leafValidAtHandshake(nullableBoolean(rs, "leaf_valid_at_handshake"))
                .chainTrusted(nullableBoolean(rs, "chain_trusted"))
                .chainTrustError(rs.getString("chain_trust_error"))
                .establishedAt(new DateTime(rs.getTimestamp("established_at")))
                .terminatedAt(nullableDateTime(rs, "terminated_at"))
                .mostRecentSegmentTime(new DateTime(rs.getTimestamp("most_recent_segment_time")))
                .durationMs(rs.getLong("duration_ms"))
                .build();
    }

    static List<String> stringListFromJson(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }

        try {
            return OM.readValue(json, STRING_LIST);
        } catch (Exception e) {
            return List.of();
        }
    }

    static Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    static Boolean nullableBoolean(ResultSet rs, String column) throws SQLException {
        boolean value = rs.getBoolean(column);
        return rs.wasNull() ? null : value;
    }

    static DateTime nullableDateTime(ResultSet rs, String column) throws SQLException {
        return rs.getTimestamp(column) == null ? null : new DateTime(rs.getTimestamp(column));
    }

}
