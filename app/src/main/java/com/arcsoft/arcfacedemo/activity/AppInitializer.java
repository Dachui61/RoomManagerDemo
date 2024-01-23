package com.arcsoft.arcfacedemo.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.arcsoft.arcfacedemo.receiver.AlarmReceiver;

import java.util.Calendar;

public class AppInitializer {
    public static void init(Context context) {
        scheduleDataUpdate(context);
        // 可以添加其他初始化逻辑
    }

    public static void initialize(Context context) {
        // 初始化数据库
        initializeDatabase(context);

    }

    private static void scheduleDataUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    private static void initializeDatabase(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        // 检查数据库是否已存在
        if (!databaseExists(dbHelper)) {
            // 如果数据库不存在，则创建和初始化
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            // 关闭数据库连接
            db.close();
        }
    }

    private static boolean databaseExists(DatabaseHelper dbHelper) {
        SQLiteDatabase checkDB = null;
        try {
            // 尝试打开数据库
            checkDB = SQLiteDatabase.openDatabase(dbHelper.getDatabaseName(), null, SQLiteDatabase.OPEN_READONLY);
            // 关闭数据库连接
            if (checkDB != null) {
                checkDB.close();
            }
        } catch (SQLiteException e) {
            // 数据库不存在
        }
        return checkDB != null;
    }
}
