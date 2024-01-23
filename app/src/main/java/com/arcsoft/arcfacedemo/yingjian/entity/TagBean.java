package com.arcsoft.arcfacedemo.yingjian.entity;

public class TagBean {
    private String tag;
    private String content;
    private String fileName;
    private boolean writeToFile;

    public TagBean(String tag, String content, String fileName) {
        this.tag = tag;
        this.content = content;
        this.fileName = fileName;
        writeToFile = true;
    }

    public TagBean(String tag, String content, String fileName, boolean writeToFile) {
        this.tag = tag;
        this.content = content;
        this.fileName = fileName;
        this.writeToFile = writeToFile;
    }

    public boolean isWriteToFile() {
        return writeToFile;
    }

    public void setWriteToFile(boolean writeToFile) {
        this.writeToFile = writeToFile;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
