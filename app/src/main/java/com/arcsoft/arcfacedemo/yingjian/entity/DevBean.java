package com.arcsoft.arcfacedemo.yingjian.entity;

import java.util.List;

public class DevBean {
    private String ip;
    private String position;
    private String mfrs;
    private String tid;
    private String name;
    private String self_mac;
    private String mesh_id;
    private String version;
    private String idf_version;
    private String mdf_version;
    private String mlink_version;
    private String mlink_trigger;
    private String mlink_scenes;
    private String eth_ip;
    private String ssid;
    private String password;
    private String channel;
    private String status_msg;
    private String status_code;
    private List<CidBean> characteristics;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getMfrs() {
        return mfrs;
    }

    public void setMfrs(String mfrs) {
        this.mfrs = mfrs;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSelf_mac() {
        return self_mac;
    }

    public void setSelf_mac(String self_mac) {
        this.self_mac = self_mac;
    }

    public String getMesh_id() {
        return mesh_id;
    }

    public void setMesh_id(String mesh_id) {
        this.mesh_id = mesh_id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getIdf_version() {
        return idf_version;
    }

    public void setIdf_version(String idf_version) {
        this.idf_version = idf_version;
    }

    public String getMdf_version() {
        return mdf_version;
    }

    public void setMdf_version(String mdf_version) {
        this.mdf_version = mdf_version;
    }

    public String getMlink_version() {
        return mlink_version;
    }

    public void setMlink_version(String mlink_version) {
        this.mlink_version = mlink_version;
    }

    public String getMlink_trigger() {
        return mlink_trigger;
    }

    public void setMlink_trigger(String mlink_trigger) {
        this.mlink_trigger = mlink_trigger;
    }

    public String getMlink_scenes() {
        return mlink_scenes;
    }

    public void setMlink_scenes(String mlink_scenes) {
        this.mlink_scenes = mlink_scenes;
    }

    public String getEth_ip() {
        return eth_ip;
    }

    public void setEth_ip(String eth_ip) {
        this.eth_ip = eth_ip;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getStatus_msg() {
        return status_msg;
    }

    public void setStatus_msg(String status_msg) {
        this.status_msg = status_msg;
    }

    public String getStatus_code() {
        return status_code;
    }

    public void setStatus_code(String status_code) {
        this.status_code = status_code;
    }

    public List<CidBean> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(List<CidBean> characteristics) {
        this.characteristics = characteristics;
    }

    @Override
    public String toString() {
        return "YourClass{" +
                "ip='" + ip + '\'' +
                ", position='" + position + '\'' +
                ", mfrs='" + mfrs + '\'' +
                ", tid='" + tid + '\'' +
                ", name='" + name + '\'' +
                ", self_mac='" + self_mac + '\'' +
                ", mesh_id='" + mesh_id + '\'' +
                ", version='" + version + '\'' +
                ", idf_version='" + idf_version + '\'' +
                ", mdf_version='" + mdf_version + '\'' +
                ", mlink_version='" + mlink_version + '\'' +
                ", mlink_trigger='" + mlink_trigger + '\'' +
                ", mlink_scenes='" + mlink_scenes + '\'' +
                ", eth_ip='" + eth_ip + '\'' +
                ", ssid='" + ssid + '\'' +
                ", password='" + password + '\'' +
                ", channel='" + channel + '\'' +
                ", status_msg='" + status_msg + '\'' +
                ", status_code='" + status_code + '\'' +
                ", characteristics=" + characteristics +
                '}';
    }
}
