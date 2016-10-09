package com.coolweather.app.model;
/**
 * 城市表
 * Created by linwei on 2016-10-09.
 */
public class City {

	private int id; // 主键，自增
	private String cityName; // 城市名称
	private String cityCode; // 城市代码
	private int provinceId;  // 省份id

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public int getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(int provinceId) {
		this.provinceId = provinceId;
	}

	@Override
	public String toString() {
		return "City{" + "id=" + id + ", cityName='" + cityName + '\'' + ", cityCode='" + cityCode + '\'' + ", provinceId=" + provinceId + '}';
	}
}
