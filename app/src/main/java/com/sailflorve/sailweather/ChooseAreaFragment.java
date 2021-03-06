package com.sailflorve.sailweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sailflorve.sailweather.db.City;
import com.sailflorve.sailweather.db.County;
import com.sailflorve.sailweather.db.Province;
import com.sailflorve.sailweather.db.SavedCity;
import com.sailflorve.sailweather.gson.Weather;
import com.sailflorve.sailweather.util.HttpUtil;
import com.sailflorve.sailweather.util.Settings;
import com.sailflorve.sailweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    public static final int LEVEL_INPUT = 3;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private EditText editText;
    private Button okCityButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    List<Weather> inputCities;

    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    private Settings settings;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        editText = (EditText) view.findViewById(R.id.edit_text);
        okCityButton = (Button) view.findViewById(R.id.ok_city);
        settings = new Settings(getContext());
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    SavedCity savedCity = new SavedCity();
                    savedCity.setName(countyList.get(position).getCountyName());
                    savedCity.setWeatherId(countyList.get(position).getWeatherId());
                    CityManager.addCity(savedCity);

                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("city_weather_id", countyList.get(position).getWeatherId());
                    settings.put("auto_loc", false);
                    startActivity(intent);
                    getActivity().finish();
                } else if (currentLevel == LEVEL_INPUT) {
                    Weather weather = inputCities.get(position);
                    SavedCity city = new SavedCity();
                    city.setName(weather.basic.cityName);
                    city.setWeatherId(weather.basic.cityWeatherId);
                    CityManager.addCity(city);

                    Intent intent = new Intent(getContext(), WeatherActivity.class);
                    intent.putExtra("city_weather_id", city.getWeatherId());
                    settings.put("auto_loc", false);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                } else if (currentLevel == LEVEL_INPUT) {
                    queryProvinces();
                    editText.setText(null);
                }
            }
        });

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setFocusable(true);
                editText.setFocusableInTouchMode(true);
            }
        });

        okCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String input = editText.getText().toString().trim();
                if (TextUtils.isEmpty(input)) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            queryProvinces();
                            Toast.makeText(getContext(), "还未输入城市哦", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                showProgressDialog();
                HttpUtil.sendHttpRequest("https://free-api.heweather.com/v5/search?city="
                        + input + "&key=d8adf978646b45e2875b82c9fed6d3eb", new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "服务器连接失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                        closeProgressDialog();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        //Weather weather = Utility.handleWeatherResponse(response.body().string());
                        inputCities = Utility.handleInputCityResponse(response.body().string());
                        closeProgressDialog();
                        if (!inputCities.get(0).status.equals("ok")) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    queryProvinces();
                                    Toast.makeText(getContext(), "未找到此城市，请重新输入", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            currentLevel = LEVEL_INPUT;
                            dataList.clear();
                            for (int i = 0; i < inputCities.size(); i++) {
                                StringBuilder name = new StringBuilder();
                                Weather weather = inputCities.get(i);
                                name.append(weather.basic.cityName).append("，");
                                name.append(TextUtils.isEmpty(weather.basic.province) ? "" : weather.basic.province + "，");
                                name.append(weather.basic.country);
                                dataList.add(name.toString());
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    backButton.setVisibility(View.VISIBLE);
                                    adapter.notifyDataSetChanged();
                                    titleText.setText("选择城市");
                                }
                            });
                        }
                    }

                });
            }
        });
        queryProvinces();
    }

    //从数据库里寻找province表的provinceid一列，并用其初始化privinceList
    //如果还没有数据库，就用queryFromServer请求返回省数据的JSON数组，再加入数据库。
    //dataList为listview的adapter绑定的list，通知list更新。
    private void queryProvinces() {
        titleText.setText("选择城市");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }


    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败，可能是因为没有网络", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            Toast.makeText(getContext(), "加载列表失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(true);
        }
        progressDialog.show();
    }
}
