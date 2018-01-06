package com.mmall.util;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * Created by huxiaosa on 2017/12/26.
 */
public class DateTimeUtil {
    //joda-time
    public static final String FORMATSTR = "yyyy-MM-dd HH:mm:ss";
    public static Date strToDate(String dateTimeStr){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(FORMATSTR);
        DateTime dateTime =dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }
    public static String DateToStr(Date date){
        if(date==null){
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(FORMATSTR);
    }

    public static Date strToDate(String dateTimeStr,String formatStr){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(formatStr);
        DateTime dateTime =dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    public static String DateToStr(Date date,String formatStr){
       if(date==null){
          return StringUtils.EMPTY;
       }
       DateTime dateTime = new DateTime(date);
       return dateTime.toString(formatStr);
    }
}
