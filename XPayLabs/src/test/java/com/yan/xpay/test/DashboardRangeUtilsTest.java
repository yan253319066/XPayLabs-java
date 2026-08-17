package com.yan.xpay.test;

import com.yan.xpay.utils.DashboardRangeUtils;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class DashboardRangeUtilsTest {

    @Test
    void today_startsAtMidnight_endsTomorrow() {
        Date[] r = DashboardRangeUtils.resolve("today");
        Calendar c = Calendar.getInstance();
        c.setTime(r[0]);
        assertEquals(0, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.MINUTE));
        assertTrue(r[1].after(r[0]));
        long hours = (r[1].getTime() - r[0].getTime()) / 3_600_000L;
        assertEquals(24, hours);
    }

    @Test
    void sevenDays_spansSevenDays() {
        Date[] r = DashboardRangeUtils.resolve("7d");
        long days = (r[1].getTime() - r[0].getTime()) / 86_400_000L;
        assertEquals(7, days);
    }

    @Test
    void thirtyDays_spansThirtyDays() {
        Date[] r = DashboardRangeUtils.resolve("30d");
        long days = (r[1].getTime() - r[0].getTime()) / 86_400_000L;
        assertEquals(30, days);
    }

    @Test
    void invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> DashboardRangeUtils.resolve("year"));
    }
}
