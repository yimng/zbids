package com.caicui.commons.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

/**
 * 日期操作的工具类
 */
public class DateUtil {

    /**
     * 根据date,判断和今天是否否同一天
     *
     * @param date Date
     * @return Boolean
     */
    public static boolean isSameDay(Date date) {
        return DateUtils.isSameDay(date, new Date());
    }

    /**
     * 得到今年的年份
     *
     * @return Integer
     */
    public static Integer getYear() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        return year;
    }


    /**
     * 获得当年月 Description:
     *
     * @return
     * @Version1.0 2014年12月24日 下午8:57:56 by 于科为 创建
     */
    public static String getMonth() {
        Integer month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        String monthStr = "";
        if (month < 10) {
            monthStr = "0" + month;
        } else {
            monthStr = String.valueOf(month);
        }
        return monthStr;
    }

    /**
     * 获得当前天 Description:
     *
     * @return
     * @Version1.0 2014年12月24日 下午8:58:07 by 于科为 创建
     */
    public static String getDay() {
        Integer day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        String dayStr = "";
        if (day < 10) {
            dayStr = "0" + day;
        } else {
            dayStr = String.valueOf(day);
        }
        return dayStr;
    }

    /**
     * 和现在比相差多少天
     *
     * @param date Date
     * @return Integer
     */
    public static Integer getDistanceDays(Date date) {
        return (int) (new Date().getTime() - date.getTime()) / 86400000;
    }

    public static String getFormatDate(Date date, String Format) {
        SimpleDateFormat df = new SimpleDateFormat(Format);
        return df.format(date);
    }

    /**
     * @param date Calendar
     * @param fild Calendar.MONDAY~Calendar.SUNDAY
     * @return
     * @Title: getWeekDay
     * @Description: 根据给定的日期获一周内指定的日期
     */
    public static Date getWeekDay(Date date, int fild) {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.setFirstDayOfWeek(Calendar.MONDAY); // 以周1为首日
        // 当前时间，貌似多余，其实是为了所有可能的系统一致
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, fild);
        return calendar.getTime();
    }

    /**
     * @param date   时间字符串
     * @param format 格式化字符串
     * @return
     * @throws java.text.ParseException
     * @Title: strToDate
     * @Description: 字符串转换成时间类型
     */
    public static Date strToDate(String date, String format)
            throws java.text.ParseException {
        if (StringUtils.isEmpty(format)) {
            format = "yyyy-MM-dd";
        }
        SimpleDateFormat formatDate = new SimpleDateFormat(format);
        return formatDate.parse(date);
    }

    /**
     * @param date
     * @param format
     * @return
     * @throws java.text.ParseException
     * @Title: dateToString
     * @Description:转换时间格式
     */
    public static String dateToString(Date date, String format)
            throws java.text.ParseException {
        if (StringUtils.isEmpty(format)) {
            format = "yyyy-MM-dd";
        }
        SimpleDateFormat formatDate = new SimpleDateFormat(format);
        return formatDate.format(date);
    }

}
