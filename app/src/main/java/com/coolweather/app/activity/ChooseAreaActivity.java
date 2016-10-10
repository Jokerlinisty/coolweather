package com.coolweather.app.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
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
import com.coolweather.app.util.HttpCallBackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends Activity implements AdapterView.OnItemClickListener{

    public static final String TYPE_PROVINCE = "province"; // 省类型
    public static final String TYPE_CITY = "city"; // 市类型
    public static final String TYPE_COUNTY = "county"; // 区/县类型

    public static final int LEVEL_PROVINCE = 0; // 省级别
    public static final int LEVEL_CITY = 1; // 市级别
    public static final int LEVEL_COUNTY = 2; // 区级别

    private ProgressDialog progressDialog; // 进度对话框
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            case LEVEL_PROVINCE:
                selectedProvince = provinceList.get(position);
                queryCities(); // 查询某省份所有城市
                break;
            case LEVEL_CITY:
                selectedCity = cityList.get(position);
                queryCounties(); // 查询某城市所有区/县
                break;
            default:
                break;
        }
    }

    /**
     *  查询所有省份。先从数据库查，如果没有则从指定服务器获取
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
            queryFromServer("china", TYPE_PROVINCE);
        }
    }

    /**
     *  查询选中省份下所有城市。先从数据库查，如果没有则从指定服务器获取
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
        }else{
            queryFromServer(selectedProvince.getProvinceCode(), TYPE_CITY);
        }
    }

    /**
     *  查询选中城市下所有区/县。先从数据库查，如果没有则从指定服务器获取
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
        }else{
            queryFromServer(selectedCity.getCityCode(), TYPE_COUNTY);
        }
    }

    /**
     * 根据省市区代码，从服务器查询相应的省市区/县数据
     */
    private void queryFromServer(final String code, final String type) {
        String address = this.getAddressByType(code, type);
        if (TextUtils.isEmpty(address)){
            return;
        }
        // 显示加载进度对话框
        showProgressDialog();
        // 执行查询
        HttpUtil.sendHttpRequest(address, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                switch (type){
                    case TYPE_PROVINCE:
                        result = Utility.handleProvincesResponse(coolWeatherDb, response);
                        break;
                    case TYPE_CITY:
                        result = Utility.handleCitiesResponse(coolWeatherDb, response, code, selectedProvince.getId());
                        break;
                    case TYPE_COUNTY:
                        result = Utility.handleCountiesResponse(coolWeatherDb, response, code, selectedCity.getId());
                        break;
                    default:
                        break;
                }

                if(result){
                    // 通过runOnUiThread方法回到主线程
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog(); // 关闭弹窗
                            switch (type){
                                case TYPE_PROVINCE:
                                    queryProvinces();
                                    break;
                                case TYPE_CITY:
                                    queryCities();
                                    break;
                                case TYPE_COUNTY:
                                    queryCounties();
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                // 通过runOnUiThread方法回到主线程
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog(); // 关闭弹窗
                        Toast.makeText(ChooseAreaActivity.this, "加载失败，请稍后再试。", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 根据传入省市区/县类型，返回服务器获取地址
     */
    private String getAddressByType(String code, String type){
        String address = "";
        if (TextUtils.isEmpty(code)){
            address = "http://www.weather.com.cn/data/city3jdata/china.html";
        }else{
            switch (type){
                case TYPE_PROVINCE:
                    address = "http://www.weather.com.cn/data/city3jdata/china.html";
                    break;
                case TYPE_CITY:
                    address = "http://www.weather.com.cn/data/city3jdata/provshi/" + code + ".html";
                    break;
                case TYPE_COUNTY:
                    address = "http://www.weather.com.cn/data/city3jdata/station/" + code + ".html";
                    break;
                default:
                    break;
            }
        }
        return address;
    }

    /**
     * 显示加载进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    /**
     * 补货Back按键，根据当前级别，此时应该返回省、市、区还是直接退出
     */
    @Override
    public void onBackPressed() {
        if (currencyLevel == LEVEL_COUNTY){
            queryCities();
        }else if(currencyLevel == LEVEL_CITY){
            queryProvinces();
        }else{
            finish();
        }
    }
}
