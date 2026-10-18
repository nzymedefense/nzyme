package app.nzyme.core.ethernet.tls.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static app.nzyme.core.ethernet.tls.db.TLSSessionEntryMapper.*;

public class TLSCertificateEntryMapper implements RowMapper<TLSCertificateEntry> {

    @Override
    public TLSCertificateEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TLSCertificateEntry.builder()
                .sha256(rs.getString("sha256"))
                .sha1(rs.getString("sha1"))
                .parsed(rs.getTimestamp("parsed_at") != null && rs.getString("parse_error") == null)
                .subject(rs.getString("subject"))
                .issuer(rs.getString("issuer"))
                .commonName(rs.getString("common_name"))
                .subjectAlternativeNames(stringList(rs.getArray("subject_alternative_names")))
                .notBefore(nullableDateTime(rs, "not_before"))
                .notAfter(nullableDateTime(rs, "not_after"))
                .isCa(nullableBoolean(rs, "is_ca"))
                .selfSigned(nullableBoolean(rs, "self_signed"))
                .publicKeyAlgorithm(rs.getString("public_key_algorithm"))
                .publicKeyBits(nullableInt(rs, "public_key_bits"))
                .signatureAlgorithm(rs.getString("signature_algorithm"))
                .extendedKeyUsage(stringList(rs.getArray("extended_key_usage")))
                .firstSeen(new DateTime(rs.getTimestamp("first_seen")))
                .lastSeen(new DateTime(rs.getTimestamp("last_seen")))
                .sessionCount(rs.getLong("session_count"))
                .sources(stringList(rs.getArray("sources")))
                .build();
    }

    private static List<String> stringList(Array array) throws SQLException {
        if (array == null) {
            return List.of();
        }

        return Arrays.asList((String[]) array.getArray());
    }

}
