package com.arcsoft.arcfacedemo.model;

public class User {
    private long userID;
    private String userName;
    private String studentID;
    private String cardNumber;
    private String faceFeature;

    public  User(){}
    // 构造函数
    public User(long userID, String userName, String studentID, String cardNumber, String faceFeature) {
        this.userID = userID;
        this.userName = userName;
        this.studentID = studentID;
        this.cardNumber = cardNumber;
        this.faceFeature = faceFeature;
    }

    // Getter 和 Setter 方法
    public long getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    public String getStudentID() {
        return studentID;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getFaceFeature() {
        return faceFeature;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setFaceFeature(String faceFeature) {
        this.faceFeature = faceFeature;
    }
}
