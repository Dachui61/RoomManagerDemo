package com.arcsoft.arcfacedemo.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.arcsoft.arcfacedemo.activity.DatabaseHelper;
import com.arcsoft.arcfacedemo.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private DatabaseHelper databaseHelper;

    public UserDAO(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public long insertUser(User user) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("UserName", user.getUserName());
        values.put("StudentID", user.getStudentID());
        values.put("CardNumber", user.getCardNumber());
        values.put("FaceFeature", user.getFaceFeature());

        // 插入数据，返回插入的行号
        return db.insert("User", null, values);
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM User", null);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    long userID = cursor.getLong(cursor.getColumnIndex("UserID"));
                    String userName = cursor.getString(cursor.getColumnIndex("UserName"));
                    String studentID = cursor.getString(cursor.getColumnIndex("StudentID"));
                    String cardNumber = cursor.getString(cursor.getColumnIndex("CardNumber"));
                    String faceFeature = cursor.getString(cursor.getColumnIndex("FaceFeature"));

                    User user = new User(userID, userName, studentID, cardNumber, faceFeature);
                    userList.add(user);
                }
            } finally {
                cursor.close();
            }
        }

        return userList;
    }

    public int getUserCount() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM User", null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getInt(0); // 获取第一列的值，即 COUNT(*) 的结果
                } else {
                    Log.e("getUserCount", "Cursor is empty");
                }
            } finally {
                cursor.close();
            }
        } else {
            Log.e("getUserCount", "Cursor is null");
        }

        return 0; // 或者返回其他默认值
    }

    public void deleteAllUsers() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete("User", null, null);
        db.close();
    }
}
