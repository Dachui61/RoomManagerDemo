package com.arcsoft.arcfacedemo.API;

import retrofit2.Call;

import com.arcsoft.arcfacedemo.model.Appointment;
import com.arcsoft.arcfacedemo.model.Participant;
import com.arcsoft.arcfacedemo.model.User;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("users")
    Call<List<User>> getUsers();

    @GET("reservationInfo/getInfo")
    Call<List<Appointment>> getAppointments(@Query("date") String date);

    @GET("participants")
    Call<List<Participant>> getParticipants();
}
