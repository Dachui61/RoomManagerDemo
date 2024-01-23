package com.arcsoft.arcfacedemo.yingjian.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClient {
    public static String doGet(String httpurl) {
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        String result = null;// 返回结果字符串
        try {
            // 创建远程url连接对象
            URL url = new URL(httpurl);
            // 通过远程url连接对象打开一个连接，强转成httpURLConnection类
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接方式：get
            connection.setRequestMethod("GET");
            // 设置连接主机服务器的超时时间：15000毫秒
            connection.setConnectTimeout(15000);
            // 设置读取远程返回的数据时间：60000毫秒
            connection.setReadTimeout(60000);
            // 发送请求
            connection.connect();
            // 通过connection连接，获取输入流
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                // 封装输入流is，并指定字符集
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                // 存放数据
                StringBuffer sbf = new StringBuffer();
                String temp = null;
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append("\r\n");
                }
                result = sbf.toString();
            }
        } catch (Exception e) {
            LogUtil.addError("----------Http Get请求异常-----------");
            LogUtil.addError("请求地址:" + httpurl);
            LogUtil.addError(e);
            LogUtil.addError("-------------------------------------");
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(connection!=null) {
                connection.disconnect();// 关闭远程连接
            }
        }

        return result;
    }

    public static void doPost(final String ip,final String urlPath, final String json, final HttpListener httpListener, final String fileName,final String mac) {
                // HttpClient 6.0被抛弃了
                String result = "";
                BufferedReader reader = null;
                HttpURLConnection conn = null;
                try {

                    URL url = null;
                    url = new URL(urlPath);
                    LogUtil.add("请求地址", urlPath, fileName, false);
                    LogUtil.add("请求参数", json, fileName, false);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    conn.setConnectTimeout(1000);
                    if(mac==null){
                        conn.setRequestProperty("Mesh-Node-Mac", "ffffffffffff"); //4022d8de1128
                    }else{
                        conn.setRequestProperty("Mesh-Node-Mac", mac); //4022d8de1128
                    }
                    //conn.setRequestProperty("Mesh-Node-Group", "007000000077");
                    // 设置文件类型:
                    conn.setRequestProperty("Content-Type", "application/json");
                    // 设置接收类型否则返回415错误
                    //conn.setRequestProperty("accept","*/*")此处为暴力方法设置接受所有类型，以此来防范返回415;
                    // 往服务器里面发送数据
                    if (json != null && !TextUtils.isEmpty(json)) {
                        byte[] writebytes = json.getBytes();
                        // 设置文件长度
                        conn.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
                        OutputStream outwritestream = conn.getOutputStream();
                        outwritestream.write(json.getBytes());
                        outwritestream.flush();
                        outwritestream.close();
                    }
                    LogUtil.add("响应码", "" + conn.getResponseCode(), fileName, false);
                    if (conn.getResponseCode() == 200) {
                        reader = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()));
                        String strResult="";
                        while (strResult!=null){
                            strResult = reader.readLine();
                            if(strResult!=null){
                                LogUtil.add("请求成功", result, fileName, false);
                                if (httpListener != null) {
                                    httpListener.success(ip,strResult);
                                }
                            }
                        }

                    } else {
                        if (httpListener != null) {
                            httpListener.fail();
                        }
                    }
                } catch (Exception e) {
                    if (httpListener != null) {
                        httpListener.fail();
                    }
                    LogUtil.addError("urlPath=" + urlPath);
                    LogUtil.addError("json=" + json);
                    LogUtil.addError(e);
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {}
                    }
                    if(conn!=null){
                        try{
                            conn.disconnect();
                        }catch (Exception e){}
                    }
                }

    }
}
