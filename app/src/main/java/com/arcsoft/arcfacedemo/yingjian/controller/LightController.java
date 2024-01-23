package com.arcsoft.arcfacedemo.yingjian.controller;

import android.content.Context;
import android.util.Log;


import com.arcsoft.arcfacedemo.activity.MyApplication;
import com.arcsoft.arcfacedemo.yingjian.entity.DevBean;
import com.arcsoft.arcfacedemo.yingjian.utils.HttpClient;
import com.arcsoft.arcfacedemo.yingjian.utils.HttpListener;
import com.arcsoft.arcfacedemo.yingjian.utils.IpUtil;

import java.util.List;

public class LightController{
    private Context context;

    public LightController(Context context) {
        this.context = context;
    }

    // 开启全部灯光
    public void openLightAll(){
        if (context != null) {
            MyApplication devBeanActivity = (MyApplication) context.getApplicationContext();
            List<DevBean> devList = devBeanActivity.getDevBeanList();
            // 处理全局的数据
            for (DevBean devBean : devList) {
                if (devBean.getTid().equals("103")){
                    String ip = devBean.getIp();
                    String json = "{\"request\": \"set_status\", \"characteristics\": ["
                            + "{\"cid\": 0, \"value\": 1},"
                            + "{\"cid\": 1, \"value\": 1},"
                            + "{\"cid\": 2, \"value\": 1}"
                            + "]}";
                    String selfMac = devBean.getSelf_mac();
                    // 说明是灯光面板控制
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // 说明是灯光面板控制
                            HttpClient.doPost(ip, "http://" + ip + "/device_request", json, new HttpListener() {
                                @Override
                                public void fail() {

                                }
                                @Override
                                public void success(String ip, String result) {
                                    try {
                                        Log.i("郑腾", "灯光已全部打开");
                                    } catch (Exception e) {

                                    }
                                }
                            }, "device_request", selfMac);
                        }
                    }).start();
                }
            }
        }

    }

    /**
     * 关闭全部灯光
     */
    public void closeLightAll(){
        if (context != null) {
            MyApplication devBeanActivity = (MyApplication) context.getApplicationContext();
            List<DevBean> devList = devBeanActivity.getDevBeanList();
            // 处理全局的数据
            for (DevBean devBean : devList) {
                if (devBean.getTid().equals("103")){
                    String ip = devBean.getIp();
                    String json = "{\"request\": \"set_status\", \"characteristics\": ["
                            + "{\"cid\": 0, \"value\": 0},"
                            + "{\"cid\": 1, \"value\": 0},"
                            + "{\"cid\": 2, \"value\": 0}"
                            + "]}";
                    String selfMac = devBean.getSelf_mac();
                    // 说明是灯光面板控制
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // 说明是灯光面板控制
                            HttpClient.doPost(ip, "http://" + ip + "/device_request", json, new HttpListener() {
                                @Override
                                public void fail() {

                                }
                                @Override
                                public void success(String ip, String result) {
                                    try {
                                        Log.i("郑腾", "灯光已全部关闭");
                                    } catch (Exception e) {

                                    }
                                }
                            }, "device_request", selfMac);
                        }
                    }).start();
                }
            }
        }

    }

    /**
     * 控制灯光1
     */
    public void controlLight1(){
        if (context != null) {
            MyApplication devBeanActivity = (MyApplication) context.getApplicationContext();
            List<DevBean> devList = devBeanActivity.getDevBeanList();
            // 处理全局的数据
            for (DevBean devBean : devList) {
                if (devBean.getTid().equals("103")){
                    String ip = devBean.getIp();
                    String json = "{\"request\": \"set_status\",\"characteristics\": [{\"cid\": "+0+",\"value\": "+2+"}]}";
                    String selfMac = devBean.getSelf_mac();
                    // 说明是灯光面板控制
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // 说明是灯光面板控制
                            HttpClient.doPost(ip, "http://" + ip + "/device_request", json, new HttpListener() {
                                @Override
                                public void fail() {

                                }
                                @Override
                                public void success(String ip, String result) {
                                    try {
                                        Log.i("郑腾", "灯光1状态已反转");
                                    } catch (Exception e) {

                                    }
                                }
                            }, "device_request", selfMac);
                        }
                    }).start();
                }
            }
        }

    }

    /**
     * 查询所有硬件设备信息并存储
     * @return
     */
    public List<DevBean> getDevAll(){
        String ipPrefix= IpUtil.getIpPrefix();
        List<DevBean> devList = IpUtil.getDevList(ipPrefix);
        return devList;
    }

}
