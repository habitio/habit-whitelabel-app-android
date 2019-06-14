package com.muzzley.util;

import android.text.format.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import timber.log.Timber;

/**
 * Methods to get time from raw strings
 *
 * Created by ruigoncalo on 04/06/14.
 */
public class Time {

    public final static String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public final static String YEAR_FORMAT_PATTERN = "yyyy";
    public final static String DAY_FORMAT_PATTERN = "d MMM";

    public static String getDay(String dateStr, Locale locale) {
        String result = null;

//        SimpleDateFormat odf = new SimpleDateFormat(DAY_FORMAT_PATTERN, locale);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN, locale);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date;
//        Date now = new Date();
        try {
            date = sdf.parse(dateStr);
            result = DateFormat.getDateInstance(DateFormat.MEDIUM,locale).format(date);
//            result = odf.format(date).equals(odf.format(now)) ? "Today" : odf.format(date);
        } catch (ParseException e) {
            Timber.d(e.toString());
        }

        if (result == null) {
//            result = "Unknown";
            result = "";
        }

        return result;
    }

    public static String getYear(String raw, Locale locale){
        String result = null;
        SimpleDateFormat odf = new SimpleDateFormat(YEAR_FORMAT_PATTERN, locale);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN, locale);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date;
        try {
            date = sdf.parse(raw);
            result = odf.format(date);
        } catch (ParseException e) {
            Timber.d(e.toString());
        }

        if (result == null) {
            result = "Unknown";
        }

        return result;
    }

    public static String getTime(String dateStr, Locale locale,boolean is24hourFormat) {
        String result = null;

        SimpleDateFormat odf = new SimpleDateFormat(is24hourFormat ? "HH:mm" : "hh:mm a", locale);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN, locale);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date;
        try {
            date = sdf.parse(dateStr);
            result = odf.format(date);
        } catch (ParseException e) {
            Timber.d(e.toString());
        }

        if (result == null) {
            result = "Unknown";
        }

        return result;
    }

    public static String getLocalizedTimestamp(Date date,Locale locale,boolean is24hourFormat) {
        SimpleDateFormat odf = new SimpleDateFormat(is24hourFormat ? " HH:mm" : "hh:mm a",locale);
        return DateFormat.getDateInstance().format(date)+", " +odf.format(date);
    }

}
