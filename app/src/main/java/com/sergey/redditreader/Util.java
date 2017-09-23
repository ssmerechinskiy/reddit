package com.sergey.redditreader;

import android.content.Context;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by user on 23.09.2017.
 */

public class Util {

    public static String getAuthorString(String author) {
        return "author " + author;
    }

    public static String getCommentsString(int count) {
        if(count == 0) {
            return "";
        } else {
            String s = String.valueOf(count);
            if(s.endsWith("1")) {
                return count + " comment";
            } else {
                return count + " comments";
            }
        }
    }


    public static String getTimeAgoString(long timestamp){
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.setTimeInMillis(System.currentTimeMillis());

        Calendar timestampCalendar = Calendar.getInstance();
        timestampCalendar.setTimeInMillis(timestamp);

        return hoursBetween(nowCalendar, timestampCalendar);
    }

    public static String hoursBetween(Calendar day1, Calendar day2) {
        Calendar dayOne = (Calendar) day1.clone(),
                dayTwo = (Calendar) day2.clone();

        if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
            return Math.abs(dayOne.get(Calendar.HOUR_OF_DAY) - dayTwo.get(Calendar.HOUR_OF_DAY)) + " hours ago";
        } else {
            return Math.abs(dayOne.get(Calendar.YEAR) - dayTwo.get(Calendar.YEAR)) + " year ago";
        }
    }
}
