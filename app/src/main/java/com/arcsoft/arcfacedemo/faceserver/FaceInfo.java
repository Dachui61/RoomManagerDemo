package com.arcsoft.arcfacedemo.faceserver;

public class FaceInfo {
    private String name;
    private byte[] featureData;

    public FaceInfo(String name, byte[] featureData) {
        this.name = name;
        this.featureData = featureData;
    }

    public String getName() {
        return name;
    }

    public byte[] getFeatureData() {
        return featureData;
    }

}
