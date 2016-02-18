package com.rango.yesweather.activities;

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

import com.rango.yesweather.R;
import com.rango.yesweather.db.YesWeatherDB;
import com.rango.yesweather.model.City;
import com.rango.yesweather.model.County;
import com.rango.yesweather.model.Province;
import com.rango.yesweather.util.HttpCallbackListener;
import com.rango.yesweather.util.HttpUtil;
import com.rango.yesweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends Activity {

    public static final int LEVEL_PROVENCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView title_tv;
    private ListView choose_area_lv;
    private ArrayAdapter<String> adapter;
    private YesWeatherDB yesWeatherDB;
    private List<String> dataList = new ArrayList<String>();
    //省列表
    private List<Province> provinceList;
    //市列表
    private List<City> cityList;
    //县列表
    private List<County> countyList;
    //选中的省
    private Province selectProvince;
    //选中的市
    private City selectCity;
    //当前选中级别
    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("city_selected", false)) {
            Intent intent = new Intent(this, ChooseAreaWeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area_ly);
        choose_area_lv = (ListView) findViewById(R.id.choose_area_lv);
        title_tv = (TextView) findViewById(R.id.title_tv);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,dataList);
        choose_area_lv.setAdapter(adapter);
        yesWeatherDB = YesWeatherDB.getInstance(this);
        choose_area_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVENCE){
                    selectProvince = provinceList.get(position);
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    selectCity = cityList.get(position);
                    queryCounties();
                }else if (currentLevel == LEVEL_COUNTY){
                    title_tv.setText(countyList.get(position).getCountyName());
                    String countyCode = countyList.get(position).getCountyCode();
                    Intent intent = new Intent(ChooseAreaActivity.this,ChooseAreaWeatherActivity.class);
                    intent.putExtra("county_code", countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvinces();
    }

    /*
    查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryProvinces() {
        provinceList = yesWeatherDB.loadProvince();
        if (provinceList.size() > 0){
            dataList.clear();
            for (Province p : provinceList){
                dataList.add(p.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            choose_area_lv.setSelection(0);
            title_tv.setText("中国");
            currentLevel = LEVEL_PROVENCE;
        }else{
            queryFormServer(null,"province");
        }
    }

    /*
    查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCounties() {
        countyList = yesWeatherDB.loadCounties(selectCity.getId());
        if (countyList.size() > 0){
            dataList.clear();
            for (County c : countyList){
                dataList.add(c.getCountyName());
            }
            adapter.notifyDataSetChanged();
            choose_area_lv.setSelection(0);
            title_tv.setText(selectCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        }else {
            queryFormServer(selectCity.getCityCode(),"county");
        }
    }

    /*
    查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCities() {
        cityList = yesWeatherDB.loadCities(selectProvince.getId());
        if (cityList.size() > 0){
            dataList.clear();
            for (City c : cityList){
                dataList.add(c.getCityName());
            }
            adapter.notifyDataSetChanged();
            choose_area_lv.setSelection(0);
            title_tv.setText(selectProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        }else{
            queryFormServer(selectProvince.getProvinceCode(),"city");
        }
    }

    /*
    根据传入的代号和类型从服务器上查询省市县数据。
     */
    private void queryFormServer(final String code, final String type) {
        String address;
        if (!TextUtils.isEmpty(code)){
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProvincesResponse(yesWeatherDB,response);
                }else if ("city".equals(type)){
                    result = Utility.handleCitiesResponse(yesWeatherDB,response,selectProvince.getId());
                }else if ("county".equals(type)){
                    result = Utility.handleCountiesResponse(yesWeatherDB,response,selectCity.getId());
                }
                if (result){
                    //通过runOnUiThread()方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                //通过runOnUiThread()方法回到主线程处理逻辑
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,"加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /*
    显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /*
    关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    /*
    捕获Back按键，根据当前的级别来判断，此时应该返回市列表、省列表、还是直接退出。
     */
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (currentLevel == LEVEL_COUNTY){
            queryCities();
        }else if (currentLevel == LEVEL_CITY){
            queryProvinces();
        }else {
            finish();
        }
    }
}
