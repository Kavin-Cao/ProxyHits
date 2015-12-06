package com.kevin.proxyhits;

public class LocalIPResult {
    /**
     * 接口返回格式` var returnCitySN = {"cip": "119.123.164.243", "cid": "440300",
     * "cname": "广东省深圳市"};`
     */
    public static final String API_URL = "http://pv.sohu.com/cityjson?ie=utf-8";

    private String cip;

    private String cid;

    private String cname;

    public String getCip() {
        return cip;
    }

    public String getCid() {
        return cid;
    }

    public String getCname() {
        return cname;
    }

    public void setCip(String cip) {
        this.cip = cip;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }
}
