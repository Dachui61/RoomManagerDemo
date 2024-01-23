package com.arcsoft.arcfacedemo.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.model.Appointment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class UpcomingAppointmentsAdapter  extends ArrayAdapter<Appointment> {
    public UpcomingAppointmentsAdapter(@NonNull Context context, @NonNull List<Appointment> appointments) {
        super(context, 0, appointments);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_upcoming_appointment, parent, false);
        }

        Appointment appointment = getItem(position);

        TextView timeTextView = itemView.findViewById(R.id.timeTextView);
        TextView titleTextView = itemView.findViewById(R.id.titleTextView);
        TextView organizerTextView = itemView.findViewById(R.id.organizerTextView);

        if (appointment != null) {
            // 获取原始的起始时间和结束时间字符串
            String originalStartTime = appointment.getStartTime();
            String originalEndTime = appointment.getEndTime();

            // 解析原始的起始时间和结束时间字符串
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat newFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            try {
                Date startDate = originalFormat.parse(originalStartTime);
                Date endDate = originalFormat.parse(originalEndTime);

                // 格式化为 "HH:mm:ss"
                String formattedStartTime = newFormat.format(startDate);
                String formattedEndTime = newFormat.format(endDate);

                // 合并起始时间和结束时间为一个字符串
                String timePeriod = formattedStartTime + " - - -" + formattedEndTime;

                // 设置时间文本
                timeTextView.setText(timePeriod);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // 设置其他文本
            titleTextView.setText(" 主题：" + appointment.getTitle());
            organizerTextView.setText(" 发起人：" + appointment.getName());
        }

        // 禁用 item 点击事件
        itemView.setClickable(false);
        itemView.setFocusable(false);
        return itemView;
    }
}
