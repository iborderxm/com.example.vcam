package com.example.vcam;

import android.content.Context;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Created by itgoyo on 2017/2/23.
 * <p>
 * 更新时间 2017/2/23
 * 更新描述 ${TODO}
 */

public class LogToFileUtils {
    /**
     * 上下文对象
     */
    private static Context      mContext;
    /**
     * FileLogUtils类的实例
     */
    private static LogToFileUtils instance;
    /**
     * 用于保存日志的文件
     */
    private static File         logFile;
    private static BufferedWriter bw;
    /**
     * 日志中的时间显示格式
     */
    private static       SimpleDateFormat logSDF       = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 日志的最大占用空间 - 单位：字节
     * <p>
     * 注意：为了性能，没有每次写入日志时判断，故日志在写入第二次初始化之前，不会受此变量限制，所以，请注意日志工具类的初始化时间
     * <p>
     * 为了衔接上文，日志超出设定大小后不会被直接删除，而是存储一个副本，所以实际占用空间是两份日志大小
     * <p>
     * 除了第一次超出大小后存为副本外，第二次及以后再次超出大小，则会覆盖副本文件，所以日志文件最多也只有两份
     * <p>
     * 默认10M
     */
    private static final int              LOG_MAX_SIZE = 10 * 1024 * 1024;
    /**
     * 以调用者的类名作为TAG
     */
    private static String tag;

    private static final String MY_TAG = "VCAMLOG";

    public static boolean islog = true;

    /**
     * 初始化日志库
     *
     * @param context
     */
    public static void init(Context context) {
        if (!islog){
            return;
        }

        //Log.i(MY_TAG, "init ...");
        System.out.println("[VCAMLOG]LogToFileUtils init ...");
        if (null == instance || null == logFile || !logFile.exists()) {
            mContext = context;
            instance = new LogToFileUtils();
            logFile = getLogFile();
            System.out.println("[VCAMLOG]LogFilePath is: " + logFile.getPath());
            // 获取当前日志文件大小
            long logFileSize = getFileSize(logFile);
            // 若日志文件超出了预设大小，则重置日志文件
            if (LOG_MAX_SIZE < logFileSize) {
                resetLogFile();
            }
            try {
                if (bw == null){
                    bw = new BufferedWriter(new FileWriter(logFile, true));
                }
                bw.write("==========================================================================\n\n");
                bw.write("==============================VCAM开始记录日志==============================\n\n");
                bw.write("==========================================================================\n\n");
                bw.flush();
            } catch (Exception e){
                System.out.println("[VCAMLOG]open log file fail.");
            }
        } else {
            System.out.println("[VCAMLOG]LogToFileUtils has been init ...");
        }
    }

    /**
     * 写入日志文件的数据
     *
     * @param str 需要写入的数据
     */
    public static void write(Object str) {
        // 判断是否初始化或者初始化是否成功
        if (null == instance || null == logFile || !logFile.exists() || bw == null) {
            //Log.e(MY_TAG, "Initialization failure !!!");
            System.out.println("[VCAMLOG]Initialization failure !!!");
            return;
        }
        //String logStr = getFunctionInfo() + " - " + str.toString();
        String logStr = str.toString();
        //Log.i(tag, logStr);
        System.out.println(String.format("[VCAMLOG]%s", logStr));

        try {
            //BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true));
            bw.write(logStr);
            bw.write("\r\n");
            bw.flush();
        } catch (Exception e) {
            System.out.println(String.format("[VCAMLOG]Write failure !!! :%s", e.toString()));
            //Log.e(tag, "Write failure !!! " + e.toString());
        } finally {
            if (bw == null){
                try {
                    bw.close();
                } catch (IOException e) {
                    System.out.println(String.format("[VCAMLOG]close bw failure !!! :%s", e.toString()));
                }
            }
        }
    }

    /**
     * 重置日志文件
     * <p>
     * 若日志文件超过一定大小，则把日志改名为lastLog.txt，然后新日志继续写入日志文件
     * <p>
     * 每次仅保存一个上一份日志，日志文件最多有两份
     * <p/>
     */
    private static void resetLogFile() {
        Log.i(MY_TAG, "Reset Log File ... ");
        // 创建lastLog.txt，若存在则删除
        File lastLogFile = new File(logFile.getParent() + "/lastLog.txt");
        if (lastLogFile.exists()) {
            lastLogFile.delete();
        }
        // 将日志文件重命名为 lastLog.txt
        logFile.renameTo(lastLogFile);
        // 新建日志文件
        try {
            logFile.createNewFile();
        } catch (Exception e) {
            Log.e(MY_TAG, "Create log file failure !!! " + e.toString());
        }
    }

    /**
     * 获取文件大小
     *
     * @param file 文件
     * @return
     */
    private static long getFileSize(File file) {
        long size = 0;
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                size = fis.available();
            } catch (Exception e) {
                Log.e(MY_TAG, e.toString());
            }
        }
        return size;
    }

    /**
     * 获取APP日志文件
     *
     * @return APP日志文件
     */
    private static File getLogFile() {
        File file;
        // 判断是否有SD卡或者外部存储器
        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/log/");
        // 若目录不存在则创建目录
        if (!file.exists()) {
            file.mkdir();
        }
        File logFile = new File(file.getPath() + "/logs.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (Exception e) {
                System.out.println("[VCAMLOG]Create log file failure !!! " + e.toString());
            }
        }
        return logFile;
    }

    /**
     * 获取当前函数的信息
     *
     * @return 当前函数的信息
     */
    private static String getFunctionInfo() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(instance.getClass().getName())) {
                continue;
            }
            tag = st.getFileName();
            return "[" + logSDF.format(new java.util.Date()) + " " + st.getClassName() + " " + st
                    .getMethodName() + " Line:" + st.getLineNumber() + "]";
        }
        return null;
    }
}