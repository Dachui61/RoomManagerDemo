package com.arcsoft.arcfacedemo.yingjian.controller;

import android.content.Context;
import android.util.Log;


import com.arcsoft.arcfacedemo.activity.MyApplication;
import com.arcsoft.arcfacedemo.yingjian.entity.CidBean;
import com.arcsoft.arcfacedemo.yingjian.entity.DevBean;
import com.arcsoft.arcfacedemo.yingjian.utils.HttpClient;
import com.arcsoft.arcfacedemo.yingjian.utils.HttpListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SensorController {
    private Context context;

    public SensorController(Context context) {
        this.context = context;
    }

    /**
     * 获取传感器温度，湿度，亮度， 32 33 34 cid值
     */
    public List<CidBean> getParameter(){
        List<CidBean> cidBeans = new ArrayList<>();
        if (context != null) {
            MyApplication devBeanActivity = (MyApplication) context.getApplicationContext();
            List<DevBean> devList = devBeanActivity.getDevBeanList();
            // 处理数据
            for (DevBean devBean : devList) {
                if (devBean.getTid().equals("18")){
                    // 说明是传感器
                    String ip = devBean.getIp();
                    String json = "{\"request\": \"get_status\", \"cids\": [32, 33, 34]}";
                    String selfMac = devBean.getSelf_mac();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // 说明是传感器控制
                            HttpClient.doPost(ip, "http://" + ip + "/device_request", json, new HttpListener() {
                                @Override
                                public void fail() {

                                }
                                @Override
                                public void success(String ip, String result) {
                                    try {
                                        System.out.println("我是result" + result);
                                        JSONObject jsonResult = new JSONObject(result);
                                        JSONArray characteristics = jsonResult.getJSONArray("characteristics");
                                        System.out.println("握手"+ characteristics);
                                        for (int i = 0; i < characteristics.length(); i++) {
                                            JSONObject item = characteristics.getJSONObject(i);
                                            CidBean cidBean = new CidBean();
                                            String cid = String.valueOf(item.getInt("cid"));
                                            String value = String.valueOf(item.getDouble("value"));
                                            cidBean.setCid(cid);
                                            cidBean.setValue(value);
                                            cidBeans.add(cidBean);
                                        }
                                        Log.i("郑腾", "获取到传感器参数");
                                    } catch (Exception e) {

                                    }
                                }
                            }, "device_request", selfMac);
                        }
                    }).start();
                }
            }
        }
        try {
            Thread.sleep(1000); // 5000毫秒 = 5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return cidBeans;
    }
}
