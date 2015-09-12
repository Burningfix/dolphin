package org.dolphin.lib;

/**
 * Time unit conversions.
 */
public class TimeConversions {
    public static final long NS_PER_MS = 1000000;
    public static final long NS_PER_US = 1000;
    public static final long US_PER_MS = 1000;
    public static final long NS_PER_SECOND = 1000 * 1000 * 1000;
    public static final long US_PER_SECOND = 1000 * 1000;
    public static final long MS_PER_SECOND = 1000;
    public static final long SECONDS_PER_MINUTE = 60;
    public static final long MINUTES_PER_HOUR = 60;
    public static final long HOURS_PER_DAY = 24;
    public static final long DAYS_PER_WEEK = 7;
    public static final long DAYS_PER_MONTH = 30;
    public static final long DAYS_PER_YEAR = 365;

    public static final long MS_PER_MINUTE = MS_PER_SECOND * SECONDS_PER_MINUTE;
    public static final long MS_PER_HOUR = MS_PER_MINUTE * MINUTES_PER_HOUR;
    public static final long MS_PER_DAY = MS_PER_HOUR * HOURS_PER_DAY;
    public static final long MS_PER_WEEK = MS_PER_DAY * DAYS_PER_WEEK;
    public static final long MS_PER_YEAR = MS_PER_DAY * DAYS_PER_YEAR;

    public static final long SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
    public static final long SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;
    public static final long SECONDS_PER_WEEK = SECONDS_PER_DAY * DAYS_PER_WEEK;
    public static final long SECONDS_PER_MONTH = SECONDS_PER_DAY * DAYS_PER_MONTH;
    public static final long SECONDS_PER_YEAR = SECONDS_PER_DAY * DAYS_PER_YEAR;
  /**
   * Convert time in millisecond to hours rounding down.
   */
  public static long millisecondsToHours(long timeMs) {
    return timeMs / MS_PER_HOUR;
  }

  /**
   * Convert time in milliseconds to minutes rounding down.
   */
  public static long millisecondsToMinutes(long timeMs) {
    return timeMs / MS_PER_MINUTE;
  }

  public static long millisecondsToDays(long timeMs) {
    return timeMs / MS_PER_DAY;
  }

  public static long millisecondsToYears(long timeMs) {
    return timeMs / MS_PER_YEAR;
  }

  /**
   * Convert time in milliseconds to seconds rounding down.
   */
  public static long millisecondsToSeconds(long timeMs) {
    return timeMs / MS_PER_SECOND;
  }
}
