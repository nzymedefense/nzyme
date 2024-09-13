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

                return TimeRange.create(now.minusMinutes(tr.minutes()), now, false);
            case "absolute":
                if (tr.from() == null || tr.to() == null || tr.from().isAfter(tr.to())) {
                    throw new IllegalArgumentException("Invalid time range parameters provided.");
                }

                return TimeRange.create(tr.from().withMillisOfSecond(0), tr.to().withMillisOfSecond(0), false);
            case "named":
                if (Strings.isNullOrEmpty(tr.name())) {
                    throw new IllegalArgumentException("Invalid time range parameters provided.");
                }

                switch (tr.name()) {
                    case "today":
                        return TimeRange.create(
                                now.withTimeAtStartOfDay(),
                                now.plusDays(1).withTimeAtStartOfDay().minusSeconds(1),
                                false
                        );
                    case "yesterday":
                        return TimeRange.create(
                                now.withTimeAtStartOfDay().minusDays(1),
                                now.withTimeAtStartOfDay().minusSeconds(1),
                                false
                        );
                    case "week_to_date":
                        return TimeRange.create(
                                now.withTimeAtStartOfDay().dayOfWeek().withMinimumValue(),
                                now.plusDays(1).withTimeAtStartOfDay().minusSeconds(1),
                                false
                        );
                    case "month_to_date":
                        return TimeRange.create(
                                now.withTimeAtStartOfDay().dayOfMonth().withMinimumValue(),
                                now.plusDays(1).withTimeAtStartOfDay().minusSeconds(1),
                                false
                        );
                   case "all_time":
                       return allTime();
                }
            default:
                throw new IllegalArgumentException("Unknown time range type provided.");
        }
    }

    public static TimeRange allTime() {
        return TimeRange.create(new DateTime(0), new DateTime().plusYears(1000), true);
    }

    public static TimeRange oneMinute() {
        DateTime now = DateTime.now();
        return TimeRange.create(now.minusMinutes(1), now, false);
    }

    public static TimeRange fifteenMinutes() {
        DateTime now = DateTime.now();
        return TimeRange.create(now.minusMinutes(15), now, false);
    }

    public static TimeRange eightHours() {
        DateTime now = DateTime.now();
        return TimeRange.create(now.minusHours(8), now, false);
    }

    public static TimeRange oneDay() {
        DateTime now = DateTime.now();
        return TimeRange.create(now.minusHours(24), now, false);
    }

}
