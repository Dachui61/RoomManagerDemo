package com.arcsoft.arcfacedemo.yingjian.utils;

public interface HttpListener {
    void fail();

    void success(String ip,String result);
}
