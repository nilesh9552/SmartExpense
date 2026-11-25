package com.smartexpense.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private static final SimpleDateFormat MONTH_YEAR_FORMAT = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private static final SimpleDateFormat CSV_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DAY_SHORT_FORMAT = new SimpleDateFormat("EEE", Locale.getDefault());

    public static String formatDate(long timestamp) {
        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(timestamp);

        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        if (isSameDay(target, today)) {
            return "Today, " + DATE_FORMAT.format(new Date(timestamp));
        } else if (isSameDay(target, yesterday)) {
            return "Yesterday, " + DATE_FORMAT.format(new Date(timestamp));
        } else {
            return DATE_FORMAT.format(new Date(timestamp));
        }
    }

    public static String formatSimpleDate(long timestamp) {
        return DATE_FORMAT.format(new Date(timestamp));
    }

    public static String formatMonthYear(long timestamp) {
        return MONTH_YEAR_FORMAT.format(new Date(timestamp));
    }

    public static String formatCsvDate(long timestamp) {
        return CSV_DATE_FORMAT.format(new Date(timestamp));
    }

    public static String formatDayShort(long timestamp) {
        return DAY_SHORT_FORMAT.format(new Date(timestamp));
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static long getStartOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long getEndOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    public static long getStartOfWeek(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        return getStartOfDay(cal.getTimeInMillis());
    }

    public static long getEndOfWeek(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(getStartOfWeek(timestamp));
        cal.add(Calendar.DAY_OF_WEEK, 6);
        return getEndOfDay(cal.getTimeInMillis());
    }

    public static long getStartOfMonth(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return getStartOfDay(cal.getTimeInMillis());
    }

    public static long getEndOfMonth(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return getEndOfDay(cal.getTimeInMillis());
    }

    public static long getStartOfYear(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        return getStartOfDay(cal.getTimeInMillis());
    }

    public static long getEndOfYear(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        return getEndOfDay(cal.getTimeInMillis());
    }
}
