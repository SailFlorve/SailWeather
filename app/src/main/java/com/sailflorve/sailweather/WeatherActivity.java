package com.sailflorve.sailweather;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.sailflorve.sailweather.gson.CityInfo;
import com.sailflorve.sailweather.gson.Forecast;
import com.sailflorve.sailweather.gson.Weather;
import com.sailflorve.sailweather.util.HttpUtil;
import com.sailflorve.sailweather.util.Settings;
import com.sailflorve.sailweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WeatherActivity extends BaseActivity
{
    public DrawerLayout drawerLayout;
    private Button navButton;
    private Button menuButton;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView qualityText;
    private TextView comfortText;
    private TextView sportText;
    private TextView fluText;
    private TextView travelText;
    private TextView dressText;
    private TextView uvText;
    private ImageView bingPicImg;
    private TextView appNameText;
    private ImageView weatherPic;
    private TextView windInfo;
    public SwipeRefreshLayout swipeRefresh;
    private ImageView fab;
    private LinearLayout aqiLayout;
    private ImageView appImage;
    private TextView bottomText;

    private final String weatherKey = "d8adf978646b45e2875b82c9fed6d3eb";
    private String mWeatherId;

    private Settings settings;
    private LocationClient client;

    private final int VERSION_TO_ELVA = 0;
    private final int VERSION_TO_PUBLIC = 1;

    private Boolean showBingPic;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        settings = new Settings(this);
        if (Build.VERSION.SDK_INT >= 21)
        {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            //选择并设置主题
            int theme = (int) settings.get("current_theme", R.style.AppTheme);
            setTheme(theme);
        }
        setContentView(R.layout.activity_weather);

        //初始化各个控件
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        qualityText = (TextView) findViewById(R.id.qlty_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        travelText = (TextView) findViewById(R.id.travel_text);
        uvText = (TextView) findViewById(R.id.uv_text);
        fluText = (TextView) findViewById(R.id.flu_text);
        dressText = (TextView) findViewById(R.id.dress_text);
        bottomText = (TextView) findViewById(R.id.bottom_text);
        weatherPic = (ImageView) findViewById(R.id.weather_pic);
        appImage = (ImageView) findViewById(R.id.elva_image);
        windInfo = (TextView) findViewById(R.id.wind_info_text);
        appNameText = (TextView) findViewById(R.id.app_name_text);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);
        fab = (ImageView) findViewById(R.id.float_button);
        menuButton = (Button) findViewById(R.id.menu_button);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        aqiLayout = (LinearLayout) findViewById(R.id.aqi_layout);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        client = new LocationClient(getApplicationContext());
        client.registerLocationListener(new MyLocationListener());
        initWeather();
        //设置下拉刷新事件
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                if ((Boolean) settings.get("auto_loc", false))
                {
                    mWeatherId = "auto_loc";
                }
                requestWeather(mWeatherId);
                loadBingPic();
            }
        });
        //选择城市按钮点击事件
        navButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        //菜单点击事件
        menuButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showPopupMenu(menuButton);
            }
        });
        //透明ImageView点击事件
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                weatherLayout.setVisibility(View.VISIBLE);
                fab.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
        initWeather();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        settings.put("change_city", false);
    }

    //创建菜单
    private void showPopupMenu(View view)
    {
        // View当前PopupMenu显示的相对View的位置
        PopupMenu popupMenu = new PopupMenu(this, view);
        // menu布局
        popupMenu.getMenuInflater().inflate(R.menu.title_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                switch (item.getItemId())
                {
                    case R.id.change_city:
                        Intent intent = new Intent(WeatherActivity.this, MainActivity.class);
                        settings.put("change_city", true);
                        startActivity(intent);

                        break;

                    case R.id.update:
                        requestWeather(mWeatherId);
                        loadBingPic();
                        break;

                    case R.id.watch_wallpaper:
                        Toast.makeText(WeatherActivity.this, "点击任意处恢复", Toast.LENGTH_SHORT).show();
                        weatherLayout.setVisibility(View.INVISIBLE);
                        fab.setVisibility(View.VISIBLE);
                        break;

                    case R.id.off_pic:
                        showBingPic = (Boolean) settings.get("show_bing_pic", true);
                        if (showBingPic)
                        {
                            settings.put("show_bing_pic", false);
                            bingPicImg.setVisibility(View.INVISIBLE);
                        }
                        else if (!showBingPic)
                        {
                            settings.put("show_bing_pic", true);
                            bingPicImg.setVisibility(View.VISIBLE);
                        }
                        break;

                    case R.id.choose_color:
                        chooseColor();
                        break;

                    case R.id.about:
                        showAboutDialog();
                        break;
                    case R.id.exit:
                        finish();
                        break;
                    default:
                }
                return true;
            }
        });
        popupMenu.show();
    }


    //初始化app，包括显示数据、加载图片
    private void initWeather()
    {
        appNameText.setText("Sail天气");
        bottomText.setText("By SailFlorve");

        //如果没有网络 使用默认背景图
        if (settings.get("bing_pic", null) == null || !Utility.isNetworkAvailable(WeatherActivity.this))
        {
            Glide.with(WeatherActivity.this).load(R.drawable.bg).into(bingPicImg);
            Glide.with(WeatherActivity.this).load(R.drawable.weather_pic).into(appImage);
        }

        showBingPic = (Boolean) settings.get("show_bing_pic", true);

        if (showBingPic == true)
        {
            bingPicImg.setVisibility(View.VISIBLE);
        }
        else
        {
            bingPicImg.setVisibility(View.INVISIBLE);
        }

        String weatherString = (String) settings.get("weather", null);

        if (weatherString != null)
        {
            //有缓存，直接解析天气
            Weather weather = Utility.handleWeatherResponse(weatherString);
            if (!(Boolean) settings.get("auto_loc", false))
            {
                mWeatherId = weather.basic.cityWeatherId;
            }
            else
            {
                mWeatherId = "auto_loc";
            }
            showWeatherInfo(weather);

            //如果是申请了切换城市，更新城市天气id
            if ((Boolean) settings.get("change_city", false))
            {
                mWeatherId = getIntent().getStringExtra("weather_id");
                settings.put("change_city", false);
            }
            requestWeather(mWeatherId);
        }

        else
        {
            titleCity.setText("未选择城市");
            degreeText.setText("0");
            titleUpdateTime.setText("获取天气信息失败");
            weatherInfoText.setText("null");
            qualityText.setText("null");
            aqiText.setText("null");
            pm25Text.setText("null");
            comfortText.setText("null");
            sportText.setText("null");
            fluText.setText("null");
            travelText.setText("null");
            uvText.setText("null");
            dressText.setText("null");

            mWeatherId = getIntent().getStringExtra("weather_id");
            requestWeather(mWeatherId);
        }
    }

    private void showAboutDialog()
    {
        TextView developerInfo;
        final TextView updateInfo;


        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setTitle("关于");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

        developerInfo = (TextView) dialog.findViewById(R.id.developer_info);
        updateInfo = (TextView) dialog.findViewById(R.id.update_info);
        developerInfo.setText("Sail天气是一款界面美观、功能方便、简单易用的天气软件。\n开发者：SailFlorve");
        updateInfo.setText("正在加载...");
        HttpUtil.sendOkHttpRequest("http://www.sailflorve.com/elvaweather/update/update.txt", new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        updateInfo.setText("网络连接错误，请在联网后重试。");
                    }
                });

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException
            {
                final String updateStr = response.body().string();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        updateInfo.setText(updateStr);
                    }
                });
            }
        });

    }

    //根据天气id请求天气信息
    public void requestWeather(final String weatherId)
    {
        swipeRefresh.setRefreshing(true);
        loadBingPic();

        if (weatherId.equals("auto_loc"))
        {
            startLocation();
        }
        else
        {
            loadWeather(weatherId);
        }
        swipeRefresh.setRefreshing(false);
    }

    private void loadWeather(final String weatherId)
    {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                weatherId + "&key=" + weatherKey;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                e.printStackTrace();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败，错误代码10", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);//关键
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (weather != null && "ok".equals(weather.status))
                        {
                            settings.put("weather", responseText);
                            showWeatherInfo(weather);
                            mWeatherId = weatherId;
                        }
                        else
                        {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败，错误代码11", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
    }

    //处理和展示Weather中的数据
    private void showWeatherInfo(Weather weather)
    {
        showNowInfo(weather);
        showForecastInfo(weather);
        showAqiInfo(weather);
        showSuggestInfo(weather);
    }

    private void showNowInfo(Weather weather)
    {
        String cityName = weather.basic.cityName;
        String updateTime = "更新时间" + weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature;
        String weatherInfo = weather.now.more.info;
        String weatherCode = weather.now.more.code;
        String windDirection = weather.now.wind.direction;
        String windPower = weather.now.wind.power;

        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        windInfo.setText(windDirection + " " + windPower + " 级");

        loadWeatherIcon(this.getResources().getIdentifier("icon" + weatherCode, "drawable", this.getPackageName()), weatherPic);
    }

    private void showForecastInfo(Weather weather)
    {
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList)
        {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            ImageView weatherIcon = (ImageView) view.findViewById(R.id.weather_icon);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            loadWeatherIcon(this.getResources().getIdentifier("icon" + forecast.more.code, "drawable", this.getPackageName()), weatherIcon);
            maxText.setText("高温 " + forecast.temperature.max + " ℃");
            minText.setText("低温 " + forecast.temperature.min + " ℃");
            forecastLayout.addView(view);
        }
    }

    private void showAqiInfo(Weather weather)
    {
        if (weather.aqi != null)
        {
            qualityText.setText("空气质量状况: " + weather.aqi.city.qlty);
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        else
        {
            aqiLayout.setVisibility(View.GONE);
        }
    }

    private void showSuggestInfo(Weather weather)
    {
        String comfort = "舒适度: " + weather.suggestion.comfort.level + "\n" + weather.suggestion.comfort.info;
        String sport = "运动建议: " + weather.suggestion.sport.level + "\n" + weather.suggestion.sport.info;
        String flu = "感冒指数: " + weather.suggestion.flu.level + "\n" + weather.suggestion.flu.info;
        String travel = "旅游指数: " + weather.suggestion.travel.level + "\n" + weather.suggestion.travel.info;
        String dress = "穿衣指数: " + weather.suggestion.dress.level + "\n" + weather.suggestion.dress.info;
        String UV = "紫外线指数: " + weather.suggestion.uv.level + "\n" + weather.suggestion.uv.info;

        comfortText.setText(comfort);
        sportText.setText(sport);
        fluText.setText(flu);
        travelText.setText(travel);
        uvText.setText(UV);
        dressText.setText(dress);
    }

    private void loadBingPic()
    {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                e.printStackTrace();
                Looper.prepare();
                Toast.makeText(WeatherActivity.this, "加载背景图片失败。", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                        Glide.with(WeatherActivity.this).load(bingPic).into(appImage);

                    }
                });
            }
        });
    }

    //加载天气晴雨图标
    private void loadWeatherIcon(final int resourceId, ImageView imageView)
    {
        if (Util.isOnMainThread())
        {
            Glide.with(getApplicationContext()).load(resourceId).into(imageView);
        }
    }



    //选择主题对话框
    private void chooseColor()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(WeatherActivity.this);
        builder.setTitle("请选择主题色");
        final String[] sex = {"炫酷黑", "姨妈红", "哔哩粉", "亮骚紫", "基佬紫", "深邃蓝", "知乎蓝", "草原绿", "亮瞎橙", "绅士棕", "阴天灰"};
        //    设置一个单项选择下拉框
        /**
         * 第一个参数指定我们要显示的一组下拉单选框的数据集合
         * 第二个参数代表索引，指定默认哪一个单选框被勾选上，1表示默认'女' 会被勾选上
         * 第三个参数给每一个单选项绑定一个监听器
         */
        builder.setSingleChoiceItems(sex, -1, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch (which)
                {
                    case 0:
                        settings.put("current_theme", R.style.AppTheme);
                        break;
                    case 1:
                        settings.put("current_theme", R.style.RedTheme);
                        break;
                    case 2:
                        settings.put("current_theme", R.style.PinkTheme);
                        break;
                    case 3:
                        settings.put("current_theme", R.style.PurpleTheme);
                        break;
                    case 4:
                        settings.put("current_theme", R.style.DeepPurpleTheme);
                        break;
                    case 5:
                        settings.put("current_theme", R.style.IndigoTheme);
                        break;
                    case 6:
                        settings.put("current_theme", R.style.BlueTheme);
                        break;
                    case 7:
                        settings.put("current_theme", R.style.GreenTheme);
                        break;
                    case 8:
                        settings.put("current_theme", R.style.OrangeTheme);
                        break;
                    case 9:
                        settings.put("current_theme", R.style.BrownTheme);
                        break;
                    case 10:
                        settings.put("current_theme", R.style.BleGreyTheme);
                        break;
                }
            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                recreate();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });
        builder.show();
    }

    public void startLocation()
    {
        initLocation();
        client.start();
    }

    private void initLocation()
    {
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        client.setLocOption(option);
    }

    public class MyLocationListener implements BDLocationListener
    {
        @Override
        public void onReceiveLocation(BDLocation bdLocation)
        {
            String district = bdLocation.getDistrict();
            final String city = bdLocation.getCity();
            Toast.makeText(WeatherActivity.this, "定位成功，当前城市:" + city, Toast.LENGTH_SHORT).show();

            String sendName = district;
            if (district.charAt(district.length() - 1) == '区')
            {
                sendName = city;
            }

            HttpUtil.sendOkHttpRequestForCity("https://free-api.heweather.com/v5/search", sendName, new Callback()
            {
                @Override
                public void onFailure(Call call, IOException e)
                {
                    e.printStackTrace();
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(WeatherActivity.this, "获取城市信息失败，错误代码12", Toast.LENGTH_SHORT).show();
                            swipeRefresh.setRefreshing(false);
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException
                {
                    final String responseText = response.body().string();
                    final CityInfo cityInfo = Utility.handleCityInfoResponse(responseText);
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (cityInfo != null && cityInfo.status.equals("ok"))
                            {
                                settings.put("loc_city", cityInfo.basic.city);
                                loadWeather(cityInfo.basic.id);
                            }
                            else
                            {
                                Toast.makeText(WeatherActivity.this, "获取城市信息失败，错误代码13", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            });

            client.stop();
        }
    }
}
