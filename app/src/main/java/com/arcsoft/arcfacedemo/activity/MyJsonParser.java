package com.arcsoft.arcfacedemo.activity;

import com.arcsoft.arcfacedemo.model.Appointment;
import com.arcsoft.arcfacedemo.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonParser;


public class MyJsonParser {

    public static List<Appointment> parseAppointmentData(String jsonData) {
        List<Appointment> appointments = new ArrayList<>();

        try {
            JsonElement jsonElement = JsonParser.parseString(jsonData);
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                // 获取 "data" 字段对应的 JsonArray
                JsonArray dataArray = jsonObject.getAsJsonArray("data");

                if (dataArray != null) {
                    for (JsonElement appointmentElement : dataArray) {
                        JsonObject appointmentObject = appointmentElement.getAsJsonObject();

                        // 从 JsonObject 中获取预约信息的各个字段
                        long appointmentID = appointmentObject.getAsJsonPrimitive("appointmentID").getAsLong();
                        String userID = appointmentObject.getAsJsonPrimitive("userID").getAsString();
                        String title = appointmentObject.getAsJsonPrimitive("title").getAsString();
                        String startTime = appointmentObject.getAsJsonPrimitive("startTime").getAsString();
                        String endTime = appointmentObject.getAsJsonPrimitive("endTime").getAsString();
                        String location = appointmentObject.getAsJsonPrimitive("location").getAsString();
                        String content = appointmentObject.getAsJsonPrimitive("content").getAsString();
                        String name = appointmentObject.getAsJsonPrimitive("name").getAsString();

                        // 创建 Appointment 对象并添加到列表
                        Appointment appointment = new Appointment(appointmentID, userID, title, startTime, endTime, location, content, name);
                        appointments.add(appointment);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return appointments;
    }

    public static List<User> parseUserData(String jsonData) {
        List<User> users = new ArrayList<>();

        try {
            JsonElement jsonElement = JsonParser.parseString(jsonData);
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                // 获取 "data" 字段对应的 JsonArray
                JsonArray dataArray = jsonObject.getAsJsonArray("data");

                if (dataArray != null) {
                    for (JsonElement userElement : dataArray) {
                        JsonObject userObject = userElement.getAsJsonObject();

                        // 从 JsonObject 中获取用户信息的各个字段
                        long userID = userObject.getAsJsonPrimitive("id").getAsLong();
                        String userName = userObject.getAsJsonPrimitive("name").getAsString();
                        String studentID = userObject.getAsJsonPrimitive("userId").getAsString();
                        String cardNumber = userObject.getAsJsonPrimitive("cardFeature").getAsString();
                        String faceFeature = userObject.getAsJsonPrimitive("faceFeature").getAsString();

                        // 创建 User 对象并添加到列表
                        User user = new User(userID, userName, studentID, cardNumber, faceFeature);
                        users.add(user);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }
}
