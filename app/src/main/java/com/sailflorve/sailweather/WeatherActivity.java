package com.sailflorve.sailweather;

import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AppCompatActivity;
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

import com.bumptech.glide.Glide;
import com.sailflorve.sailweather.gson.Forecast;
import com.sailflorve.sailweather.gson.Weather;
import com.sailflorve.sailweather.util.HttpUtil;
import com.sailflorve.sailweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity
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
    private String mWeatherId;
    private ImageView fab;
    private LinearLayout aqiLayout;

    private ImageView appImage;
    private TextView bottomText;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private final String weatherKey = "d8adf978646b45e2875b82c9fed6d3eb";

    private final int VERSION_TO_ELVA = 0;
    private final int VERSION_TO_PUBLIC = 1;

    private boolean showBingPic;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (Build.VERSION.SDK_INT >= 21)
        {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            //选择并设置主题
            int theme = prefs.getInt("current_theme", R.style.AppTheme);
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
        editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();

        initWeather();
        //设置下拉刷新事件
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
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
                        drawerLayout.openDrawer(GravityCompat.START);
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
                        showBingPic = prefs.getBoolean("show_bing_pic", true);
                        if (showBingPic == true)
                        {
                            editor.putBoolean("show_bing_pic", false);
                            editor.apply();
                            bingPicImg.setVisibility(View.INVISIBLE);
                        }
                        else if (showBingPic == false)
                        {
                            editor.putBoolean("show_bing_pic", true);
                            editor.apply();
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
        if (prefs.getString("bing_pic", null) == null || isNetworkAvailable(WeatherActivity.this) == false)
        {
            Glide.with(WeatherActivity.this).load(R.drawable.bg).into(bingPicImg);
            Glide.with(WeatherActivity.this).load(R.drawable.weather_pic).into(appImage);
        }

        showBingPic = prefs.getBoolean("show_bing_pic", true);
        if (showBingPic == true)
        {
            bingPicImg.setVisibility(View.VISIBLE);
        }
        else
        {
            bingPicImg.setVisibility(View.INVISIBLE);
        }

        String weatherString = prefs.getString("weather", null);

        if (weatherString != null)
        {
            //有缓存，直接解析天气
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
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

                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        String updateStr = new String();
                        try
                        {
                            updateStr = response.body().string();
                            updateInfo.setText(updateStr);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }

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
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                            mWeatherId = weatherId;
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
        Glide.with(WeatherActivity.this).load(resourceId).into(imageView);
    }

    //返回网络是否畅通
    public static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected())
            {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED)
                {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
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
                        editor.putInt("current_theme", R.style.AppTheme);
                        break;
                    case 1:
                        editor.putInt("current_theme", R.style.RedTheme);
                        break;
                    case 2:
                        editor.putInt("current_theme", R.style.PinkTheme);
                        break;
                    case 3:
                        editor.putInt("current_theme", R.style.PurpleTheme);
                        break;
                    case 4:
                        editor.putInt("current_theme", R.style.DeepPurpleTheme);
                        break;
                    case 5:
                        editor.putInt("current_theme", R.style.IndigoTheme);
                        break;
                    case 6:
                        editor.putInt("current_theme", R.style.BlueTheme);
                        break;
                    case 7:
                        editor.putInt("current_theme", R.style.GreenTheme);
                        break;
                    case 8:
                        editor.putInt("current_theme", R.style.OrangeTheme);
                        break;
                    case 9:
                        editor.putInt("current_theme", R.style.BrownTheme);
                        break;
                    case 10:
                        editor.putInt("current_theme", R.style.BleGreyTheme);
                        break;
                }
                editor.apply();
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
}