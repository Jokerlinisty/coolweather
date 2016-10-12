package com.coolweather.app.model;
/**
 * 城市表
 * Created by linwei on 2016-10-09.
 */
public class City {

	private int id; // 主键，自增
	private String cityName; // 城市名称
	private int provinceId;  // 省份id

	public City() {
	}

	public City(String cityName, int provinceId) {
		this.cityName = cityName;
		this.provinceId = provinceId;
	}

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

	public int getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(int provinceId) {
		this.provinceId = provinceId;
	}

	@Override
	public String toString() {
		return "City{" + "id=" + id + ", cityName='" + cityName + '\'' + ", provinceId=" + provinceId + '}';
	}
}
