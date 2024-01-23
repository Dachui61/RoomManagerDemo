package com.arcsoft.arcfacedemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import retrofit2.Call;
import android.util.Log;

import com.arcsoft.arcfacedemo.API.ApiService;
import com.arcsoft.arcfacedemo.model.Appointment;
import com.arcsoft.arcfacedemo.model.Participant;
import com.arcsoft.arcfacedemo.model.User;
import com.arcsoft.arcfacedemo.util.RetrofitClient;

import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // 在这里执行从服务器获取数据的操作
        fetchDataFromServer(context);

        // 更新本地数据库
//        updateLocalDatabase(context);

        // 更新班牌界面等操作
        updateClassBoardUI(context);
    }

    private void fetchDataFromServer(Context context) {
        ApiService apiService = RetrofitClient.getApiService(); // RetrofitClient 是一个 Retrofit 实例的管理类

        // 获取用户数据
        Call<List<User>> userCall = apiService.getUsers();
        userCall.enqueue(new Callback<List<User>>() {
            //            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> userList = response.body();
                    // 在这里处理从服务器获取的用户数据，可以将其保存到本地数据库
                    updateLocalUserDatabase(context, userList);
                } else {
                    Log.e("ApiService", "Failed to fetch users: " + response.message());
                }
            }

            //            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e("ApiService", "Failed to fetch users: " + t.getMessage());
            }
        });

        // 获取预约数据
        Call<List<Appointment>> appointmentCall = apiService.getAppointments("2023-12-04");
        appointmentCall.enqueue(new Callback<List<Appointment>>() {
            @Override
            public void onResponse(Call<List<Appointment>> call, Response<List<Appointment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Appointment> appointmentList = response.body();
                    // 在这里处理从服务器获取的预约数据，可以将其保存到本地数据库
                    updateLocalAppointmentDatabase(context, appointmentList);
                } else {
                    Log.e("ApiService", "Failed to fetch appointments: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Appointment>> call, Throwable t) {
                Log.e("ApiService", "Failed to fetch appointments: " + t.getMessage());
            }
        });

        // 获取参与者数据
        Call<List<Participant>> participantCall = apiService.getParticipants();
        participantCall.enqueue(new Callback<List<Participant>>() {
            @Override
            public void onResponse(Call<List<Participant>> call, Response<List<Participant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Participant> participantList = response.body();
                    // 在这里处理从服务器获取的参与者数据，可以将其保存到本地数据库
                    updateLocalParticipantDatabase(context, participantList);
                } else {
                    Log.e("ApiService", "Failed to fetch participants: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Participant>> call, Throwable t) {
                Log.e("ApiService", "Failed to fetch participants: " + t.getMessage());
            }
        });
    }

    private void updateLocalUserDatabase(Context context, List<User> userList) {
        // 在这里实现更新本地用户数据库的逻辑
    }

    private void updateLocalAppointmentDatabase(Context context, List<Appointment> appointmentList) {
        // 在这里实现更新本地预约数据库的逻辑
    }

    private void updateLocalParticipantDatabase(Context context, List<Participant> participantList) {
        // 在这里实现更新本地参与者数据库的逻辑
    }


    private void updateLocalDatabase(Context context) {
        // 执行更新本地数据库的逻辑，可以使用 SQLite 或 Room 等数据库框架
        // ...
    }

    private void updateClassBoardUI(Context context) {
        // 执行更新班牌界面的逻辑，例如发送广播通知 UI 更新
        Intent updateUIIntent = new Intent("com.example.ACTION_UPDATE_UI");
        context.sendBroadcast(updateUIIntent);
    }
}
