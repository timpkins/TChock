package cn.chock.util;

import android.util.Log;

/**
 * Log日志输出
 * @author timpkins
 */
public final class LogUtils {
    private static final String LOG_PREFIX = "CHOCK";
    private static final String LOG_SEPARATOR = "_";

    /**
     * 输出级别为{@link Log#VERBOSE}的Log日志
     * @param tag Log日志的tag
     * @param msg Log日志的message
     */
    public static void v(String tag, String msg) {
        print(Log.VERBOSE, tag, msg, null);
    }

    /**
     * 输出级别为{@link Log#VERBOSE}，异常的Log日志
     * @param tag Log日志的tag
     * @param msg Log日志的message
     * @param tr Log日志的异常信息
     */
    public static void v(String tag, String msg, Throwable tr) {
        print(Log.VERBOSE, tag, msg, tr);
    }

    /**
     * 输出级别为{@link Log#DEBUG}的Log日志
     * @param tag Log日志的tag
     * @param msg Log日志的message
     */
    public static void d(String tag, String msg) {
        print(Log.DEBUG, tag, msg, null);
    }

    /**
     * 输出级别为{@link Log#DEBUG}，异常的Log日志
     * @param tag Log日志的tag
     * @param msg Log日志的message
     * @param tr Log日志的异常信息
     */
    public static void d(String tag, String msg, Throwable tr) {
        print(Log.DEBUG, tag, msg, tr);
    }

    /**
     * 输出级别为{@link Log#INFO}的Log日志
     * @param tag Log日志的tag
     * @param msg Log日志的message
     */
    public static void i(String tag, String msg) {
        print(Log.INFO, tag, msg, null);
    }

    /**
     * 输出级别为{@link Log#INFO}，异常的Log日志
     * @param tag Log日志的tag
     * @param msg Log日志的message
     * @param tr Log日志的异常信息
     */
    public static void i(String tag, String msg, Throwable tr) {
        print(Log.INFO, tag, msg, tr);
    }

    /**
     * 输出级别为{@link Log#WARN}的Log日志
     * @param tag Log日志的tag
     * @param msg Log日志的message
     */
    public static void w(String tag, String msg) {
        print(Log.WARN, tag, msg, null);
    }

    /**
     * 输出级别为{@link Log#WARN}，异常的Log日志
     * @param tag Log日志的tag
     * @param msg Log日志的message
     * @param tr Log日志的异常信息
     */
    public static void w(String tag, String msg, Throwable tr) {
        print(Log.WARN, tag, msg, tr);
    }

    /**
     * 输出级别为{@link Log#ERROR}的Log日志
     * @param tag Log日志的tag
     * @param msg Log日志的message
     */
    public static void e(String tag, String msg) {
        print(Log.ERROR, tag, msg, null);
    }

    /**
     * 输出级别为{@link Log#ERROR}，异常的Log日志
     * @param tag Log日志的tag
     * @param msg Log日志的message
     * @param tr Log日志的异常信息
     */
    public static void e(String tag, String msg, Throwable tr) {
        print(Log.ERROR, tag, msg, tr);
    }

    private static void print(int priority, String tag, String msg, Throwable tr) {
        System.out.println(msg);
        if (tr != null) {
            msg = msg.concat("\n").concat(Log.getStackTraceString(tr));
        }
        tag = LOG_PREFIX.concat(LOG_SEPARATOR).concat(tag);
        Log.println(priority, tag, msg);
    }
}
