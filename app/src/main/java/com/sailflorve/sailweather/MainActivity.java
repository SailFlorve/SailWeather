package com.sailflorve.sailweather;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sailflorve.sailweather.util.Settings;
import com.sailflorve.sailweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity
{

    private Settings settings;
    private Button autoLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        settings = new Settings(this);
        setContentView(R.layout.activity_main);
        autoLoc = (Button) findViewById(R.id.auto_loc);
        Intent intent = getIntent();

        String locCity = (String) settings.get("loc_city", null);
        if (locCity != null)
        {
            autoLoc.setText("自动定位(" + locCity + ")");
        }

        //如果已经存储了weather（天气JSON数组），而且没有申请切换城市，则直接进入天气界面，不选择城市。
        if (settings.get("weather", null) != null && !((Boolean) settings.get("change_city", false)))
        {
            Intent weatherIntent = new Intent(this, WeatherActivity.class);
            startActivity(weatherIntent);
            finish();
        }
        autoLoc.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!Utility.isNetworkAvailable(MainActivity.this))
                {
                    Toast.makeText(MainActivity.this, "没有网络，无法定位。", Toast.LENGTH_SHORT).show();
                    return;
                }
                requestPermissions();
                settings.put("auto_loc", true);
            }
        });

    }

    private void requestPermissions()
    {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
        {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!permissionList.isEmpty())
        {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }
        else
        {
            useAutoLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case 1:
                if (grantResults.length > 0)
                {
                    for (int result : grantResults)
                    {
                        if (result != PackageManager.PERMISSION_GRANTED)
                        {
                            Toast.makeText(this, "必须同意所有权限，才可使用自动定位。", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    useAutoLocation();
                }
                else
                {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    private void useAutoLocation()
    {
        Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
        intent.putExtra("weather_id", "auto_loc");
        startActivity(intent);
        this.finish();
    }
}
