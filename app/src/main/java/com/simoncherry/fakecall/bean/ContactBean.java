package com.simoncherry.fakecall.bean;

/**
 * Created by Simon on 2016/8/1.
 */
public class ContactBean {

    private int contactId;
    private String displayName;
    private String phoneNum;
    private String sortKey;
    private String lookUpKey;
    private int selected = 0;
    private String formattedNumber;
    private String pinyin;

    public int getContactId() {
        return contactId;
    }
    public void setContactId(int contactId) {
        this.contactId = contactId;
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public String getPhoneNum() {
        return phoneNum;
    }
    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }
    public String getSortKey() {
        return sortKey;
    }
    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }
    public String getLookUpKey() {
        return lookUpKey;
    }
    public void setLookUpKey(String lookUpKey) {
        this.lookUpKey = lookUpKey;
    }
    public int getSelected() {
        return selected;
    }
    public void setSelected(int selected) {
        this.selected = selected;
    }
    public String getFormattedNumber() {
        return formattedNumber;
    }
    public void setFormattedNumber(String formattedNumber) {
        this.formattedNumber = formattedNumber;
    }
    public String getPinyin() {
        return pinyin;
    }
    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }
}
