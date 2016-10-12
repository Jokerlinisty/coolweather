package com.coolweather.app.model;
/**
 * 县份表
 * Created by linwei on 2016-10-09.
 */
public class County {

	private int id; // 主键，自增
	private String countyName; // 县份名称
	private String countyCode; // 县份代码
	private int cityId; // 城市id

	public County() {
	}

	public County(String countyName, String countyCode, int cityId) {
		this.countyName = countyName;
		this.countyCode = countyCode;
		this.cityId = cityId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCountyName() {
		return countyName;
	}

	public void setCountyName(String countyName) {
		this.countyName = countyName;
	}

	public String getCountyCode() {
		return countyCode;
	}

	public void setCountyCode(String countyCode) {
		this.countyCode = countyCode;
	}

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	@Override
	public String toString() {
		return "County{" + "id=" + id + ", countyName='" + countyName + '\'' + ", countyCode='" + countyCode + '\'' + ", cityId=" + cityId + '}';
	}
}
