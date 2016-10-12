package com.coolweather.app.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.R;
import com.coolweather.app.db.CoolWeatherDb;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.AddressUtil;
import com.coolweather.app.util.HttpCallBackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends Activity implements AdapterView.OnItemClickListener{

    public static final int LEVEL_PROVINCE = 0; // 省级别
    public static final int LEVEL_CITY = 1; // 市级别
    public static final int LEVEL_COUNTY = 2; // 区级别

    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDb coolWeatherDb;
    private List<String> dataList = new ArrayList<>();

    private List<Province>  provinceList; // 省列表
    private List<City> cityList; // 市列表
    private List<County> countyList; // 县列表
    private Province selectedProvince; // 选中的省份
    private City selectedCity; // 选中的城市
    private int currencyLevel; // 当前选中的级别

    private boolean isFromWeatherActivity; // 是否从WeatherActivity跳转过来

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);

        // 尝试从SharedPreferences中取出城市代码
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        // 如果已选且不是从WeatherActivity跳转过来，则直接展示天气信息
        if (pref.getBoolean("city_selected", false) && !isFromWeatherActivity){
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_choose_area);

        titleText = (TextView) findViewById(R.id.title_text);
        listView = (ListView) findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        coolWeatherDb = CoolWeatherDb.getInstance(this);
        listView.setOnItemClickListener(this); // 注册Item单击事件
        queryProvinces(); // 默认加载所有省份
    }

    /**
     * ListView的Item单击事件
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (currencyLevel){
            case LEVEL_PROVINCE: // 选中省份，则查其下所有城市
                selectedProvince = provinceList.get(position);
                queryCities();
                break;
            case LEVEL_CITY: // 选中城市，则查其下所有县/区
                selectedCity = cityList.get(position);
                queryCounties();
                break;
            case LEVEL_COUNTY: // 选中县/区，则直接跳到天气活动，展示天气信息
                String countyCode = countyList.get(position).getCountyCode();
                Intent intent = new Intent(this, WeatherActivity.class);
                intent.putExtra("county_code", countyCode);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
    }

    /**
     *  查询所有省份。先从数据库查，如果没有则解析本地天气城市代码
     */
    private void queryProvinces() {
        provinceList = coolWeatherDb.loadProvinces();
        if (provinceList.size() > 0){
            dataList.clear();
            for (Province province: provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged(); // 通知列表改变
            listView.setSelection(0);
            titleText.setText("中国");
            currencyLevel = LEVEL_PROVINCE; // 设置当前级别
        }else{
            // 解析本地天气城市代码Json，并保存到数据库中（正常只会执行一次，就是在进入app时）
            int result = parseAndSaveCityCodeToDB();
            if (result > 0){ // 确保数据库操作成功且有数据，否则会造成死循环！
                queryProvinces(); // 重新查询
            }
        }
    }

    /**
     * 解析并保存天气城市代码到数据库中
     */
    private int parseAndSaveCityCodeToDB() {
        int result = 0;
        // 读取本地天气城市文件Json信息
        String cityCodeJson = Utility.readCityCodeFromRaw(ChooseAreaActivity.this, R.raw.city_code, "utf-8");
        // 解析Json并保存到数据库中
        Utility.saveCityCodeToDB(coolWeatherDb, cityCodeJson);
        // 这里查询下省份记录数，确保以上操作成功
        result = coolWeatherDb.getTableCount(CoolWeatherDb.T_PROVINCE);
        return result;
    }

    /**
     *  查询选中省份下所有城市。先从数据库查，如果没有则解析本地天气城市代码
     */
    private void queryCities() {
        cityList = coolWeatherDb.loadCities(selectedProvince.getId());
        if (cityList.size() > 0){
            dataList.clear();
            for (City city: cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged(); // 通知列表改变
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currencyLevel = LEVEL_CITY;
        }
    }

    /**
     *  查询选中城市下所有区/县。先从数据库查，如果没有则解析本地天气城市代码
     */
    private void queryCounties() {
        countyList = coolWeatherDb.loadCounties(selectedCity.getId());
        if (countyList.size() > 0){
            dataList.clear();
            for (County county: countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged(); // 通知列表改变
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currencyLevel = LEVEL_COUNTY;
        }
    }

    /**
     * 捕获Back按键，根据当前级别，此时应该返回省、市、区还是直接退出
     */
    @Override
    public void onBackPressed() {
        if (currencyLevel == LEVEL_COUNTY){
            queryCities();
        }else if(currencyLevel == LEVEL_CITY){
            queryProvinces();
        }else{
            if (isFromWeatherActivity){ // 如果从跳转WeatherActivity过来，则原路返回
                Intent intent = new Intent(this, WeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }
}
