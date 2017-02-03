package com.sailflorve.sailweather;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class BaseActivity extends AppCompatActivity
{
    //方便观察Activity的生命周期
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState)
    {
        //Log.d(getClass().getSimpleName(),"onCreate");
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onRestart()
    {
        //Log.d(getClass().getSimpleName(),"onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume()
    {
        //Log.d(getClass().getSimpleName(),"onResume");
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        //Log.d(getClass().getSimpleName(),"onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        //Log.d(getClass().getSimpleName(),"onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onStop()
    {
        //Log.d(getClass().getSimpleName(),"onStop");
        super.onStop();
    }

    @Override
    protected void onStart()
    {
        //Log.d(getClass().getSimpleName(),"onStart");
        super.onStart();
    }
}
