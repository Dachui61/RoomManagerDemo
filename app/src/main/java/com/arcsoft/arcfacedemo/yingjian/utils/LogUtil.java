package com.arcsoft.arcfacedemo.yingjian.utils;

import android.content.Context;
import android.util.Log;


import com.arcsoft.arcfacedemo.yingjian.entity.TagBean;

import java.text.SimpleDateFormat;


/**
 * 日志工具类
 */
public class LogUtil {
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
    private static LogUtil instance;

    private LogUtil() {
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new LogUtil();
        }
    }

    public static LogUtil getInstance() {
        return instance;
    }
    /**
     * 往队列中添加日志数据
     *
     * @param bean 日志数据
     */
    private static void add(TagBean bean) {
        Log.i(bean.getFileName() + " " + bean.getTag(), bean.getContent());
    }

    /**
     * 往队列中添加日志数据
     *
     * @param tag         标记
     * @param content     内容
     * @param fileName    要写入到哪个文件中,当为null时不写入到文件
     * @param writeToFile 是否写入到文件中
     */
    public static void add(String tag, String content, String fileName, boolean writeToFile) {
        add(new TagBean(tag, content, fileName, writeToFile));
    }

    /**
     * 往队列中添加日志数据
     *
     * @param tag      标记
     * @param content  内容
     * @param fileName 要写入到哪个文件中
     */
    public static void add(String tag, String content, String fileName) {
        add(new TagBean(tag, content, fileName));
    }

    private static Throwable getLastCause(Throwable e) {
        if (e != null && e.getCause() != null) {
            return getLastCause(e.getCause());
        }
        return e;
    }

    /**
     * 记录一条错误日志
     *
     * @param t 线程对象
     * @param e 错误对象
     */
    public static void addError(Thread t, Throwable e) {
        Throwable cause = getLastCause(e);
        add("luohao", "ThreadName:" + t.getName(), "错误");
        add("luohao", cause.toString(), "错误");
        StackTraceElement[] seCause = cause.getStackTrace();
        for (int i = 0; i < seCause.length; i++) {
            add("luohao", "    at " + seCause[i].getClassName() + "." + seCause[i].getMethodName() + "(" + seCause[i].getFileName() + ":" + seCause[i].getLineNumber() + ")", "错误");
        }
        add("luohao", "-------------------------------------------------------------", "错误");
    }

    /**
     * 记录一条错误日志
     *
     * @param e 错误对象
     */
    public static void addError(Throwable e) {
        addError(Thread.currentThread(), e);
    }

    /**
     * 记录一条错误日志
     *
     * @param text 要记录的文本内容
     */
    public static void addError(String text) {
        add("luohao", text, "错误");
    }
}
