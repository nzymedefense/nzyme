package app.nzyme.core.util;

import app.nzyme.core.rest.parameters.TimeRangeParameter;
import com.google.common.base.Strings;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

public class TimeRangeFactory {

    public static TimeRange fromRestQuery(TimeRangeParameter tr) {
        DateTime now = DateTime.now();

        switch (tr.type()) {
            case "relative":
                if (tr.minutes() == null || tr.minutes() <= 0) {
                    throw new IllegalArgumentException("Invalid time range parameters provided.");
                }

                return TimeRange.create(now.minusMinutes(tr.minutes()), now);
            case "absolute":
                if (tr.from() == null || tr.to() == null || tr.from().isAfter(tr.to())) {
                    throw new IllegalArgumentException("Invalid time range parameters provided.");
                }

                return TimeRange.create(tr.from().withMillisOfSecond(0), tr.to().withMillisOfSecond(0));
            case "named":
                if (Strings.isNullOrEmpty(tr.name())) {
                    throw new IllegalArgumentException("Invalid time range parameters provided.");
                }

                switch (tr.name()) {
                    case "today":
                        return TimeRange.create(
                                now.withTimeAtStartOfDay(),
                                now.plusDays(1).withTimeAtStartOfDay().minusSeconds(1)
                        );
                    case "yesterday":
                        return TimeRange.create(
                                now.withTimeAtStartOfDay().minusDays(1),
                                now.withTimeAtStartOfDay().minusSeconds(1)
                        );
                    case "week_to_date":
                        return TimeRange.create(
                                now.withTimeAtStartOfDay().dayOfWeek().withMinimumValue(),
                                now.plusDays(1).withTimeAtStartOfDay().minusSeconds(1)
                        );
                    case "month_to_date":
                        return TimeRange.create(
                                now.withTimeAtStartOfDay().dayOfMonth().withMinimumValue(),
                                now.plusDays(1).withTimeAtStartOfDay().minusSeconds(1)
                        );
                   case "all_time":
                       return TimeRange.create(new DateTime(0), new DateTime().plusYears(1000));
                }
            default:
                throw new IllegalArgumentException("Unknown time range type provided.");
        }
    }

}
