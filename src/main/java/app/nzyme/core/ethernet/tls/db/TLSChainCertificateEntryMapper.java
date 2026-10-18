package app.nzyme.core.ethernet.tls.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

import static app.nzyme.core.ethernet.tls.db.TLSSessionEntryMapper.*;

public class TLSChainCertificateEntryMapper implements RowMapper<TLSChainCertificateEntry> {

    @Override
    public TLSChainCertificateEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TLSChainCertificateEntry.builder()
                .sha256(rs.getString("sha256"))
                .parsed(rs.getTimestamp("parsed_at") != null && rs.getString("parse_error") == null)
                .subject(rs.getString("subject"))
                .issuer(rs.getString("issuer"))
                .commonName(rs.getString("common_name"))
                .notBefore(nullableDateTime(rs, "not_before"))
                .notAfter(nullableDateTime(rs, "not_after"))
                .isCa(nullableBoolean(rs, "is_ca"))
                .selfSigned(nullableBoolean(rs, "self_signed"))
                .publicKeyAlgorithm(rs.getString("public_key_algorithm"))
                .publicKeyBits(nullableInt(rs, "public_key_bits"))
                .signatureAlgorithm(rs.getString("signature_algorithm"))
                .build();
    }

}
