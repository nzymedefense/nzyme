package horse.wtf.nzyme.crypto.database;

import horse.wtf.nzyme.crypto.PGPKeyFingerprint;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PGPKeyFingerprintMapper implements RowMapper<PGPKeyFingerprint> {

    @Override
    public PGPKeyFingerprint map(ResultSet rs, StatementContext ctx) throws SQLException {
        return PGPKeyFingerprint.create(
                rs.getString("node"),
                rs.getString("key_signature"),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}
