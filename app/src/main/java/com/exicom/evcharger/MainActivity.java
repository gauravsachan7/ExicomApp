package com.exicom.evcharger;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class MainActivity extends AppCompatActivity {

    LinearLayout homeView, configView, searchView;

    TextView showControl, showStatus;

    //Home top menu
    LinearLayout controlLayout, statusLayout;

    //control items
    TextView current_val, session_time, session_kwh, gun_command_text, gun_status_text, set_schedule;
    ImageView gun_command_image;
    RelativeLayout loadingPanel;

    //Status top menu
    TextView btnLiveData, btnAlarms, btnLogs, btnSessionLog, btnAlarmLogs;

    //Status items
    LinearLayout live_data_layout, live_alarm_layout, log_data_layout, session_log_layout, alarm_log_layout;
    ListView live_alarm_list_view,live_data_list, session_log_list, alarm_log_list;
    LiveAlarmListAdapter liveAlarmListAdapter;
    LiveDataAdapter liveDataAdapter;
    SessionLogAdapter sessionLogAdapter;
    AlarmLogAdapter alarmLogAdapter;
    Button btnReportAlarm;

    //Log Items
    Integer alarmLogCount = 0;
    Integer alarmCountVal = 0;
    Integer sessionLogCount = 0;
    String alarmLogString = "";
    String sessionLogString = "";
    Integer sessionCountVal = 0;
    String lastCommand = "";

    //Configuration view items
    RelativeLayout ac_input, config_server, config_wifi, config_system, config_bluetooth, config_protection, config_rfid, config_guestaccess, config_swupdate;

    //Flags for each views
    boolean get_controlData = false, get_alarmData = false, get_logData = false, getting_alaramLogs = false, getting_sessionLogs = false, inMain = true;

    int deviceIndex = 0;
    ProgressDialog progressDialog = null;
    String deviceID, devPassword, finalHexPass, dev_id;
    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    private final static int REQUEST_ENABLE_BT = 2;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    ArrayList<BluetoothDevice> devicesDiscovered = new ArrayList<>();
    BluetoothGatt bluetoothGatt = null;
    Globals gInstance;
    CallWebService callWebService;

    // Stops scanning after 5 seconds.
    private Handler mHandler;
    private static final long SCAN_PERIOD = 20000;
    final static int REQUEST_LOCATION = 199;

    private GoogleApiClient googleApiClient;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    configView.setVisibility(View.GONE);
                    searchView.setVisibility(View.GONE);
                    homeView.setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_configuration:
                    searchView.setVisibility(View.GONE);
                    homeView.setVisibility(View.GONE);
                    configView.setVisibility(View.VISIBLE);
                    return true;
//                case R.id.navigation_search:
//                    configView.setVisibility(View.GONE);
//                    homeView.setVisibility(View.GONE);
//                    searchView.setVisibility(View.VISIBLE);
//                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        mHandler = new Handler();
        homeView = findViewById(R.id.home_view);
        configView = findViewById(R.id.config_view);
        searchView = findViewById(R.id.search_view);
        live_data_layout = findViewById(R.id.live_data_layout);
        live_alarm_layout = findViewById(R.id.live_alarm_layout);
        log_data_layout = findViewById(R.id.log_data_layout);
        gInstance = new Globals();
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        setHomeControls();
        createBluetoothConnection();
    }

    protected void setHomeControls(){
        showControl = findViewById(R.id.show_ctrl);
        showStatus = findViewById(R.id.show_status);
        controlLayout = findViewById(R.id.control_view);
        statusLayout = findViewById(R.id.status_view);
        loadingPanel = findViewById(R.id.loadingPanel);
        showControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHomeData();
            }
        });
        showStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                get_controlData = false;
                get_alarmData = false;
                get_logData = false;
                showStatus.setTextColor(getResources().getColor(R.color.colorWhite));
                showStatus.setTypeface(null, Typeface.BOLD);
                showControl.setTextColor(getResources().getColor(R.color.colorGrey));
                showControl.setTypeface(null, Typeface.NORMAL);
                statusLayout.setVisibility(View.VISIBLE);
                controlLayout.setVisibility(View.GONE);
            }
        });

        setControlItems();
        setStatusItems();
    }

    protected void setControlItems(){
        current_val = findViewById(R.id.current_val);
        session_time = findViewById(R.id.session_time);
        session_kwh = findViewById(R.id.session_kwh);
        gun_command_text = findViewById(R.id.gun_command_text);
        gun_status_text = findViewById(R.id.gun_status_text);
        gun_command_image = findViewById(R.id.gun_command_image);
        set_schedule = findViewById(R.id.set_schedule);
        gInstance.setInitialLiveData();
        gun_command_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gun_command_text.getText().equals("Start Charging")) {
                    writeCommand(Globals.START_CHARGING+finalHexPass);
                }else if(gun_command_text.getText().equals("Stop Charging") || gun_command_text.getText().toString().trim().equals("Paused")){
                    writeCommand(Globals.STOP_CHARGING+finalHexPass);
                }else if(gun_status_text.getText().equals("Please Reconnect")) {
                    Toast.makeText(MainActivity.this, "Please disconnect and reconnect the Plug", Toast.LENGTH_LONG).show();
                }else if(gun_status_text.getText().equals("Alarm")){
                    showStatus.performClick();
                    btnAlarms.performClick();
                }else{
                    Toast.makeText(MainActivity.this, "Still in "+gun_status_text.getText().toString()+" mode", Toast.LENGTH_LONG).show();
                }
            }
        });

        set_schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gun_command_text.getText().equals("Start Charging")) {
                    inMain = false;
                    Intent intent = new Intent(MainActivity.this, SetConfiguration.class);
                    intent.putExtra("config_name", ((TextView) v).getText().toString());
                    intent.putExtra("charger_password", finalHexPass);
                    startActivity(intent);
                }else if(gun_status_text.getText().equals("Please Reconnect")) {
                    Toast.makeText(MainActivity.this, "Please disconnect and reconnect the Plug", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(MainActivity.this, "More options not available as charger is in "+gun_status_text.getText().toString()+" mode", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void setStatusItems(){
        btnLiveData = findViewById(R.id.btn_live_data);
        btnAlarms = findViewById(R.id.btn_alaram_data);
        btnLogs = findViewById(R.id.btn_log_data);
        btnSessionLog = findViewById(R.id.session_log_head);
        btnAlarmLogs = findViewById(R.id.alarm_log_head);
        live_data_list = findViewById(R.id.live_data_list);
        liveDataAdapter = new LiveDataAdapter(MainActivity.this, gInstance);
        live_data_list.setAdapter(liveDataAdapter);
        live_alarm_list_view = findViewById(R.id.live_alarm_list_view);
        liveAlarmListAdapter = new LiveAlarmListAdapter(MainActivity.this, gInstance);
        live_alarm_list_view.setAdapter(liveAlarmListAdapter);
        btnReportAlarm = findViewById(R.id.report_alarm);

        session_log_layout = findViewById(R.id.session_log_layout);
        alarm_log_layout = findViewById(R.id.alarm_log_layout);
        session_log_list = findViewById(R.id.session_log_list);
        sessionLogAdapter = new SessionLogAdapter(MainActivity.this, gInstance);
        session_log_list.setAdapter(sessionLogAdapter);
        alarm_log_list = findViewById(R.id.alarm_log_list);
        alarmLogAdapter = new AlarmLogAdapter(MainActivity.this, gInstance);
        alarm_log_list.setAdapter(alarmLogAdapter);
        btnLiveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                get_controlData = true;
                get_alarmData = false;
                get_logData = false;
                btnLiveData.setBackground(getResources().getDrawable(R.drawable.button_shape));
                btnAlarms.setBackground(getResources().getDrawable(R.drawable.edit_text_shape));
                btnLogs.setBackground(getResources().getDrawable(R.drawable.edit_text_shape));
                log_data_layout.setVisibility(View.GONE);
                live_alarm_layout.setVisibility(View.GONE);
                live_data_layout.setVisibility(View.VISIBLE);
                writeCommand(Globals.GET_AC_VOLTAGE);
            }
        });

        btnAlarms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                get_controlData = false;
                get_alarmData = true;
                get_logData = false;
                sendDisplayCommands(Globals.GET_ALARMS);
                btnLiveData.setBackground(getResources().getDrawable(R.drawable.edit_text_shape));
                btnAlarms.setBackground(getResources().getDrawable(R.drawable.button_shape));
                btnLogs.setBackground(getResources().getDrawable(R.drawable.edit_text_shape));
                log_data_layout.setVisibility(View.GONE);
                live_data_layout.setVisibility(View.GONE);
                live_alarm_layout.setVisibility(View.VISIBLE);
            }
        });

        btnLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                get_controlData = false;
                get_alarmData = false;
                get_logData = true;
                //sendDisplayCommands(Globals.GET_ALARM_LOG_COUNT);
                btnLiveData.setBackground(getResources().getDrawable(R.drawable.edit_text_shape));
                btnAlarms.setBackground(getResources().getDrawable(R.drawable.edit_text_shape));
                btnLogs.setBackground(getResources().getDrawable(R.drawable.button_shape));
                btnSessionLog.setBackground(getResources().getDrawable(R.drawable.edit_text_shape));
                btnAlarmLogs.setBackground(getResources().getDrawable(R.drawable.edit_text_shape));
                live_data_layout.setVisibility(View.GONE);
                live_alarm_layout.setVisibility(View.GONE);
                log_data_layout.setVisibility(View.VISIBLE);
                session_log_layout.setVisibility(View.GONE);
                alarm_log_layout.setVisibility(View.GONE);
            }
        });

        btnAlarmLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSessionLog.setBackground(getResources().getDrawable(R.drawable.edit_text_shape));
                btnAlarmLogs.setBackground(getResources().getDrawable(R.drawable.button_shape));
                session_log_layout.setVisibility(View.GONE);
                alarm_log_layout.setVisibility(View.VISIBLE);
                sendDisplayCommands(Globals.GET_ALARM_LOG_COUNT);
            }
        });

        btnSessionLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAlarmLogs.setBackground(getResources().getDrawable(R.drawable.edit_text_shape));
                btnSessionLog.setBackground(getResources().getDrawable(R.drawable.button_shape));
                alarm_log_layout.setVisibility(View.GONE);
                session_log_layout.setVisibility(View.VISIBLE);
                sendDisplayCommands(Globals.GET_SESSION_LOGS_COUNT);
            }
        });

        btnReportAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<HashMap<String, String>> alarmArray = gInstance.getAlarmList();
                ArrayList<HashMap<String, String>> liveDataArray = gInstance.getliveDataArray();
                if(alarmArray.size() > 0){

                    String alarmString = "";
                    for(int i = 0; i < alarmArray.size(); i++){
                        alarmString = alarmString+alarmArray.get(i).get("name")+", ";
                    }

                    String liveDataString = "";
                    for(int i = 0; i < liveDataArray.size(); i++){
                        liveDataString = liveDataString+liveDataArray.get(i).get("label")+"-"+liveDataArray.get(i).get("value")+", ";
                    }

                    Log.d("alarm_log", alarmString);
                    JSONObject requestObj = new JSONObject();
                    try{
                        String user_id = Globals.getUserId(MainActivity.this);
                        requestObj.put("user_id", user_id);
                        requestObj.put("device_number", deviceID);
                        requestObj.put("fault", alarmString);
                        requestObj.put("live_data", liveDataString);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                    callWebService = (CallWebService) new CallWebService(MainActivity.this, requestObj + "", Globals.CREATE_COMPLAINT, new CallWebService.DataReceivedListener() {
                        @Override
                        public void onDataReceived(String data) {
                            Toast.makeText(MainActivity.this, "Alarm reported successfully", Toast.LENGTH_LONG).show();
                        }
                    }).execute();
                }
            }
        });
    }

    public void showHomeData(){
        get_controlData = true;
        get_alarmData = false;
        get_logData = false;
        showControl.setTextColor(getResources().getColor(R.color.colorWhite));
        showControl.setTypeface(null, Typeface.BOLD);
        showStatus.setTextColor(getResources().getColor(R.color.colorGrey));
        showStatus.setTypeface(null, Typeface.NORMAL);
        statusLayout.setVisibility(View.GONE);
        controlLayout.setVisibility(View.VISIBLE);
        writeCommand(Globals.GET_AC_VOLTAGE);
    }

    public void configViewClicked(View v){
        inMain = false;
        Intent intent = new Intent(MainActivity.this, SetConfiguration.class);
        intent.putExtra("config_name", ((TextView) v).getText().toString());
        intent.putExtra("charger_password", finalHexPass);
        startActivity(intent);
    }

    public void configViewDelete(View v){
        new AlertDialog.Builder(this)
            .setTitle("Title")
            .setMessage("Do you really want to delete the device from your account?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    deleteDevice(dev_id);
                }})
            .setNegativeButton(android.R.string.no, null).show();
    }

    private void deleteDevice(String device_id){
        callWebService = (CallWebService) new CallWebService(MainActivity.this, "", Globals.DELETE_DEVICE + device_id, new CallWebService.DataReceivedListener() {
            @Override
            public void onDataReceived(String data) {
                Toast.makeText(MainActivity.this, "Charger deleted from account successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).execute();
    }

    public void createBluetoothConnection(){
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            deviceID = bundle.getString("charger_serial_no");
            dev_id = bundle.getString("charger_id");
            devPassword = bundle.getString("charger_password");
            String hexPass = Integer.toHexString(Integer.parseInt(devPassword)).toUpperCase();
            finalHexPass = ("000000"+ hexPass).substring(hexPass.length());
            Log.d("charger_serial_no", deviceID);
        }
        if(bluetoothGatt != null){
            bluetoothGatt.disconnect();
        }

        checkPermissions();
    }

    private void checkPermissions(){
        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }else{
            checkIfLocationEnabled();
        }
    }

    private void checkIfLocationEnabled(){
        LocationManager lm = (LocationManager)MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled){
            enableGps();
        }else{
            startBLEService();
        }
    }

    private void enableGps(){
        this.setFinishOnTouchOutside(true);

        // Todo Location Already on  ... start
        final LocationManager manager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(MainActivity.this)) {
            Toast.makeText(MainActivity.this,"Gps already enabled",Toast.LENGTH_SHORT).show();
            finish();
        }
        // Todo Location Already on  ... end

        if(!hasGPSDevice(MainActivity.this)){
            Toast.makeText(MainActivity.this,"Gps not Supported",Toast.LENGTH_SHORT).show();
        }

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(MainActivity.this)) {
            Log.e("TAG","Gps already enabled");
            Toast.makeText(MainActivity.this,"Gps not enabled",Toast.LENGTH_SHORT).show();
            enableLoc();
        }else{
            Log.e("TAG","Gps already enabled");
            Toast.makeText(MainActivity.this,"Gps already enabled",Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasGPSDevice(Context context) {
        final LocationManager mgr = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null)
            return false;
        final List<String> providers = mgr.getAllProviders();
        if (providers == null)
            return false;
        return providers.contains(LocationManager.GPS_PROVIDER);
    }

    private void enableLoc() {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.d("Location error","Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, REQUEST_LOCATION);

                            //finish();
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                }
            }
        });
    }

    private void startBLEService(){
        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }else{
            startScanning();
        }
    }

    public void startScanning() {
        deviceIndex = 0;
        devicesDiscovered.clear();
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.progressdialog);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScanning();
            }
        }, SCAN_PERIOD);
    }

    private void stopScanning(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
                checkIfFound();
            }
        });
    }

    private void checkIfFound(){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(devicesDiscovered.size() == 0) {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Charger Not Found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

    }

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            try {
                if(result.getDevice() != null && result.getDevice().getName() != null) {
                    Log.d("devicefound", result.getDevice().getName());
                    String deviceName = result.getDevice().getName();
                    String bleString = deviceID.substring(12);
                    Log.d("foundChange", bleString);
                    if (deviceName.toLowerCase().contains(bleString.toLowerCase())) {
                        devicesDiscovered.add(result.getDevice());
                        connectToDeviceSelected();
                    }
                }

            }catch (Exception e){

            }
        }
    };

    public void connectToDeviceSelected() {
        if(devicesDiscovered.size() > 0) {
            bluetoothGatt = devicesDiscovered.get(0).connectGatt(this, false, btleGattCallback);
            btScanner.stopScan(leScanCallback);
        }else{
            Toast.makeText(this, "No devices available to connect", Toast.LENGTH_SHORT).show();
        }
    }

    // Device connect call back
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    if(inMain) {
                        String received = Globals.bytesToHex(characteristic.getValue());
                        Log.d("received_data", received);
                        if (get_logData) {
                            parseLogData(received);
                        } else if(!get_logData && received.length() == 16){
                            parseData(received);
                        }
                    }
                }
            });
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            System.out.println(newState);
            switch (newState) {
                case 0:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MainActivity.this, "Device Disconnected", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                    break;
                case 2:
                    bluetoothGatt.discoverServices();
                    break;
                default:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                        }
                    });
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // this will get called after the client initiates a BluetoothGatt.discoverServices() call
            super.onServicesDiscovered(gatt, status);
            if(status == BluetoothGatt.GATT_SUCCESS) {
                enableTXNotification();
            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            String data_written = Globals.bytesToHex(characteristic.getValue());
            Log.d("writen_data",data_written);
            if(status == BluetoothGatt.GATT_SUCCESS) {
                if (inMain) {
                    if (get_controlData) {
                        parseControlCommands(data_written);
                    } else if (get_alarmData) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Handler handle = new Handler();
                                handle.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (get_alarmData) {
                                            sendDisplayCommands(Globals.GET_ALARMS);
                                        }
                                    }
                                }, 6000);
                            }
                        });
                    }
                    lastCommand = data_written;
                }
            }
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);

        }
    };

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        System.out.println(characteristic.getUuid());
    }

    public void enableTXNotification()
    {
        BluetoothGattService RxService = bluetoothGatt.getService(Globals.RX_SERVICE_UUID);
        if (RxService == null) {
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(Globals.TX_CHAR_UUID);
        if (TxChar == null) {
            return;
        }
        bluetoothGatt.setCharacteristicNotification(TxChar,true);

        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(Globals.CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(progressDialog != null){
                    progressDialog.dismiss();
                }
                writeCommand(Globals.GET_PASSWORD_COMMAND);

            }
        }, 2000);
    }

    public void writeCommand(final String cmd){
        byte[] strBytes = Globals.hexStringToByteArray(cmd);
        //byte[] strBytes = str.getBytes();
        try{
            BluetoothGattService RxService = bluetoothGatt.getService(Globals.RX_SERVICE_UUID);
            if (RxService == null) {
                return;
            }
            BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(Globals.RX_CHAR_UUID);
            if (RxChar == null) {
                return;
            }
            RxChar.setValue(strBytes);
            bluetoothGatt.writeCharacteristic(RxChar);
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    private void parseData(String data){
        if(data.length() > 16){
            data = data.substring(0, 16);
        }
        if(data.equals(Globals.START_CHARGING+finalHexPass)){
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                gun_command_text.setText("Starting...");
                gun_status_text.setText("Starting...");
                gun_command_image.setImageDrawable(getResources().getDrawable(R.drawable.stop_charging));
                }
            });
        }else if(data.equals(Globals.STOP_CHARGING+finalHexPass)){
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                gun_command_text.setText("Stopping...");
                gun_status_text.setText("Finishing...");
                gun_command_image.setImageDrawable(getResources().getDrawable(R.drawable.start_charging));
                }
            });
        }else if(data.equals(Globals.START_CHARGING+"FFFFFF") || data.equals(Globals.STOP_CHARGING+"FFFFFF")){
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Unauthorized Command", Toast.LENGTH_SHORT).show();
                }
            });
        }else if(data.length() == 16 && !data.substring(8).equals("FFFFFFFF")){

            String signalType = data.substring(4, 6);
            switch (signalType) {
                case Globals.SIGNAL_AC_VOLTAGE:{
                    String currVolt = data.substring(8);
                    Long i = Long.parseLong(currVolt, 16);
                    Float volVal = Float.intBitsToFloat(i.intValue());
                    final double volt = Math.round(volVal * 100.0) / 100.0;
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gInstance.setLiveDataArray(0, String.valueOf(volt)+" V");
                            liveDataAdapter.notifyDataSetChanged();
                        }
                    });
                    return;
                }

                case Globals.SIGNAL_CURRENT:{
                    String currString = data.substring(8);
                    Long i = Long.parseLong(currString, 16);
                    Float currentVal = Float.intBitsToFloat(i.intValue());
                    final double current = Math.round(currentVal * 100.0) / 100.0;
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            current_val.setText(String.valueOf(current)+" A");
                            gInstance.setLiveDataArray(1, String.valueOf(current)+" A");
                            liveDataAdapter.notifyDataSetChanged();
                        }
                    });
                    return;
                }

                case Globals.SIGNAL_SESSION_TIME:{
                    String sessString = data.substring(12);
                    Integer minuteVal = Integer.parseInt(sessString, 16);
                    int minutes = Math.round(minuteVal/60);
                    int hour = minutes / 60;
                    int remMin = minutes % 60;
                    final String finalHour = String.format("%02d", hour);
                    final String finalMin = String.format("%02d", remMin);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            session_time.setText(finalHour+":"+finalMin);
                            gInstance.setLiveDataArray(4, finalHour+":"+finalMin);
                            liveDataAdapter.notifyDataSetChanged();
                        }
                    });
                    return;
                }

                case Globals.SIGNAL_SESSION_UNIT:{
                    String unitString = data.substring(8);
                    float unitKwh = Integer.parseInt(unitString, 16);
                    final double unitkWh = Math.round(unitKwh/100 * 100.00)/100.00;
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            session_kwh.setText(String.valueOf(unitkWh)+" kWh");
                            gInstance.setLiveDataArray(3, String.valueOf(unitkWh)+" kWh");
                            liveDataAdapter.notifyDataSetChanged();
                        }
                    });
                    return;
                }

                case Globals.SIGNAL_KWH: {
                    String kwhStr = data.substring(8);
                    final Integer totalkwh = Math.round(Integer.parseInt(kwhStr, 16)/100);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            gInstance.setLiveDataArray(5, String.valueOf(totalkwh)+" kWh");
                            liveDataAdapter.notifyDataSetChanged();
                        }
                    });
                    return;
                }

                case Globals.SIGNAL_TOTAL_TIME:{
                    Log.d("Totol_time_received", data);
                    String timeStr = data.substring(8);

                    Integer minuteVal = Integer.parseInt(timeStr, 16);

                    int minutes = Math.round(minuteVal/60);
                    int hour = minutes / 60;
                    int remMin = minutes % 60;
                    final String finalHour = String.format("%02d", hour);
                    final String finalMin = String.format("%02d", remMin);

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gInstance.setLiveDataArray(6, finalHour+":"+finalMin);
                            liveDataAdapter.notifyDataSetChanged();
                        }
                    });
                    return;
                }

                case Globals.SIGNAL_NE_VOLTAGE:{
                    String neStr = data.substring(8);
                    Long i = Long.parseLong(neStr, 16);
                    Float neVal = Float.intBitsToFloat(i.intValue());
                    final double neVolt = Math.round(neVal * 100.0) / 100.0;
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gInstance.setLiveDataArray(7, String.valueOf(neVolt)+" V");
                            liveDataAdapter.notifyDataSetChanged();
                        }
                    });
                    return;
                }

                case Globals.SIGNAL_EARTH_LEAKAGE_CURRENT:{
                    String leakStr = data.substring(8);

                    Long i = Long.parseLong(leakStr, 16);
                    Float leakVal = Float.intBitsToFloat(i.intValue());
                    final double leakCurr = Math.round(leakVal * 100.0) / 100.0;
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gInstance.setLiveDataArray(8, String.valueOf(leakCurr)+" mA");
                            liveDataAdapter.notifyDataSetChanged();
                        }
                    });
                    return;
                }

                case Globals.SIGNAL_CHARGER_STATUS: {
                    String statusVal = data.substring(14);
                    final Integer status = Integer.parseInt(statusVal, 16);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            String stateText = "";
                            String command_text = "";
                            loadingPanel.setVisibility(View.GONE);
                            gun_command_image.setVisibility(View.VISIBLE);
                            gun_status_text.setTextColor(getResources().getColor(android.R.color.tab_indicator_text));
                            if (status == 1) {
                                stateText = "Idle";
                                command_text = "Connect Plug";
                                gun_command_image.setImageDrawable(getResources().getDrawable(R.drawable.start_charging));
                            } else if (status == 2) {
                                stateText = "Plug Connected";
                                command_text = "Start Charging";
                                gun_command_image.setImageDrawable(getResources().getDrawable(R.drawable.start_charging));
                            } else if (status == 3) {
                                stateText = "Preparing";
                                command_text = "Preparing";
                                gun_command_image.setImageDrawable(getResources().getDrawable(R.drawable.stop_charging));
                            } else if (status == 5) {
                                stateText = "Alarm";
                                command_text = "Alarm";
                                gun_status_text.setTextColor(getResources().getColor(R.color.colorRed));
                                gun_command_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_alarm));
                            } else if (status == 4) {
                                stateText = "Charging";
                                command_text = "Stop Charging";
                                gun_command_image.setImageDrawable(getResources().getDrawable(R.drawable.stop_charging));
                            } else if(status == 6){
                                command_text = "Done";
                                stateText = "Please Reconnect";
                                gun_command_image.setImageDrawable(getResources().getDrawable(R.drawable.start_charging));
                            } else if(status == 7){
                                command_text = "Paused";
                                stateText = "Charging Paused";
                                gun_command_image.setImageDrawable(getResources().getDrawable(R.drawable.stop_charging));
                            } else if(status == 8){
                                command_text = "Paused";
                                stateText = "EV Suspended Charging";
                                gun_command_image.setImageDrawable(getResources().getDrawable(R.drawable.stop_charging));
                            } else if(status == 9){
                                command_text = "Booting";
                                stateText = "Booting Up";
                                gun_command_image.setVisibility(View.GONE);
                                loadingPanel.setVisibility(View.VISIBLE);
                                gun_command_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_booting));
                            }
                            gun_command_text.setText(command_text);
                            gun_status_text.setText(stateText);
                            gInstance.setLiveDataArray(9, stateText);
                            liveDataAdapter.notifyDataSetChanged();
                        }
                    });
                    return;
                }

                case Globals.SIGNAL_CO2_STATUS: {
                    String co2Hex = data.substring(12);
                    final Integer co2Val = Math.round(Integer.parseInt(co2Hex, 16)/100);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gInstance.setLiveDataArray(2, String.valueOf(co2Val)+" Kg");
                            liveDataAdapter.notifyDataSetChanged();
                        }
                    });
                    return;
                }

                case Globals.SIGNAL_GET_PASSWORD: {
                    String passHex = data.substring(10);
                    if(passHex.equals(Globals.DEFAULT_PASS) || !passHex.equals(finalHexPass)){
                        changePass();
                    }else if(passHex.equals(finalHexPass)){
                        Calendar calendar = Calendar.getInstance();
                        String currentHour = Integer.toHexString(calendar.get(Calendar.HOUR_OF_DAY));
                        String currentMinute = Integer.toHexString(calendar.get(Calendar.MINUTE));
                        String second = Integer.toHexString(calendar.get(Calendar.SECOND));

                        String hexHour = ("00" + currentHour).substring(currentHour.length());
                        String hexMin = ("00"+currentMinute).substring(currentMinute.length());
                        String hexSec = ("00"+second).substring(second.length());
                        writeCommand(Globals.SET_RTC_TIME_PREFIX+hexSec+hexMin+hexHour);
                    }
                    return;
                }

                case Globals.SIGNAL_RTC_TIME :{
                    Calendar calendar = Calendar.getInstance();
                    String date = Integer.toHexString(calendar.get(Calendar.DAY_OF_MONTH));
                    String month = Integer.toHexString(calendar.get(Calendar.MONTH));
                    String year = Integer.toHexString(calendar.get(Calendar.YEAR) - 1900);

                    String hexDate = ("00"+date).substring(date.length());
                    String hexMonth = ("00"+month).substring(month.length());
                    String hexYear = ("00"+year).substring(year.length());
                    writeCommand(Globals.SET_RTC_DATE_PREFIX+hexDate+hexMonth+hexYear);
                    return;
                }

                case Globals.SIGNAL_RTC_DATE :{
                    get_controlData = true;
                    writeCommand(Globals.GET_AC_VOLTAGE);
                    return;
                }

                case Globals.SIGNAL_ALARM :{
                    //Right now using only last 4 chars, need to use last 8
                    Log.d("raw_alarm", data);
                    String alarmString = data.substring(12);
                    String binAlarm = Globals.hexToBinary(alarmString);
                    gInstance.setAlarmList(binAlarm);
                    liveAlarmListAdapter.notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    private void parseLogData(String data){
        if(data.length() == 16 && data.substring(0, 6).equals("10AC6F")){
            String signal = data.substring(4, 6);
            if(signal.equals(Globals.SIGNAL_ALARM_LOG_COUNT)){
                String countString = data.substring(8, 12);
                Integer countVal = Integer.parseInt(countString, 16);
                if(countVal > 50){
                    alarmLogCount = 50;
                }else{
                    alarmLogCount = countVal;
                }
                alarmCountVal = 1;
                alarmLogString = "";
                String overString = data.substring(12);
                Integer overFlag = Integer.parseInt(overString, 16);
                getting_alaramLogs = true;
                progressDialog.show();
                sendDisplayCommands(Globals.GET_ALARM_LOGS_PREFIX+"0001"+"0005");
            }
        }else if(get_logData && getting_alaramLogs){
            alarmLogString += data;
            if(alarmLogString.length() == 32*alarmLogCount || alarmLogCount == alarmCountVal){
                ArrayList<HashMap<String, String>> logPacket = new ArrayList<>();
                int index = 0;
                while (index < alarmLogString.length()){
                    String packet = alarmLogString.substring(index, index+32);
                    String hour = String.format("%02d", Integer.parseInt(packet.substring(2, 4), 16));
                    String min = String.format("%02d", Integer.parseInt(packet.substring(4, 6), 16));
                    String sec = String.format("%02d", Integer.parseInt(packet.substring(6, 8), 16));
                    String alarmString = packet.substring(26, 30);
                    String binAlarm = Globals.hexToBinary(alarmString);
                    String alarmName = gInstance.getLogAlarmItem(binAlarm);
                    String alarmType = packet.substring(8, 10);
                    String alarmFlag = "";
                    if(alarmType.equals("00")){
                        alarmFlag = "Clear";
                    }else if(alarmType.equals("01")){
                        alarmFlag = "Set";
                    }else if(alarmType.equals("2B")){
                        alarmFlag = "Reset";
                        alarmName = "Power Reset";
                    }

                    String year = String.valueOf(Integer.parseInt(packet.substring(12, 14), 16)+1900);
                    String month = String.format("%02d", Integer.parseInt(packet.substring(14, 16), 16)+1);
                    String day = String.format("%02d", Integer.parseInt(packet.substring(16, 18), 16));

                    HashMap<String, String> alarmLogItem = new HashMap<>();
                    alarmLogItem.put("time_date", hour+":"+min+":"+sec+" "+day+"-"+month+"-"+year);
                    alarmLogItem.put("alarmFlag", alarmFlag);
                    alarmLogItem.put("alarmName", alarmName);
                    logPacket.add(alarmLogItem);
                    index += 32;
                }
                gInstance.setAlarmLogArray(logPacket);
                alarmLogAdapter.notifyDataSetChanged();
                getting_alaramLogs = false;
                progressDialog.dismiss();
                //sendDisplayCommands(Globals.GET_SESSION_LOGS_COUNT);
            }else if(alarmLogString.length() % 32 == 0 && alarmLogCount > alarmCountVal){
                if(alarmCountVal == 1){
                    alarmCountVal += 4;
                }else {
                    alarmCountVal += 5;
                }
                Integer logCount = 5;
                if(alarmCountVal + logCount > alarmLogCount){
                    logCount = alarmLogCount - alarmCountVal;
                }
                String offSetHex = Integer.toHexString(alarmCountVal);
                String offSet = ("00"+offSetHex).substring(offSetHex.length());
                sendDisplayCommands(Globals.GET_ALARM_LOGS_PREFIX+"00"+offSet+"000"+String.valueOf(logCount));
            }
        }else if(data.length() == 16 && data.substring(0,6).equals("10AC69")){
            String signal = data.substring(4, 6);
            if(signal.equals(Globals.SIGNAL_SESSION_LOG_COUNT)){
                sessionLogString = "";
                String countString = data.substring(8, 12);
                Integer countVal = Integer.parseInt(countString, 16);
                if(countVal > 50){
                    sessionLogCount = 50;
                }else{
                    sessionLogCount = countVal;
                }
                sessionCountVal = 0;
                String overString = data.substring(12);
                Integer overFlag = Integer.parseInt(overString, 16);
                getting_sessionLogs = true;
                progressDialog.show();
                sendDisplayCommands(Globals.GET_SESSION_LOGS_PREFIX+"0000"+"0006");
            }
        }else if(get_logData && getting_sessionLogs){
            Log.d("session_log", data);
            sessionLogString += data;
            if(sessionLogString.length() >= 52*sessionLogCount || sessionLogCount == sessionCountVal){
                Log.d("final_sess_log", sessionLogString);
                ArrayList<HashMap<String, String>> logPacket = new ArrayList<>();
                int index = 0;
                while (index < sessionLogString.length()){
                    String packet = sessionLogString.substring(index, index+52);
                    String startHour = String.format("%02d", Integer.parseInt(packet.substring(2, 4), 16));
                    String startMin = String.format("%02d", Integer.parseInt(packet.substring(4, 6), 16));
                    String startSec = String.format("%02d", Integer.parseInt(packet.substring(6, 8), 16));
                    String startYear = String.valueOf(Integer.parseInt(packet.substring(12, 14), 16)+1900);
                    String startMonth = String.format("%02d", Integer.parseInt(packet.substring(14, 16), 16)+1);
                    String startDay = String.format("%02d", Integer.parseInt(packet.substring(16, 18), 16));

                    String stopHour = String.format("%02d", Integer.parseInt(packet.substring(22, 24), 16));
                    String stopMin = String.format("%02d", Integer.parseInt(packet.substring(24, 26), 16));
                    String stopSec = String.format("%02d", Integer.parseInt(packet.substring(26, 28), 16));
                    String stopYear = String.valueOf(Integer.parseInt(packet.substring(32, 34), 16)+1900);
                    String stopMonth = String.format("%02d", Integer.parseInt(packet.substring(34, 36), 16)+1);
                    String stopDay = String.format("%02d", Integer.parseInt(packet.substring(36, 38), 16));
                    String unitString = packet.substring(42, 50);
                    float unitKwh = Integer.parseInt(unitString, 16);
                    final double unitkWh = Math.round(unitKwh/100 * 100.00)/100.00;
                    HashMap<String, String> packetData = new HashMap<>();
                    packetData.put("start_time", startHour+":"+startMin+":"+startSec+" "+startDay+"-"+startMonth+"-"+startYear);
                    packetData.put("stop_time", stopHour+":"+stopMin+":"+stopSec+" "+stopDay+"-"+stopMonth+"-"+stopYear);
                    packetData.put("kwh_unit", String.valueOf(unitkWh));
                    logPacket.add(packetData);
                    index += 52;
                }
                gInstance.setSessionLogArray(logPacket);
                sessionLogAdapter.notifyDataSetChanged();
                getting_sessionLogs = false;
                progressDialog.dismiss();
            }else if(sessionLogString.length() % 52 == 0 && sessionCountVal < sessionLogCount){
                if(sessionCountVal == 0){
                    sessionCountVal += 6;
                }else{
                    sessionCountVal += 5;
                }
                Integer logCount = 5;
                if(sessionCountVal+logCount > sessionLogCount){
                    logCount = sessionLogCount - sessionCountVal;
                }
                String offsetHex = Integer.toHexString(sessionCountVal);
                String offset = ("00" + offsetHex).substring(offsetHex.length());
                sendDisplayCommands(Globals.GET_SESSION_LOGS_PREFIX + "00"+ offset + "000" + String.valueOf(logCount));
            }
        }
    }

    private void parseControlCommands(String command){
        switch (command){
            case Globals.GET_AC_VOLTAGE:{
                sendDisplayCommands(Globals.GET_CURRENT);
                return;
            }

            case Globals.GET_CURRENT:{
                sendDisplayCommands(Globals.GET_CO2_SAVED);
                return;
            }

            case Globals.GET_CO2_SAVED:{
                sendDisplayCommands(Globals.GET_SESSION_UNIT);
                return;
            }

            case Globals.GET_SESSION_UNIT:{
                Log.d("session_unit_called", Globals.GET_SESSION_UNIT);
                sendDisplayCommands(Globals.GET_SESSION_TIME);
                return;
            }

            case Globals.GET_SESSION_TIME:{
                sendDisplayCommands(Globals.GET_KWH);
                return;
            }

            case Globals.GET_KWH:{
                sendDisplayCommands(Globals.GET_TOTAL_TIME);
                return;
            }

            case Globals.GET_TOTAL_TIME:{
                sendDisplayCommands(Globals.GET_NE_VOLTAGE);
                return;
            }

            case Globals.GET_NE_VOLTAGE:{
                sendDisplayCommands(Globals.GET_EARTH_LEAKAGE_CURRENT);
                return;
            }

            case Globals.GET_EARTH_LEAKAGE_CURRENT:{
                sendDisplayCommands(Globals.GET_CHARGING_STATUS);
                return;
            }

            case Globals.GET_CHARGING_STATUS:{
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Handler handle = new Handler();
                        handle.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(get_controlData) {
                                    sendDisplayCommands(Globals.GET_AC_VOLTAGE);
                                }
                            }
                        }, 6000);
                    }
                });
                return;
            }
        }
    }

    protected void changePass(){
        writeCommand(Globals.SET_PASSWORD_PREFIX+finalHexPass);
    }

    protected void sendDisplayCommands(final String cmd){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Handler handle = new Handler();
                handle.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        writeCommand(cmd);
                    }
                }, 150);
            }
        });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkPermissions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT) {
            startBLEService();
        }else if(requestCode == REQUEST_LOCATION){
            checkIfLocationEnabled();
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        inMain = false;
        Log.d("main_activity", "stopped");
    }

    @Override
    public void onRestart(){
        super.onRestart();
        inMain = true;
        Log.d("main_activity", "restarted");
        showHomeData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(bluetoothGatt != null){
            bluetoothGatt.disconnect();
            bluetoothGatt = null;
        }
    }
}