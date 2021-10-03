package com.edgedx.connectpoc.utils;

import lombok.extern.java.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;

@Log
public class DateUtils {

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");

    public static SimpleDateFormat dateFormatNodeHealth = new SimpleDateFormat("MMMM d, yyyy HH:mm");

    public static DateTimeFormatter MGIT_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static DateTimeFormatter MGIT_FORMAT_2 = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    public static DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss.S");

    public static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public static String getFullDateTimezone(Date date) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());

        try {
            return sdf.format(new Date());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.sql.Timestamp getCurrentTimeStamp() {

        Date today = new Date();
        return new java.sql.Timestamp(today.getTime());

    }

    public static LocalDateTime parseGeneXpert(String dataString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
        try {
            return LocalDateTime.parse(dataString, formatter);
        } catch (DateTimeParseException e) {
            return parseDateTime(dataString);
        }
    }

    public static LocalDateTime parseGeneXpert2(String dataString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss");
        try {
            return LocalDateTime.parse(dataString, formatter);
        } catch (DateTimeParseException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }

    }

    public static LocalDateTime parseGeneXpertTime(String dataString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");

        try {
            return LocalDateTime.parse(dataString, formatter);
        } catch (DateTimeParseException e) {
            return parseDateTime(dataString);
        }
    }

    public static LocalDateTime parseGeneXpertTime2(String dataString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/dd/MM HH:mm:ss.SSS");
        try {
            return LocalDateTime.parse(dataString, formatter);
        } catch (DateTimeParseException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }


    public static LocalDateTime parseDateTime(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");
        try {
            return LocalDateTime.parse(dateString, formatter);
        } catch (DateTimeParseException e) {
            try {
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
                return LocalDateTime.parse(dateString, formatter);
            } catch (DateTimeParseException e2) {
                log.log(Level.SEVERE, e2.getMessage(), e);
                return null;
            }
        }
    }

    public static LocalDate parseGeneXpertShort(String dataString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
        try {
            return LocalDate.parse(dataString, formatter);
        } catch (DateTimeParseException e) {
            LocalDateTime time = parseGeneXpert(dataString);
            if (time != null)
                return time.toLocalDate();
            else
                return null;
        }
    }

    public static LocalDateTime parseTemperatureDate(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss");
        try {
            return LocalDateTime.parse(dateString, formatter);
        } catch (DateTimeParseException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }

    public static LocalDate parseGeneXpertShort2(String dataString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
        try {
            return LocalDate.parse(dataString, formatter);
        } catch (DateTimeParseException e) {
            LocalDateTime time = parseGeneXpert(dataString);
            if (time != null)
                return time.toLocalDate();
            return null;
        }

    }


    public static String formatDateString(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        return format.format(dateString);
    }

    public static String formatDateCalendar(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

        return format.format(date);
    }

    public static LocalDate parseDate(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        try {
            return LocalDate.parse(dateString, formatter);
        } catch (DateTimeParseException e) {

        }
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            return LocalDate.parse(dateString, formatter);
        } catch (DateTimeParseException e) {

        }
        formatter = DateTimeFormatter.ofPattern("MM-dd-yy");


        try {
            return LocalDate.parse(dateString, formatter);
        } catch (DateTimeParseException e) {

        }

        return null;
    }

}
