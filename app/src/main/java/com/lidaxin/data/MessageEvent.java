package com.lidaxin.data;

public class MessageEvent {
    public static final String SAVELOG = "savelog";
    public static final String STARTLOG = "startlog";
    public static final String STOPLOG = "stoplog";

    private String action;
    private String message;

    public  MessageEvent(String action, String message){
        this.action = action;
        this.message=message;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
