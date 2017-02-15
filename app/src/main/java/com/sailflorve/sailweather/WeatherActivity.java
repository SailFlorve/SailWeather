package com.sailflorve.sailweather;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.sailflorve.sailweather.gson.BingImages;
import com.sailflorve.sailweather.gson.DailyForecast;
import com.sailflorve.sailweather.gson.HourlyForecast;
import com.sailflorve.sailweather.gson.Weather;
import com.sailflorve.sailweather.util.HttpUtil;
import com.sailflorve.sailweather.util.Settings;
import com.sailflorve.sailweather.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends BaseActivity implements View.OnClickListener
{
    public DrawerLayout drawerLayout;
    private Button navButton;
    private Button menuButton;
    private Button addCityButton;
    private Button aboutButton;
    private Button exitButton;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private LinearLayout hourlyForecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView qualityText;
    private TextView comfortText;
    private TextView sportText;
    private TextView fluText;
    private TextView travelText;
    private TextView bingPicInfo;
    private TextView dressText;
    private TextView uvText;
    private ImageView bingPicImg;
    private TextView appNameText;
    private ImageView weatherPic;
    private TextView windInfo;
    public SwipeRefreshLayout swipeRefresh;
    private ImageView fab;
    private CardView hourlyForecastCardView;
    private CardView aqiLayout;
    private CardView suggestionLayout;
    private ImageView appImage;
    private ListView savedCitiesListView;
    private ListView themeListView;

    private final String weatherKey = "d8adf978646b45e2875b82c9fed6d3eb";
    private String mCityName;

    private Settings settings;
    private LocationClient client;

    private final String CURRENT_VERSION = "1.7.0";
    private final String appInfo = "By SailFlorve Ver " + CURRENT_VERSION;

    final int[] themesId = {R.style.AppTheme, R.style.RedTheme, R.style.PinkTheme,
            R.style.PurpleTheme, R.style.DeepPurpleTheme, R.style.IndigoTheme,
            R.style.BlueTheme, R.style.GreenTheme, R.style.BrownTheme, R.style.BleGreyTheme};

    private Boolean showBingPic;

    private ArrayAdapter<String> adapter1;
    private List<String> themeList = new ArrayList<>();

    private ArrayAdapter<String> adapter2;

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
            int themeNum = (int) settings.get("current_theme", 0);
            try
            {
                setTheme(themesId[themeNum]);
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                e.printStackTrace();
            }

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
        hourlyForecastLayout = (LinearLayout) findViewById(R.id.hourly_forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        qualityText = (TextView) findViewById(R.id.qlty_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        travelText = (TextView) findViewById(R.id.travel_text);
        uvText = (TextView) findViewById(R.id.uv_text);
        fluText = (TextView) findViewById(R.id.flu_text);
        dressText = (TextView) findViewById(R.id.dress_text);
        weatherPic = (ImageView) findViewById(R.id.weather_pic);
        bingPicInfo = (TextView) findViewById(R.id.bing_pic_info);
        appImage = (ImageView) findViewById(R.id.elva_image);
        windInfo = (TextView) findViewById(R.id.wind_info_text);
        appNameText = (TextView) findViewById(R.id.app_name_text);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);
        suggestionLayout = (CardView) findViewById(R.id.suggestion_layout);
        addCityButton = (Button) findViewById(R.id.add_city);
        aboutButton = (Button) findViewById(R.id.about_button);
        exitButton = (Button) findViewById(R.id.exit_button);
        fab = (ImageView) findViewById(R.id.float_button);
        menuButton = (Button) findViewById(R.id.menu_button);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        aqiLayout = (CardView) findViewById(R.id.aqi_layout);
        hourlyForecastCardView = (CardView) findViewById(R.id.hourly_forecast_card_view);
        savedCitiesListView = (ListView) findViewById(R.id.city_listview);
        themeListView = (ListView) findViewById(R.id.theme_settings_listview);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        client = new LocationClient(getApplicationContext());
        client.registerLocationListener(new MyLocationListener());
        fab.setOnClickListener(this);
        exitButton.setOnClickListener(this);
        aboutButton.setOnClickListener(this);
        addCityButton.setOnClickListener(this);
        navButton.setOnClickListener(this);
        menuButton.setOnClickListener(this);

        appNameText.setText("Sail天气");

        checkUpdate("load");
        initViewLists();
        initWeather();
        //设置下拉刷新事件
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                if ((Boolean) settings.get("auto_loc", false))
                {
                    mCityName = "auto_loc";
                }
                requestWeather(mCityName);
                loadBingPic();
            }
        });

        themeListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (position == 0)
                {
                    chooseColor();
                }
                if (position == 1)
                {
                    showBingPic = (Boolean) settings.get("show_bing_pic", true);
                    if (showBingPic)
                    {
                        settings.put("show_bing_pic", false);
                        bingPicImg.setVisibility(View.INVISIBLE);
                        themeList.set(1, "显示天气背景");
                        adapter1.notifyDataSetChanged();
                    }
                    else if (!showBingPic)
                    {
                        settings.put("show_bing_pic", true);
                        bingPicImg.setVisibility(View.VISIBLE);
                        themeList.set(1, "隐藏天气背景");
                        adapter1.notifyDataSetChanged();
                    }
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });

        savedCitiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                swipeRefresh.setRefreshing(true);
                drawerLayout.closeDrawer(GravityCompat.START);
                String cityName = CityManager.getCityList().get(position);
                if (cityName.contains("自动定位"))
                {
                    settings.put("auto_loc", true);
                    startLocation();
                }
                else
                {
                    settings.put("auto_loc", false);
                    loadWeather(cityName);
                }

            }
        });

        savedCitiesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                CityManager.deleteCity(CityManager.getCityList().get(position));
                adapter2.notifyDataSetChanged();
                return true;
            }
        });

        appImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String[] items = new String[]{"使用每日一图", "自定义图片"};
                AlertDialog dialog = new AlertDialog.Builder(WeatherActivity.this)
                        .setTitle("更改图片")
                        .setItems(items, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                switch (which)
                                {
                                    case 0:
                                        settings.put("use_bing_pic", true);
                                        loadBingPic();
                                        break;
                                    case 1:
                                        checkPermission();
                                        break;
                                }
                            }
                        }).create();
                dialog.show();
            }
        });
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            //选择城市按钮点击事件
            case R.id.nav_button:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            //菜单点击事件
            case R.id.menu_button:
                showPopupMenu(menuButton);
                break;
            case R.id.float_button:
                weatherLayout.setVisibility(View.VISIBLE);
                fab.setVisibility(View.INVISIBLE);
                break;
            case R.id.about_button:
                showAboutDialog();
                break;
            case R.id.exit_button:
                finish();
                break;
            case R.id.add_city:
                Intent intent = new Intent(WeatherActivity.this, MainActivity.class);
                settings.put("change_city", true);
                startActivity(intent);
                break;
            default:
        }
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
                    case R.id.update:
                        requestWeather(mCityName);
                        loadBingPic();
                        break;

                    case R.id.watch_wallpaper:
                        Toast.makeText(WeatherActivity.this, "点击任意处恢复", Toast.LENGTH_SHORT).show();
                        weatherLayout.setVisibility(View.INVISIBLE);
                        fab.setVisibility(View.VISIBLE);
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
        drawerLayout.closeDrawer(GravityCompat.START);
        adapter2.notifyDataSetChanged();
        bingPicInfo.setText(appInfo);

        //如果没有网络且没有缓存， 使用默认背景图
        if (settings.get("bing_pic_json", null) == null && !Utility.isNetworkAvailable(WeatherActivity.this))
        {
            if ((boolean) settings.get("use_bing_pic", true))
            {
                Glide.with(WeatherActivity.this).load(R.drawable.bg).crossFade(500).into(bingPicImg);
                Glide.with(WeatherActivity.this).load(R.drawable.weather_pic).crossFade(500).into(appImage);
            }
            else
            {
                Glide.with(WeatherActivity.this).load(Uri.parse((String) settings.get("uri_string", null))).error(R.drawable.weather_pic).into(appImage);
            }
        }

        showBingPic = (Boolean) settings.get("show_bing_pic", true);
        if (showBingPic)
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
                mCityName = weather.basic.cityName;
            }
            else
            {
                mCityName = "auto_loc";
            }
            showWeatherInfo(weather);

            //如果是申请了切换城市(点击了城市管理那里)，更新城市天气id
            if ((Boolean) settings.get("change_city", false))
            {
                mCityName = getIntent().getStringExtra("city_name");
                settings.put("change_city", false);
            }
            requestWeather(mCityName);
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

            mCityName = getIntent().getStringExtra("city_name");
            requestWeather(mCityName);
        }
    }

    private void initViewLists()
    {

        themeList.add("选择主题色");
        showBingPic = (Boolean) settings.get("show_bing_pic", true);
        if (!showBingPic)
        {
            themeList.add("显示天气背景");
        }
        else
        {
            themeList.add("隐藏天气背景");
        }
        adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, themeList);
        themeListView.setAdapter(adapter1);
        themeListView.setDividerHeight(0);

        //一旦加载一个城市的天气，就添加列表；程序启动时读取列表；退出时保存列表。
        CityManager.loadCities();

        adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, CityManager.getCityList());
        savedCitiesListView.setAdapter(adapter2);
        savedCitiesListView.setDividerHeight(0);


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
        builder.setNeutralButton("检查更新", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                checkUpdate("button");
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

        developerInfo = (TextView) dialog.findViewById(R.id.developer_info);
        updateInfo = (TextView) dialog.findViewById(R.id.update_info);
        developerInfo.setText("Sail天气 Ver " + CURRENT_VERSION + "\n@SailFlorve");
        updateInfo.setText("正在加载...");
        HttpUtil.sendHttpRequest("http://www.sailflorve.com/elvaweather/update/update.txt", new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        updateInfo.setText("获取更新日志发生错误。");
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
    public void requestWeather(final String cityName)
    {
        swipeRefresh.setRefreshing(true);
        loadBingPic();

        if (cityName.equals("auto_loc"))
        {
            startLocation();
        }
        else
        {
            loadWeather(cityName);
        }
    }

    //根据城市名称，返回城市天气信息。
    private void loadWeather(final String cityName)
    {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                cityName + "&key=" + weatherKey;
        HttpUtil.sendHttpRequest(weatherUrl, new Callback()
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
                            mCityName = cityName;
                        }
                        else
                        {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败，错误代码11", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    //处理和展示Weather中的数据
    private void showWeatherInfo(Weather weather)
    {
        aqiLayout.setVisibility(View.VISIBLE);
        suggestionLayout.setVisibility(View.VISIBLE);
        hourlyForecastLayout.setVisibility(View.VISIBLE);
        showNowInfo(weather);
        showHourlyForecastInfo(weather);
        showForecastInfo(weather);
        showAqiInfo(weather);
        showSuggestInfo(weather);
    }

    private void showNowInfo(Weather weather)
    {
        String cityName = weather.basic.cityName;
        String updateTime = "更新时间 " + weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature;
        String weatherInfo = weather.now.more.info;
        String weatherCode = weather.now.more.code;
        String windDirection = weather.now.wind.direction;
        String windPower = weather.now.wind.power;

        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        StringBuilder windInfoText = new StringBuilder();
        windInfoText.append(windDirection + " " + windPower);
        if (!windPower.equals("微风")) windInfoText.append(" 级");
        windInfo.setText(windInfoText.toString());

        loadWeatherIcon(this.getResources().getIdentifier("icon" + weatherCode, "drawable", this.getPackageName()), weatherPic);
    }

    private void showHourlyForecastInfo(Weather weather)
    {
        if (weather.hourlyForecastList.size() == 0)
        {
            hourlyForecastCardView.setVisibility(View.GONE);
            return;
        }

        hourlyForecastLayout.removeAllViews();
        for (HourlyForecast forecast : weather.hourlyForecastList)
        {
            View view = LayoutInflater.from(this).inflate(R.layout.hourly_forecast_item, hourlyForecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.hourly_date_text);
            TextView infoText = (TextView) view.findViewById(R.id.hourly_weather_text);
            ImageView weatherIcon = (ImageView) view.findViewById(R.id.hourly_weather_icon);
            TextView tmpText = (TextView) view.findViewById(R.id.hourly_tmp_text);
            TextView windText = (TextView) view.findViewById(R.id.hourly_wind_info);
            String[] time = forecast.date.split(" ");
            dateText.setText(time[1]);
            infoText.setText(forecast.more.info);
            loadWeatherIcon(this.getResources().getIdentifier("icon" + forecast.more.code, "drawable", this.getPackageName()), weatherIcon);
            tmpText.setText(forecast.temperature + " ℃");

            StringBuilder windInfoText = new StringBuilder();
            windInfoText.append(forecast.wind.direction + " " + forecast.wind.level);
            if (!forecast.wind.level.equals("微风")) windInfoText.append(" 级");
            windText.setText(windInfoText.toString());
            hourlyForecastLayout.addView(view);
        }
    }

    private void showForecastInfo(Weather weather)
    {
        forecastLayout.removeAllViews();
        for (DailyForecast forecast : weather.forecastList)
        {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            ImageView weatherIcon = (ImageView) view.findViewById(R.id.weather_icon);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            String[] ymd = forecast.date.split("-");
            dateText.setText(ymd[1] + "月" + ymd[2] + "日");
            infoText.setText(forecast.more.info);
            loadWeatherIcon(this.getResources().getIdentifier("icon" + forecast.more.code, "drawable", this.getPackageName()), weatherIcon);
            maxText.setText(forecast.temperature.max + " ℃");
            minText.setText(forecast.temperature.min + " ~ ");
            forecastLayout.addView(view);
        }
    }

    private void showAqiInfo(Weather weather)
    {
        if (weather.aqi != null)
        {
            qualityText.setText(Html.fromHtml("空气质量状况<font><small>：" + weather.aqi.city.qlty + "</small></font>"));
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
        if (weather.suggestion == null)
        {
            suggestionLayout.setVisibility(View.GONE);
            return;
        }
        String comfort = "舒适度：" + "<font><big>" + weather.suggestion.comfort.level + "</big></font><br><br>" + weather.suggestion.comfort.info;
        String sport = "运动建议：" + "<font><big>" + weather.suggestion.sport.level + "</big></font><br><br>" + weather.suggestion.sport.info;
        String flu = "感冒指数：" + "<font><big>" + weather.suggestion.flu.level + "</big></font><br><br>" + weather.suggestion.flu.info;
        String travel = "旅游指数：" + "<font><big>" + weather.suggestion.travel.level + "</big></font><br><br>" + weather.suggestion.travel.info;
        String dress = "穿衣指数：" + "<font><big>" + weather.suggestion.dress.level + "</big></font><br><br>" + weather.suggestion.dress.info;
        String UV = "紫外线指数：" + "<font><big>" + weather.suggestion.uv.level + "</big></font><br><br>" + weather.suggestion.uv.info;

        comfortText.setText(Html.fromHtml(comfort));
        sportText.setText(Html.fromHtml(sport));
        fluText.setText(Html.fromHtml(flu));
        travelText.setText(Html.fromHtml(travel));
        uvText.setText(Html.fromHtml(UV));
        dressText.setText(Html.fromHtml(dress));
    }

    private void loadBingPic()
    {
        String oldBingPic = (String) settings.get("bing_pic_json", null);
        if (oldBingPic != null && !Utility.isNewDay())
        {
            final BingImages images = Utility.handleBingResponse(oldBingPic);
            final String imageUrl = "http://s.cn.bing.net" + images.url;
            Glide.with(WeatherActivity.this).load(imageUrl).error(R.drawable.bg).into(bingPicImg);
            if (!(boolean) settings.get("use_bing_pic", true))
            {
                Glide.with(WeatherActivity.this).load(Uri.parse((String) settings.get("uri_string", null))).error(R.drawable.weather_pic).into(appImage);
                bingPicInfo.setText(appInfo);
            }
            else
            {
                Glide.with(WeatherActivity.this).load(imageUrl).error(R.drawable.weather_pic).crossFade(500).into(appImage);
                bingPicInfo.setText(images.copyright);
            }
            return;
        }

        //String requestBingPic = "http://guolin.tech/api/bing_pic";
        String requestBingPic = "http://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";

        HttpUtil.sendHttpRequest(requestBingPic, new Callback()
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
                        Toast.makeText(WeatherActivity.this, "获取背景图片失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                final String bingPic = response.body().string();
                final BingImages images = Utility.handleBingResponse(bingPic);
                final String imageUrl = "http://s.cn.bing.net" + images.url;
                settings.put("bing_pic_json", bingPic);
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Glide.with(WeatherActivity.this).load(imageUrl).error(R.drawable.bg).crossFade(500).into(bingPicImg);
                        if (!(boolean) settings.get("use_bing_pic", true))
                        {
                            Glide.with(WeatherActivity.this).load(Uri.parse((String) settings.get("uri_string", null))).error(R.drawable.weather_pic).into(appImage);
                            bingPicInfo.setText(appInfo);
                        }
                        else
                        {
                            Glide.with(WeatherActivity.this).load(imageUrl).error(R.drawable.weather_pic).crossFade(500).into(appImage);
                            bingPicInfo.setText(images.copyright);
                        }
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
        AlertDialog dialog;
        final AlertDialog.Builder builder = new AlertDialog.Builder(WeatherActivity.this);
        builder.setTitle("请选择主题色");
        final String[] themes = {"炫酷黑", "姨妈红", "哔哩粉", "亮骚紫", "基佬紫", "深邃蓝", "知乎蓝", "草原绿", "绅士棕", "阴天灰"};
        //设置一个单项选择下拉框
        builder.setSingleChoiceItems(themes, (int) settings.get("current_theme", 0), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if ((int) settings.get("current_theme", 0) != which)
                {
                    settings.put("current_theme", which);
                    dialog.dismiss();
                    recreate();
                }
                else dialog.dismiss();
            }
        });
        dialog = builder.show();
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

    private void checkUpdate(final String type)
    {
        HttpUtil.sendHttpRequest("http://www.sailflorve.com/elvaweather/update/latest_version.txt", new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                final String newVersion = response.body().string();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        int result = Utility.compareVersion(CURRENT_VERSION, newVersion);
                        if (result == -1)
                        {
                            new AlertDialog.Builder(WeatherActivity.this).setTitle("版本升级")
                                    .setMessage("当前版本：" + CURRENT_VERSION + "\n发现新版本：" + newVersion + "\n是否升级？")
                                    .setPositiveButton("升级", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            Uri uri = Uri.parse("http://www.sailflorve.com/elvaweather/update/sailweather.apk");
                                            Intent it = new Intent(Intent.ACTION_VIEW, uri);
                                            startActivity(it);
                                        }
                                    }).setNegativeButton("不升级", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {

                                }
                            }).show();//在按键响应事件中显示此对话框
                        }
                        else
                        {
                            if (type.equals("button"))
                            {
                                Toast.makeText(WeatherActivity.this, "当前为最新版本", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
    }

    private void choosePhoto()
    {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
        {
            return;
        }
        switch (requestCode)
        {
            case 0:
                try
                {
                    Uri uri = data.getData();
                    settings.put("uri_string", uri.toString());
                    //Bitmap bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                    Glide.with(WeatherActivity.this).load(uri).into(appImage);
                    bingPicInfo.setText(appInfo);
                    settings.put("use_bing_pic", false);
                }
                catch (Exception e)
                {
                    Toast.makeText(WeatherActivity.this, "程序崩溃了", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                break;
        }
    }

    private void checkPermission()
    {
        if (ActivityCompat.checkSelfPermission
                (WeatherActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(WeatherActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        else
        {
            choosePhoto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    choosePhoto();
                }
                else
                {
                    Toast.makeText(WeatherActivity.this, "拒绝权限将无法上传图片", Toast.LENGTH_SHORT).show();
                }
        }
    }

    public class MyLocationListener implements BDLocationListener
    {
        @Override
        public void onReceiveLocation(BDLocation bdLocation)
        {
            String district = bdLocation.getDistrict();
            final String city = bdLocation.getCity();
            if (city == null)
            {
                Toast.makeText(WeatherActivity.this, "定位失败，错误代码14", Toast.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);
                return;
            }

            String sendName = district;
            if (district.charAt(district.length() - 1) == '区')
            {
                sendName = city;
            }
            Toast.makeText(WeatherActivity.this, "定位成功，当前城市:" + sendName, Toast.LENGTH_SHORT).show();
            loadWeather(sendName);
            settings.put("loc_city", sendName);
            client.stop();
        }
    }
}
