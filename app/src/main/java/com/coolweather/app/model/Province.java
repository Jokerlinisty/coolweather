package com.coolweather.app.model;

/**
 * 省份表
 * Created by linwei on 2016-10-09.
 */
public class Province {

    private int id; // 主键，自增
    private String provinceName; // 省份名称

    public Province() {
    }

    public Province(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    @Override
    public String toString() {
        return "Province{" + "id=" + id + ", provinceName='" + provinceName + '\'' + '}';
    }
}
