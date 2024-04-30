package app.nzyme.core.taps.db.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class Dot11FrequencyAndChannelWidthEntryMapper implements RowMapper<Dot11FrequencyAndChannelWidthEntry> {

    private static final ObjectMapper OM = new ObjectMapper();

    @Override
    public Dot11FrequencyAndChannelWidthEntry map(ResultSet rs, StatementContext ctx) throws SQLException {
        List<String> channelWidths;

        try {
            channelWidths = OM.readValue(
                    rs.getString("channel_widths"),
                    TypeFactory.defaultInstance().constructCollectionType(List.class, String.class)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Dot11FrequencyAndChannelWidthEntry.create(
                rs.getLong("id"),
                UUID.fromString(rs.getString("interface_uuid")),
                rs.getInt("frequency"),
                channelWidths
        );
    }

}
