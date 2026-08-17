package com.yan.xpay.utils;

import java.util.Calendar;
import java.util.Date;

public final class DashboardRangeUtils {

    private DashboardRangeUtils() {}

    /** @return [startInclusive, endExclusive] */
    public static Date[] resolve(String range) {
        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);
        end.add(Calendar.DAY_OF_MONTH, 1); // tomorrow 00:00

        Calendar start = (Calendar) end.clone();
        switch (range == null ? "" : range) {
            case "today" -> start.add(Calendar.DAY_OF_MONTH, -1);
            case "7d" -> start.add(Calendar.DAY_OF_MONTH, -7);
            case "30d" -> start.add(Calendar.DAY_OF_MONTH, -30);
            default -> throw new IllegalArgumentException("Unsupported range: " + range);
        }
        return new Date[]{start.getTime(), end.getTime()};
    }
}
