package com.fitmap.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Helper class for date and time
 */
public class TimeHelper {
    public static int[] dayInMonth = {31,28,31,30,31,30,31,31,30,31,30,31};
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    public static SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat dateFormat3 = new SimpleDateFormat("MM/dd");
    private static final int TEST_SECONDS = 1000 * 5; //5 seconds

    /**
     * Calculate the remaining time to date d
     * @param d Date in "yyyy/MM/dd HH:mm:ss" format
     * @return remaining time in milliseconds
     */
    public static long remainTime(String d){
        try {
            Date date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(d);
            long remaining =  date.getTime() - System.currentTimeMillis();
            Log.d("remain", remaining + " ");
            return TEST_SECONDS;
          //  return remaining;
        } catch (ParseException e) {
            e.printStackTrace();
            return TEST_SECONDS;
        }
    }

    /**
     * Get date of the last 7 days
     * @return day of last 7 days in "yyyy-MM-dd" and "MM/dd" format
     */
    public static String[] weekDay(){
        String[] weekDay = new String[14];
        Date date = new Date();
        long oneDay = 1000 * 3600 * 24;
        for (int i = 0; i < 7; i ++){
            Date tmp = new Date();
            tmp.setTime(date.getTime() - oneDay * i);
            weekDay[6 - i] = dateFormat2.format(tmp);
            weekDay[13 - i] = dateFormat3.format(tmp);
        }
        return weekDay;
    }

    /**
     * Get current day
     * @return day in String format
     */
    public static String currentDay(){
        Calendar cal = Calendar.getInstance();
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        return String.valueOf(dayOfMonth);
    }

    /**
     * Get current month
     * @return month in String format
     */
    public static String currentMonth(){
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        return String.valueOf(month);
    }

    /**
     * Get current year
     * @return year in String format
     */
    public static String currentYear(){
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        return String.valueOf(year);
    }

    /**
     * Check if dataBase need to update.
     * @param oldDateS Old data in "yyyy/MM/dd HH:mm:ss" format.
     * @return True if it needs to update, false otherwise.
     */
    public static boolean isPastTime(String oldDateS){
        Date oldDate;
        try {
            oldDate = dateFormat.parse(oldDateS);
            Date currentDate = new Date();
            return currentDate.getTime() - oldDate.getTime() >= 0;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
}
