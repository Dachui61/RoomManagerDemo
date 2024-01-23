package com.arcsoft.arcfacedemo.activity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.arcsoft.arcfacedemo.model.Appointment;
import com.arcsoft.arcfacedemo.model.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "your_database_name";
    private static final int DATABASE_VERSION = 1;

    // 创建用户表的 SQL 语句

    private static final String CREATE_USER_TABLE =
            "CREATE TABLE User (" +
                    "UserID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "UserName VARCHAR(20)," +
                    "StudentID VARCHAR(20)," +
                    "CardNumber VARCHAR(20)," +
                    "FaceFeature VARCHAR(255)" +
                    ")";

    // 创建预约信息表的 SQL 语句
    private static final String CREATE_APPOINTMENT_TABLE =
            "CREATE TABLE Appointment (" +
                    "AppointmentID INTEGER PRIMARY KEY," +
                    "UserID VARCHAR(255)," +
                    "Title VARCHAR(255)," +
                    "StartTime VARCHAR(255)," +
                    "EndTime VARCHAR(255)," +
                    "Location VARCHAR(255)," +
                    "Content VARCHAR(255)," +
                    "Name VARCHAR(20)," +
                    "FOREIGN KEY (UserID) REFERENCES User(UserID)" +
                    ")";

    // 创建参与者表的 SQL 语句
    private static final String CREATE_PARTICIPANT_TABLE =
            "CREATE TABLE Participant (" +
                    "ParticipantID INTEGER PRIMARY KEY," +
                    "AppointmentID INTEGER," +
                    "UserID INTEGER," +
                    "ParticipationStatus INTEGER," +
                    "FOREIGN KEY (AppointmentID) REFERENCES Appointment(AppointmentID)," +
                    "FOREIGN KEY (UserID) REFERENCES User(UserID)" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建表
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_APPOINTMENT_TABLE);
        db.execSQL(CREATE_PARTICIPANT_TABLE);
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // 在版本升级时添加新的表或修改表结构
            db.execSQL("ALTER TABLE Appointment ADD COLUMN Name VARCHAR(20)");
        }
    }

    public long insertUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
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

        SQLiteDatabase db = this.getReadableDatabase();
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
}
