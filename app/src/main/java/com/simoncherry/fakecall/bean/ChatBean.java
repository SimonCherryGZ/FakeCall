package com.simoncherry.fakecall.bean;

/**
 * Created by Simon on 2016/8/2.
 */
public class ChatBean {

    private String text;
    private String time;
    private boolean isMine;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }
}
