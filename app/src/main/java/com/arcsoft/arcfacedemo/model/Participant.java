package com.arcsoft.arcfacedemo.model;

public class Participant {
    private long participantID;
    private long appointmentID;
    private long userID;
    private int participationStatus;

    // 构造函数
    public Participant(long participantID, long appointmentID, long userID, int participationStatus) {
        this.participantID = participantID;
        this.appointmentID = appointmentID;
        this.userID = userID;
        this.participationStatus = participationStatus;
    }

    // Getter 和 Setter 方法
    public long getParticipantID() {
        return participantID;
    }

    public long getAppointmentID() {
        return appointmentID;
    }

    public long getUserID() {
        return userID;
    }

    public int getParticipationStatus() {
        return participationStatus;
    }
}
