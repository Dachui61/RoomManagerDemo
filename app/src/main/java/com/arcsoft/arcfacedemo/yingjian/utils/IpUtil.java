package com.arcsoft.arcfacedemo.yingjian.utils;

import android.text.TextUtils;
import android.util.Log;

import com.arcsoft.arcfacedemo.yingjian.entity.DevBean;
import com.google.gson.Gson;


import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ip工具类
 */
public class IpUtil {

    /**
     * 扫描本机ip段
     * @return
     */
    public static String getIpAddressString() {
        try {
            for (Enumeration<NetworkInterface> enNetI = NetworkInterface
                    .getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
                NetworkInterface netI = enNetI.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = netI
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "0.0.0.0";
    }

    /**
     * 得到ip段前面的地址
     * @return
     */
    public static String getIpPrefix() {
        String ip=getIpAddressString();
        return ip.substring(0,ip.lastIndexOf(".")+1);
    }

    public static InetAddress getIpAddress() {
        try {
            for (Enumeration<NetworkInterface> enNetI = NetworkInterface
                    .getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
                NetworkInterface netI = enNetI.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = netI
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 检查ip是否可达
     */
    public static boolean sendPingRequest(String ipAddress) {
        try {
            if (TextUtils.isEmpty(ipAddress)) return false;
            LogUtil.add("IP地址", ipAddress, "IP网络检查", false);
            InetAddress geek = InetAddress.getByName(ipAddress);
            if (geek.isReachable(2000)) {
                LogUtil.add("结果", "ip可以正常访问", "IP网络检查", false);
                return true;
            } else {
                LogUtil.add("结果", "ip不可达", "IP网络检查", false);
                return false;
            }
        } catch (Exception e) {
            LogUtil.addError("-----------------IP网络检查异常------------------");
            LogUtil.addError("IP地址=" + ipAddress);
            LogUtil.addError(e);
            LogUtil.addError("------------------------------------------------");
        }
        return false;
    }

    public static List<DevBean> getDevList(String ipPrefix){
        List<DevBean> devBeanList = new ArrayList<>();

        for (int i=1;i<255;i++) {
            String ip = ipPrefix + i;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (IpUtil.sendPingRequest(ip)) {
                            HttpClient.doPost(ip, "http://" + ip + "/device_request", "{ \"request\": \"get_device_info\"}", new HttpListener() {
                                @Override
                                public void fail() {

                                }
                                @Override
                                public void success(String ip, String result) {
                                    try {
                                        result = result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1);
                                        Log.i("郑腾", "result->" + result);
                                        DevBean devBean = new Gson().fromJson(result, DevBean.class);
                                        devBean.setIp(ip);
                                        devBeanList.add(devBean);
                                        Log.i("郑腾", "扫描到设备->" + devBean.getName());
                                    } catch (Exception e) {
                                    }
                                }
                            }, "device_request", null);
                        }
                    }
                }).start();
            }
        return devBeanList;
    }
}
