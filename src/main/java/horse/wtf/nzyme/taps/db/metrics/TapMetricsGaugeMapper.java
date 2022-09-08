/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package horse.wtf.nzyme.taps.db.metrics;

import horse.wtf.nzyme.taps.metrics.TapMetricsGauge;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TapMetricsGaugeMapper implements RowMapper<TapMetricsGauge> {

    @Override
    public TapMetricsGauge map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TapMetricsGauge.create(
                rs.getString("metric_name"),
                rs.getDouble("metric_value"),
                new DateTime(rs.getTimestamp("created_at"))
        );
    }

}

