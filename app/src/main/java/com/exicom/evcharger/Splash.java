package com.exicom.evcharger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;


public class Splash extends Activity
{
    public static final int TIMEOUT = 1000;
    Globals gInstance;
    CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        coordinatorLayout = findViewById(R.id.coordinator);
        gInstance=Globals.getInstance(getApplicationContext());


        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run() {
                Context context = getApplicationContext();
                String userToken = gInstance.getUserToken(context);

                if (!userToken.equals("0")){
                    Intent intent = new Intent(Splash.this, DeviceList.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                else {
                    Intent intent = new Intent(Splash.this, Login.class);
                    startActivity(intent);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    finish();
                }
            }
        },TIMEOUT);
    }
}
