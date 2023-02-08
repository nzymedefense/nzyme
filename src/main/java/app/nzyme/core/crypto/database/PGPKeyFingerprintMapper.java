package app.nzyme.core.crypto.database;

import app.nzyme.core.crypto.PGPKeyFingerprint;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PGPKeyFingerprintMapper implements RowMapper<PGPKeyFingerprint> {

    @Override
    public PGPKeyFingerprint map(ResultSet rs, StatementContext ctx) throws SQLException {
        return PGPKeyFingerprint.create(
                UUID.fromString(rs.getString("node_id")),
                rs.getString("node_name"),
                rs.getString("key_signature"),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}
