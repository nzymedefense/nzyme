package app.nzyme.core.gnss;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.shared.db.GenericIntegerHistogramEntry;
import app.nzyme.core.util.Bucketing;
import app.nzyme.core.util.TimeRange;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Gnss {

    private final NzymeNode nzyme;

    public Gnss(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    public List<GenericIntegerHistogramEntry> getTimeDeviationHistogram(TimeRange timeRange,
                                                                        Bucketing.BucketingConfiguration bucketing,
                                                                        List<UUID> taps) {
        if (taps.isEmpty()) {
            return Collections.emptyList();
        }

        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT date_trunc(:date_trunc, timestamp) AS bucket, " +
                                "ROUND(AVG(maximum_time_deviation_ms)) AS value " +
                                "FROM gnss_constellations WHERE created_at >= :tr_from AND created_at <= :tr_to " +
                                "AND tap_uuid IN (<taps>) " +
                                "GROUP BY bucket ORDER BY bucket DESC")
                        .bind("date_trunc", bucketing.type().getDateTruncName())
                        .bind("tr_from", timeRange.from())
                        .bind("tr_to", timeRange.to())
                        .bindList("taps", taps)
                        .mapTo(GenericIntegerHistogramEntry.class)
                        .list()
        );
    }

}
