package com.arcsoft.arcfacedemo.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.arcsoft.arcfacedemo.activity.DatabaseHelper;
import com.arcsoft.arcfacedemo.model.Appointment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppointmentDAO {
    private DatabaseHelper databaseHelper;

    public AppointmentDAO(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public long insertAppointment(Appointment appointment) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("UserID", appointment.getUserID());
        values.put("Title", appointment.getTitle());
        values.put("StartTime", appointment.getStartTime());
        values.put("EndTime", appointment.getEndTime());
        values.put("Location", appointment.getLocation());
        values.put("Content", appointment.getContent());
        values.put("Name", appointment.getName());
        return db.insert("Appointment", null, values);
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> allAppointments = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Appointment", null);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    long appointmentID = cursor.getLong(cursor.getColumnIndex("AppointmentID"));
                    String userID = cursor.getString(cursor.getColumnIndex("UserID"));
                    String title = cursor.getString(cursor.getColumnIndex("Title"));
                    String startTime = cursor.getString(cursor.getColumnIndex("StartTime"));
                    String endTime = cursor.getString(cursor.getColumnIndex("EndTime"));
                    String location = cursor.getString(cursor.getColumnIndex("Location"));
                    String content = cursor.getString(cursor.getColumnIndex("Content"));
                    String name = cursor.getString(cursor.getColumnIndex("Name"));

                    Appointment appointment = new Appointment(appointmentID, userID, title, startTime, endTime, location, content, name);
                    allAppointments.add(appointment);
                }
            } finally {
                cursor.close();
            }
        }

        return allAppointments;
    }

    public Appointment getLatestReservation() {
        // 获取当前时间的毫秒表示
        long currentTimeMillis = System.currentTimeMillis();

        // 创建一个 SimpleDateFormat 对象，指定日期时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 将 currentTimeMillis 转换为日期时间字符串
        String formattedDateTime = sdf.format(new Date(currentTimeMillis));
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Appointment WHERE StartTime > ? ORDER BY StartTime ASC LIMIT 1", new String[]{formattedDateTime});

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    long appointmentID = cursor.getLong(cursor.getColumnIndex("AppointmentID"));
                    String userID = cursor.getString(cursor.getColumnIndex("UserID"));
                    String title = cursor.getString(cursor.getColumnIndex("Title"));
                    String startTime = cursor.getString(cursor.getColumnIndex("StartTime"));
                    String endTime = cursor.getString(cursor.getColumnIndex("EndTime"));
                    String location = cursor.getString(cursor.getColumnIndex("Location"));
                    String content = cursor.getString(cursor.getColumnIndex("Content"));
                    String name = cursor.getString(cursor.getColumnIndex("Name"));

                    return new Appointment(appointmentID, userID, title, startTime, endTime, location, content, name);
                } else {
                    Log.e("getLatestReservation", "Cursor is empty");
                }
            } finally {
                cursor.close();
            }
        } else {
            Log.e("getLatestReservation", "Cursor is null");
        }

        return null;
    }
    public List<Appointment> getOtherAppointments(long latestReservationID) {
        List<Appointment> otherAppointments = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String query = "SELECT * FROM Appointment WHERE " + "AppointmentID != ? AND " + "StartTime > (SELECT StartTime FROM Appointment WHERE AppointmentID = ?) " + "ORDER BY StartTime ASC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(latestReservationID), String.valueOf(latestReservationID)});
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    long appointmentID = cursor.getLong(cursor.getColumnIndex("AppointmentID"));
                    String userID = cursor.getString(cursor.getColumnIndex("UserID"));
                    String title = cursor.getString(cursor.getColumnIndex("Title"));
                    String startTime = cursor.getString(cursor.getColumnIndex("StartTime"));
                    String endTime = cursor.getString(cursor.getColumnIndex("EndTime"));
                    String location = cursor.getString(cursor.getColumnIndex("Location"));
                    String content = cursor.getString(cursor.getColumnIndex("Content"));
                    String name = cursor.getString(cursor.getColumnIndex("Name"));

                    Appointment appointment = new Appointment(appointmentID, userID, title, startTime, endTime, location, content, name);
                    otherAppointments.add(appointment);
                }
            } finally {
                cursor.close();
            }
        }

        return otherAppointments;
    }

    public int getAppointmentCount() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM Appointment", null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getInt(0); // 获取第一列的值，即 COUNT(*) 的结果
                } else {
                    Log.e("getAppointmentCount", "Cursor is empty");
                }
            } finally {
                cursor.close();
            }
        } else {
            Log.e("getAppointmentCount", "Cursor is null");
        }

        return 0; // 或者返回其他默认值
    }

    public void deleteAllAppointments() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete("Appointment", null, null);
        db.close();
    }

    public Appointment getNextAppointment() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Appointment appointment = getLatestReservation();
        // 查询数据库以获取当前时间之后的下一个预约
        String query = "SELECT * FROM Appointment WHERE StartTime > ? ORDER BY StartTime ASC LIMIT 1";

        Cursor cursor = db.rawQuery(query, new String[]{appointment.getEndTime()});

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    long appointmentID = cursor.getLong(cursor.getColumnIndex("AppointmentID"));
                    String userID = cursor.getString(cursor.getColumnIndex("UserID"));
                    String title = cursor.getString(cursor.getColumnIndex("Title"));
                    String startTime = cursor.getString(cursor.getColumnIndex("StartTime"));
                    String endTime = cursor.getString(cursor.getColumnIndex("EndTime"));
                    String location = cursor.getString(cursor.getColumnIndex("Location"));
                    String content = cursor.getString(cursor.getColumnIndex("Content"));
                    String name = cursor.getString(cursor.getColumnIndex("Name"));

                    return new Appointment(appointmentID, userID, title, startTime, endTime, location, content, name);
                } else {
                    Log.d("getNextAppointment", "没有即将到来的预约");
                }
            } finally {
                cursor.close();
            }
        } else {
            Log.e("getNextAppointment", "Cursor 为 null");
        }

        return null;
    }
}
