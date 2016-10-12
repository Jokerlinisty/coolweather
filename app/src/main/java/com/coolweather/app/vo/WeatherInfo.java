package com.coolweather.app.vo;

/**
 * 天气信息类
 * Created by linwei on 2016-10-10.
 */
public class WeatherInfo {
    private String cityName;    // 城市名称
    private String weatherCode; // 天气代码（用来查天气信息）
    private String lowTemp;     // 最低温度
    private String highTemp;    // 最高温度
    private String weather;     // 天气状况（晴、多云、小雨等）
    private String publishTime; // 发布时间
    private boolean citySelected; // 是否选中城市

    public WeatherInfo() {
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(String weatherCode) {
        this.weatherCode = weatherCode;
    }

    public String getLowTemp() {
        return lowTemp;
    }

    public void setLowTemp(String lowTemp) {
        this.lowTemp = lowTemp;
    }

    public String getHighTemp() {
        return highTemp;
    }

    public void setHighTemp(String highTemp) {
        this.highTemp = highTemp;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public boolean isCitySelected() {
        return citySelected;
    }

    public void setCitySelected(boolean citySelected) {
        this.citySelected = citySelected;
    }

    @Override
    public String toString() {
        return "WeatherInfo{" + "cityName='" + cityName + '\'' + ", weatherCode='" + weatherCode + '\'' + ", lowTemp='" + lowTemp + '\'' + ", highTemp='" + highTemp + '\'' +
                ", weather='" + weather + '\'' + ", publishTime='" + publishTime + '\'' + ", citySelected=" + citySelected + '}';
    }
}
