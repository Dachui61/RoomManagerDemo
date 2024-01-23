package com.arcsoft.arcfacedemo.model;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.util.Date;

public class Appointment {
    private long appointmentID;
    private String userID;
    private String title;
    private String startTime;
    private String endTime;
    private String location;
    private String content;
    private String name;

    public Appointment(){}
    // 构造函数
    public Appointment(long appointmentID, String userID, String title, String startTime,
                       String endTime, String location, String content , String name) {
        this.appointmentID = appointmentID;
        this.userID = userID;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.content = content;
        this.name = name;
    }

    // Getter 和 Setter 方法
    public long getAppointmentID() {
        return appointmentID;
    }

    public String getUserID() {
        return userID;
    }

    public String getTitle() {
        return title;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getLocation() {
        return location;
    }

    public String getContent() {
        return content;
    }

    public String getName() {
        return name;
    }

    public void setAppointmentID(long appointmentID) {
        this.appointmentID = appointmentID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setName(String name) {
        this.name = name;
    }
}
