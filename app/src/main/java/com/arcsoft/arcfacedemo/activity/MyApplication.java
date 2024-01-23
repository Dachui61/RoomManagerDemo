package com.arcsoft.arcfacedemo.activity;

import android.app.Application;


import com.arcsoft.arcfacedemo.yingjian.controller.LightController;
import com.arcsoft.arcfacedemo.yingjian.entity.DevBean;

import java.util.List;

public class MyApplication extends Application {
    private List<DevBean> devBeanList;

    //判断人脸识别要进行的功能状态
    private int isMeetingMode = 0;

    //判断人脸识别要进行的功能状态
    private boolean meetingStarted = false;
    //人脸注册的状态
    private boolean RegisterNum = false;

    public boolean isRegisterNum() {
        return RegisterNum;
    }

    public boolean isMeetingStarted() {
        return meetingStarted;
    }

    public void setMeetingStarted(boolean IsMeetingStarted)
    {
        meetingStarted = IsMeetingStarted;
    }

    public void setRegisterNum(boolean RegisterNumber)
    {
        RegisterNum = RegisterNumber;
    }

    public int isMeetingMode() {
        return isMeetingMode;
    }

    public void setMeetingMode(int meetingMode) {
        isMeetingMode = meetingMode;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        // 在这里执行应用初始化操作
        //AppInitializer.init(this);
        AppInitializer.initialize(this);

        // 在应用启动时初始化数据
        initializeData();

    }

    private void initializeData() {
        // 数据初始化
        devBeanList = new LightController(this).getDevAll();
    }

    public List<DevBean> getDevBeanList() {
        return devBeanList;
    }

}
