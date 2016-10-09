package com.coolweather.app.model;

/**
 * 省份表
 * Created by linwei on 2016-10-09.
 */
public class Province {

    private int id; // 主键，自增
    private String provinceName; // 省份名称
    private String provinceCode; // 省份代码

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

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    @Override
    public String toString() {
        return "Province{" + "id=" + id + ", provinceName='" + provinceName + '\'' + ", provinceCode='" + provinceCode + '\'' + '}';
    }
}
