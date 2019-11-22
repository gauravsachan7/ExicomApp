package com.exicom.evcharger;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DeviceList extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{
    SqliteHelper sqliteHelper;
    RecyclerView recyclerView;
    FrameLayout frameLayout;
    List<DataHandler> deviceList;
    DeviceListAdapter deviceListAdapter;
    CallWebService callWebService;
    LinearLayout profile_view, main_layout;
    TextView view_profile, hide_profile, userName, userEmail;
    FloatingActionButton addDevice;
    Globals gInstance;
    ImageView guest_notif;
    Button btn_logout;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        gInstance = new Globals();
        getSupportActionBar().hide();
        main_layout = findViewById(R.id.main_layout);
        view_profile = findViewById(R.id.view_profile);
        addDevice = findViewById(R.id.add_device);
        userEmail = findViewById(R.id.user_email);
        userName = findViewById(R.id.user_name);
        guest_notif = findViewById(R.id.guest_notif);
        userEmail.setText(Globals.getUserEmail(this));
        userName.setText(Globals.getUserName(this));
        frameLayout = findViewById(R.id.frame_layout);
        recyclerView = findViewById(R.id.recycle);
        profile_view = findViewById(R.id.profile_view);
        hide_profile = findViewById(R.id.hide_profile);
        btn_logout = findViewById(R.id.btn_logout);
        deviceList = new ArrayList<>();
        sqliteHelper = new SqliteHelper(DeviceList.this);
        deviceList = sqliteHelper.getData();
        deviceListAdapter = new DeviceListAdapter(deviceList, this);
        RecyclerView.LayoutManager reLayoutManager =new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(reLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(deviceListAdapter);

        mSwipeRefreshLayout = findViewById(R.id.refresh_view);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        mSwipeRefreshLayout.post(new Runnable() {

            @Override
            public void run() {

                mSwipeRefreshLayout.setRefreshing(true);

                // Fetching data from server
                getDeviceList();
            }
        });

        showHideProfile();

        createAddDevice();

        goToGuestAccess();
    }

    protected void showHideProfile(){
        view_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main_layout.setVisibility(View.GONE);
                profile_view.setVisibility(View.VISIBLE);
            }
        });

        hide_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile_view.setVisibility(View.GONE);
                main_layout.setVisibility(View.VISIBLE);
            }
        });

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gInstance.clearAllPreferences(DeviceList.this);
                SqliteHelper db = new SqliteHelper(DeviceList.this);
                db.clearData();
                Intent intent = new Intent(DeviceList.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }


    protected void getDeviceList(){
        callWebService = (CallWebService) new CallWebService(DeviceList.this, "", Globals.GET_DEVICES_TAG+Globals.getUserId(DeviceList.this), new CallWebService.DataReceivedListener() {
            @Override
            public void onDataReceived(String data) {
                // Stopping swipe refresh
                mSwipeRefreshLayout.setRefreshing(false);
                if(data != null && !data.equals("Unauthorized")){
                    try {
                        JSONArray jsonArray = new JSONArray(data);
                        deviceList = new ArrayList<>();
                        DataHandler dataHandler = null;
                        SqliteHelper db = new SqliteHelper(DeviceList.this);
                        db.clearData();
                        for(int i = 0; i < jsonArray.length(); i++){
                            dataHandler = new DataHandler();
                            JSONObject devObject = new JSONObject(String.valueOf(jsonArray.get(i)));
                            String client_dev_no = devObject.getString("client_dev_no");
                            String nickname = devObject.getString("nickname");
                            dataHandler.setCharger_serial_no(client_dev_no);
                            dataHandler.setId(devObject.getString("id"));
                            dataHandler.setNick_name(nickname);
                            dataHandler.setClient_certificate(devObject.getString("client_certificate"));
                            dataHandler.setCreated_by(devObject.getString("created_by"));
                            if(!checkDevice(client_dev_no)){
                                String client_certificate = devObject.get("client_certificate").toString();
                                addSqlite(client_dev_no, client_certificate, nickname);
                            }
                            deviceList.add(dataHandler);
                        }
                        deviceListAdapter = new DeviceListAdapter(deviceList, DeviceList.this);
                        recyclerView.swapAdapter(deviceListAdapter, false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else if(data.equals("Unauthorized")){
                    //gInstance.clearAllPreferences(DeviceList.this);
                    SqliteHelper db = new SqliteHelper(DeviceList.this);
                    db.clearData();
                    Toast.makeText(DeviceList.this, "User Token expired, re login again", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(DeviceList.this, Login.class);
                    startActivity(intent);
                    finish();
                }
            }
        }).execute();
    }

    private boolean checkDevice(String device_name){
        SqliteHelper db = new SqliteHelper(DeviceList.this);
        return db.checkDevice(device_name);
    }

    private void addSqlite(String device_number, String client_certificate, String nick_name){
        SqliteHelper db = new SqliteHelper(DeviceList.this);
        db.addDevice(device_number, device_number, nick_name, Globals.getUserId(DeviceList.this), client_certificate);
    }

    protected void createAddDevice(){
        addDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                if(info != null && info.isConnected()){
                    checkCameraPermissions();
                }
            }
        });
    }

    protected void goToGuestAccess(){
        guest_notif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceList.this, GuestAccessList.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if(info != null && info.isConnected()){
            getDeviceList();
        }
    }

    public void checkCameraPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 100);
            }else if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            } else {
                Intent intent = new Intent(DeviceList.this, BarcodeCaptureActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        checkCameraPermissions();
    }

    @Override
    public void onRefresh() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if(info != null && info.isConnected()){
            getDeviceList();
        }
    }
}
