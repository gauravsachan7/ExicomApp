package com.exicom.evcharger;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

public class SetConfiguration extends AppCompatActivity {
    ActionBar actionBar;
    LinearLayout config_ac_input_layout, config_server_layout, config_wifi_layout, config_system_layout, config_bluetooth_layout,
            config_protection_layout, config_rfid_layout, config_guest_access_layout, config_sw_update_layout, about_system, config_set_schedule_layout;
    BluetoothGatt bluetoothGatt;
    BluetoothManager btManager;
    private Handler mHandler;
    String viewFlag = "";

    ProgressDialog progressDialog = null;
    //Ac Input layout items
    EditText low_voltage_cut_off, low_volt_cutin_hys, high_volt_cut_off, high_volt_cutin_hys, rated_current, max_output_current, min_output_current;

    //About Items
    TextView card_serial_num, card_part_num, system_serial_num, system_part_num, application_version;
    String cardSerialNum = "", cardPartNum = "", systemSerialNum = "", systemPartNum = "", applicationVersion = "";

    //SW Update items
    String lastCommand = "";
    Integer bootRetry = 0, dataIndex = 0;
    Integer hexLength, hexRemainder;
    String softwareHexString;
    ImageView check_update;
    ImageView check_update_one;
    TextView current_version, update_text;
    Button update_btn;
    LinearLayout update_info;
    ArrayList<String> dataHexArr = new ArrayList<>();
    String hardware_version, software_version, software_revision, test_revision;

    //setWifiItems
    EditText wifi_ssid, wifi_password;
    Button btn_save_wifi;
    ArrayList<String> wifiSSID, wifiPassword;

    //server settings
    EditText server_port, server_ip, server_path, charger_id;
    Button btn_save_server;
    String ipVal, portVal, pathVal, chargerId;
    ArrayList<String> pathArr, idArr, ipArr;

    //schedule items
    EditText schedule_time, schedule_wattHour, duration_min;
    Button set_time_btn, set_kwh_btn, btn_set_appointment;
    TextView pick_date, pick_time;
    Integer selected_date, selected_month, selected_year, selected_hour, selected_minute;
    boolean appointBtn_clicked = false;
    String chargerPassword;

    //protection items
    Switch energyMeterAlarm, earthDiscAlarm, neVoltAlarm;
    EditText neVoltCutOffVal;
    ImageView save_ne_volt_cutOff;
    String alarmSettings = "";
    String alarmSettingBin = "";

    //System items
    Button btn_factory_reset;
    Switch rfid_auth_switch, mains_recovery, public_mode;
    String rfidAuthSet = "";

    CallWebService callWebService;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setconfiguration);
        actionBar = getSupportActionBar();
        Bundle bundle = getIntent().getExtras();
        mHandler = new Handler();
        if(bundle != null){
            if(bundle.getString("config_name").equals("...")){
                actionBar.setTitle("Schedule");
            }else{
                actionBar.setTitle(bundle.getString("config_name"));
            }
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(0);

            switch (bundle.getString("config_name")){
                case "AC Input":{
                    config_ac_input_layout = findViewById(R.id.config_ac_input_layout);
                    config_ac_input_layout.setVisibility(View.VISIBLE);
                    low_voltage_cut_off = findViewById(R.id.low_voltage_cut_off);
                    low_volt_cutin_hys = findViewById(R.id.low_volt_cutin_hys);
                    high_volt_cut_off = findViewById(R.id.high_volt_cut_off);
                    high_volt_cutin_hys = findViewById(R.id.high_volt_cutin_hys);
                    rated_current = findViewById(R.id.rated_current);
                    max_output_current = findViewById(R.id.max_output_current);
                    min_output_current = findViewById(R.id.min_output_current);
                    viewFlag = "AC_INPUT";
                    createBluetoothCommunication();
                    return;
                }

                case "Server":{
                    config_server_layout = findViewById(R.id.config_server_layout);
                    config_server_layout.setVisibility(View.VISIBLE);
                    server_ip = findViewById(R.id.server_ip);
                    server_port = findViewById(R.id.server_port);
                    server_path = findViewById(R.id.server_path);
                    charger_id = findViewById(R.id.charger_id);
                    btn_save_server = findViewById(R.id.btn_save_server);
                    viewFlag = "SERVER";
                    createBluetoothCommunication();
                    btn_save_server.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(btn_save_server.getText().toString().trim().equals("Save")){
                                saveServer();
                            }
                        }
                    });
                    return;
                }

                case "Wi-Fi":{
                    config_wifi_layout = findViewById(R.id.config_wifi_layout);
                    config_wifi_layout.setVisibility(View.VISIBLE);
                    wifi_ssid = findViewById(R.id.wifi_ssid);
                    wifi_password = findViewById(R.id.wifi_password);
                    btn_save_wifi = findViewById(R.id.btn_save_wifi);
                    viewFlag = "WIFI_SETUP";
                    createBluetoothCommunication();
                    btn_save_wifi.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(btn_save_wifi.getText().toString().trim().equals("Save")) {
                                saveWifi();
                            }
                        }
                    });
                    return;
                }

                case "System":{
                    config_system_layout = findViewById(R.id.config_system_layout);
                    config_system_layout.setVisibility(View.VISIBLE);
                    btn_factory_reset = findViewById(R.id.btn_factory_reset);
                    rfid_auth_switch = findViewById(R.id.rfid_auth_switch);
                    mains_recovery = findViewById(R.id.mains_recovery);
                    public_mode = findViewById(R.id.public_mode);
                    viewFlag = "SYSTEM";
                    createBluetoothCommunication();
                    rfid_auth_switch.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(rfid_auth_switch.isChecked()){
                                sendNextCommand(Globals.SET_RFID_AUTH_PREFIX+rfidAuthSet.substring(0, 6)+"01");
                            }else{
                                sendNextCommand(Globals.SET_RFID_AUTH_PREFIX+rfidAuthSet.substring(0, 6)+"00");
                            }
                        }
                    });

                    mains_recovery.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(mains_recovery.isChecked()){
                                sendNextCommand(Globals.SET_RFID_AUTH_PREFIX+rfidAuthSet.substring(0, 2)+"01"+rfidAuthSet.substring(4));
                            }else{
                                sendNextCommand(Globals.SET_RFID_AUTH_PREFIX+rfidAuthSet.substring(0, 2)+"00"+rfidAuthSet.substring(4));
                            }
                        }
                    });

                    btn_factory_reset.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendNextCommand(Globals.FACTORY_RESET_CMD);
                        }
                    });

                    public_mode.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(public_mode.isChecked()){
                                showPublicModeAlert();
                            }else{
                                new AlertDialog.Builder(SetConfiguration.this)
                                    .setTitle("Title")
                                    .setMessage("Once changed to Public Mode, cannot revert to Private Mode.")
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            public_mode.setChecked(true);
                                        }
                                    }).show();
                            }
                        }
                    });
                    return;
                }

                case "Bluetooth":{
                    config_bluetooth_layout = findViewById(R.id.config_bluetooth_layout);
                    config_bluetooth_layout.setVisibility(View.VISIBLE);
                    return;
                }

                case "Protection":{
                    config_protection_layout = findViewById(R.id.config_protection_layout);
                    config_protection_layout.setVisibility(View.VISIBLE);
                    energyMeterAlarm = findViewById(R.id.energyMeterAlarm);
                    earthDiscAlarm = findViewById(R.id.earthDiscAlarm);
                    neVoltAlarm = findViewById(R.id.neVoltAlarm);
                    neVoltCutOffVal = findViewById(R.id.neVoltCutOffVal);
                    save_ne_volt_cutOff = findViewById(R.id.save_ne_volt_cutOff);
                    viewFlag = "PROTECTION";
                    createBluetoothCommunication();

                    energyMeterAlarm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            StringBuilder myName = new StringBuilder(alarmSettingBin);

                            if(energyMeterAlarm.isChecked()) {
                                myName.setCharAt(8, '1');
                            }else{
                                myName.setCharAt(8, '0');
                            }
                            alarmSettingBin = myName.toString();
                            String tempHex = Globals.binaryToHex(alarmSettingBin);
                            String hexVal = ("0000"+tempHex).substring(tempHex.length());
                            String finalCommand = Globals.SET_ALARM_SETTINGS_PREFIX+alarmSettings.substring(8, 12)+hexVal;
                            sendNextCommand(finalCommand);
                        }
                    });
                    earthDiscAlarm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            StringBuilder myName = new StringBuilder(alarmSettingBin);

                            if(earthDiscAlarm.isChecked()) {
                                myName.setCharAt(11, '1');
                            }else{
                                myName.setCharAt(11, '0');
                            }
                            alarmSettingBin = myName.toString();
                            String tempHex = Globals.binaryToHex(alarmSettingBin);
                            String hexVal = ("0000"+tempHex).substring(tempHex.length());
                            String finalCommand = Globals.SET_ALARM_SETTINGS_PREFIX+alarmSettings.substring(8, 12)+hexVal;
                            sendNextCommand(finalCommand);
                        }
                    });
                    neVoltAlarm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            StringBuilder myName = new StringBuilder(alarmSettingBin);
                            if(neVoltAlarm.isChecked()) {
                                myName.setCharAt(3, '1');
                            }else{
                                myName.setCharAt(3, '0');
                            }
                            alarmSettingBin = myName.toString();
                            String tempHex = Globals.binaryToHex(alarmSettingBin);
                            String hexVal = ("0000"+tempHex).substring(tempHex.length());
                            String finalCommand = Globals.SET_ALARM_SETTINGS_PREFIX+alarmSettings.substring(8, 12)+hexVal;
                            sendNextCommand(finalCommand);
                        }
                    });

                    save_ne_volt_cutOff.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Float intVal = Float.parseFloat(neVoltCutOffVal.getText().toString());
                            if(intVal >= 1 && intVal <= 50){
                                String hexVal = String.format("%08X", Float.floatToRawIntBits(intVal));
                                writeCommand(Globals.SET_NE_VOLT_CUTOFF_PREFIX + hexVal);
                            }else{
                                Toast.makeText(SetConfiguration.this, "Value should be between 1 to 50", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    return;
                }

                case "RFID":{
                    config_rfid_layout = findViewById(R.id.config_rfid_layout);
                    config_rfid_layout.setVisibility(View.VISIBLE);
                    return;
                }

                case "SW Update":{
                    config_sw_update_layout = findViewById(R.id.config_sw_update_layout);
                    config_sw_update_layout.setVisibility(View.VISIBLE);
                    check_update = findViewById(R.id.check_update);
                    //check_update_one = findViewById(R.id.check_update_one);
                    current_version = findViewById(R.id.current_version);
                    update_text = findViewById(R.id.update_text);
                    update_btn = findViewById(R.id.update_btn);
                    update_info = findViewById(R.id.update_info);
                    viewFlag = "SW_UPDATE";
                    createBluetoothCommunication();
                    check_update.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkUpdate(Globals.GET_SOFTWARE);
                        }
                    });
//                    check_update_one.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            checkUpdate(Globals.GET_SOFTWARE_ONE);
//                        }
//                    });
                    update_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(update_btn.getText().toString().trim().equals("Update")) {
                                updateDevice();
                            }else{
                                Toast.makeText(SetConfiguration.this, "Update still in progress", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    return;
                }

                case "Info":{
                    about_system = findViewById(R.id.about_system);
                    about_system.setVisibility(View.VISIBLE);
                    card_serial_num = findViewById(R.id.card_serial_num);
                    card_part_num = findViewById(R.id.card_part_num);
                    system_serial_num = findViewById(R.id.system_serial_num);
                    system_part_num = findViewById(R.id.system_part_num);
                    application_version = findViewById(R.id.application_version);
                    viewFlag = "Info";
                    createBluetoothCommunication();
                    return;
                }

                case "...":{
                    config_set_schedule_layout = findViewById(R.id.config_set_schedule_layout);
                    config_set_schedule_layout.setVisibility(View.VISIBLE);
                    schedule_time = findViewById(R.id.schedule_time);
                    schedule_wattHour = findViewById(R.id.schedule_wattHour);
                    pick_time = findViewById(R.id.pick_time);
                    duration_min = findViewById(R.id.duration_min);
                    set_time_btn = findViewById(R.id.set_time_btn);
                    set_kwh_btn = findViewById(R.id.set_kwh_btn);
                    pick_date = findViewById(R.id.pick_date);
                    btn_set_appointment = findViewById(R.id.btn_set_appointment);
                    chargerPassword = bundle.getString("charger_password");
                    final DatePickerDialog.OnDateSetListener dateListen = new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear,
                                              int dayOfMonth) {
                            selected_date = dayOfMonth;
                            selected_month = monthOfYear;
                            selected_year = year-1900;
                            pick_date.setText(String.valueOf(dayOfMonth)+"/"+String.valueOf(monthOfYear+1)+"/"+String.valueOf(year));
                        }

                    };

                    pick_date.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Calendar calendar = Calendar.getInstance();
                            int date = calendar.get(Calendar.DAY_OF_MONTH);
                            int month = calendar.get(Calendar.MONTH);
                            int year = calendar.get(Calendar.YEAR);
                            DatePickerDialog datePickerDialog = new DatePickerDialog(SetConfiguration.this, dateListen, year, month, date);
                            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() + 86400000);
                            datePickerDialog.show();
                        }
                    });
                    pick_time.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Calendar calendar = Calendar.getInstance();
                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            int min = calendar.get(Calendar.MINUTE);
                            TimePickerDialog mTimePicker;
                            mTimePicker = new TimePickerDialog(SetConfiguration.this, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                    pick_time.setText( selectedHour + ":" + selectedMinute);
                                    selected_hour = selectedHour;
                                    selected_minute = selectedMinute;
                                }
                            }, hour, min, true);//Yes 24 hour time
                            mTimePicker.setTitle("Select Time");
                            mTimePicker.show();
                        }
                    });
                    viewFlag = "SET_SCHEDULE";

                    set_time_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            appointBtn_clicked = false;
                            setTimeCharging();
                        }
                    });

                    set_kwh_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            appointBtn_clicked = false;
                            setKwhCharging();
                        }
                    });

                    btn_set_appointment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            appointBtn_clicked = true;
                            setAppointment();
                        }
                    });
                    createBluetoothCommunication();
                    return;
                }
            }
        }
    }


    private void createBluetoothCommunication(){
        progressDialog = new ProgressDialog(SetConfiguration.this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.progressdialog);
        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        List<BluetoothDevice> bleDevices = btManager.getConnectedDevices(BluetoothProfile.GATT);
        Log.d("dev_count", String.valueOf(bleDevices.size()));
        bluetoothGatt = bleDevices.get(0).connectGatt(this, false, bleGattCallBack);
    }


    // Device connect call back
    private final BluetoothGattCallback bleGattCallBack = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            SetConfiguration.this.runOnUiThread(new Runnable() {
                public void run() {
                    String received = Globals.bytesToHex(characteristic.getValue());
                    Log.d("in_configuration",received);
                    if (viewFlag.equals("AC_INPUT")) {
                        if(received.length() == 16) {
                            parseACData(received);
                        }
                    }else if(viewFlag.equals("Info")){
                        if(received.length() == 16) {
                            parseAboutData(received);
                        }
                    }else if(viewFlag.equals("SW_UPDATE")){
                        parseSWUpdateData(received);
                    }else if(viewFlag.equals("WIFI_SETUP")){
                        parseWifiSetup(received);
                    }else if(viewFlag.equals("SERVER")){
                        parseServerData(received);
                    }else if(viewFlag.equals("SET_SCHEDULE")){
                        parseScheduleData(received);
                    }else if(viewFlag.equals("PROTECTION")){
                        parseProtectionData(received);
                    }else if (viewFlag.equals("SYSTEM")){
                        parseSystemData(received);
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

                    SetConfiguration.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(SetConfiguration.this, "Device Disconnected", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SetConfiguration.this, DeviceList.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });

                    break;
                case 2:
                    bluetoothGatt.discoverServices();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // this will get called after the client initiates a BluetoothGatt.discoverServices() call
            super.onServicesDiscovered(gatt, status);
            Log.d("service_discoverred", "found");
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
            if(status == BluetoothGatt.GATT_SUCCESS) {
                String data_written = Globals.bytesToHex(characteristic.getValue());
                if(viewFlag.equals("AC_INPUT")) {
                    parseACCommands(data_written);
                }else if(viewFlag.equals("Info")){
                    parseAboutCommands(data_written);
                }else if(viewFlag.equals("SW_UPDATE")){
                    Log.d("in_sw_update_cmd", data_written);
                    parseUpdateCommands(data_written);
                }else if(viewFlag.equals("WIFI_SETUP")){
                    Log.d("in_wifi_cmd", data_written);
                    parseWifiCommand(data_written);
                }else if(viewFlag.equals("SERVER")){
                    Log.d("in_server", data_written);
                    parseServerCommands(data_written);
                }else if(viewFlag.equals("SET_SCHEDULE")){
                    Log.d("set_schedule", data_written);
                    parseScheduleCommands(data_written);
                }else if(viewFlag.equals("PROTECTION")){
                    Log.d("protection", data_written);
                    parseProtectionCommands(data_written);
                }else if(viewFlag.equals("SYSTEM")){
                    Log.d("system", data_written);
                    parseSystemCommands(data_written);
                }
            }

        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);

        }
    };

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
                if(viewFlag.equals("AC_INPUT")) {
                    writeCommand(Globals.GET_LOW_VOLTAGE_CUTOFF);
                }else if(viewFlag.equals("Info")){
                    writeCommand(Globals.GET_CARD_SERIAL_NUMBER_01);
                }else if(viewFlag.equals("SW_UPDATE")){
                    if(progressDialog != null){
                        progressDialog.dismiss();
                    }
                    writeCommand(Globals.GET_COMPLETE_SYSTEM_VERSION);
                }else if(viewFlag.equals("WIFI_SETUP")){
                    writeCommand(Globals.GET_WIFI_SSID);
                }else if(viewFlag.equals("SERVER")){
                    writeCommand(Globals.GET_SERVER_IP);
                }else if(viewFlag.equals("SET_SCHEDULE")){
                    writeCommand(Globals.GET_CHARGE_KWH);
                }else if(viewFlag.equals("PROTECTION")){
                    writeCommand(Globals.GET_ALARM_SETTINGS);
                }else if(viewFlag.equals("SYSTEM")){
                    writeCommand(Globals.GET_RFID_AUTH);
                }
            }
        }, 1000);
    }

    public void writeCommand(final String cmd){
        try {
            byte[] strBytes = Globals.hexStringToByteArray(cmd);
            //byte[] strBytes = str.getBytes();
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
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void parseACData(String data){
        String signal = data.substring(4, 6);
        switch (signal){
            case Globals.SIGNAL_LOW_VOLT_CUTOFF:{
                String valString = data.substring(8);
                Long valLong = Long.parseLong(valString, 16);
                Float preVal = Float.intBitsToFloat(valLong.intValue());
                final double finalVal = Math.round(preVal * 100.0) / 100.0;
                SetConfiguration.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        low_voltage_cut_off.setText(String.valueOf(finalVal));
                    }
                });
                if(data.substring(6, 8).equals("01")){
                    Toast.makeText(this, "Low Voltage Cut-Off Set Successfully", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            case Globals.SIGNAL_LOW_VOLT_CUTIN_HYS:{
                String valString = data.substring(8);
                Long valLong = Long.parseLong(valString, 16);
                Float preVal = Float.intBitsToFloat(valLong.intValue());
                final double finalVal = Math.round(preVal * 100.0) / 100.0;
                SetConfiguration.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Float cutOff = Float.parseFloat(low_voltage_cut_off.getText().toString());
                        low_volt_cutin_hys.setText(String.valueOf(finalVal+cutOff));
                    }
                });
                return;
            }

            case Globals.SIGNAL_HIGH_VOLT_CUTOFF:{
                String valString = data.substring(8);
                Long valLong = Long.parseLong(valString, 16);
                Float preVal = Float.intBitsToFloat(valLong.intValue());
                final double finalVal = Math.round(preVal * 100.0) / 100.0;
                SetConfiguration.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        high_volt_cut_off.setText(String.valueOf(finalVal));
                    }
                });
                if(data.substring(6, 8).equals("01")){
                    Toast.makeText(this, "High Voltage Cut-Off Set Successfully", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            case Globals.SIGNAL_HIGH_VOLT_CUTIN_HYS:{
                String valString = data.substring(8);
                Long valLong = Long.parseLong(valString, 16);
                Float preVal = Float.intBitsToFloat(valLong.intValue());
                final double finalVal = Math.round(preVal * 100.0) / 100.0;
                SetConfiguration.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Float cutOff = Float.parseFloat(high_volt_cut_off.getText().toString());
                        high_volt_cutin_hys.setText(String.valueOf(cutOff-finalVal));
                    }
                });
                return;
            }

            case Globals.SIGNAL_RATED_CURRENT:{
                String valString = data.substring(8);
                Long valLong = Long.parseLong(valString, 16);
                Float preVal = Float.intBitsToFloat(valLong.intValue());
                final double finalVal = Math.round(preVal * 100.0) / 100.0;
                SetConfiguration.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rated_current.setText(String.valueOf(finalVal));
                    }
                });
                if(data.substring(6, 8).equals("01")){
                    Toast.makeText(this, "Rated Current Set Successfully", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            case Globals.SIGNAL_MAX_OUT_CURR_PERCENT:{
                String valString = data.substring(8);
                Long valLong = Long.parseLong(valString, 16);
                Float preVal = Float.intBitsToFloat(valLong.intValue());
                final double finalVal = Math.round(preVal * 100.0) / 100.0;
                SetConfiguration.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        max_output_current.setText(String.valueOf(finalVal));
                    }
                });
                if(data.substring(6, 8).equals("01")){
                    Toast.makeText(this, "Maximum Output Current Percent Set Successfully", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            case Globals.SIGNAL_MIN_OUT_CURRENT:{
                String valString = data.substring(8);
                Long valLong = Long.parseLong(valString, 16);
                Float preVal = Float.intBitsToFloat(valLong.intValue());
                final double finalVal = Math.round(preVal * 100.0) / 100.0;
                SetConfiguration.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        min_output_current.setText(String.valueOf(finalVal));
                    }
                });
                if(data.substring(6, 8).equals("01")){
                    Toast.makeText(this, "Minimum Output Current Set Successfully", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void parseACCommands(String command){
        switch (command){
            case Globals.GET_LOW_VOLTAGE_CUTOFF:{
                sendNextCommand(Globals.GET_LOW_VOLTAGE_CUTIN_HYS);
                return;
            }

            case Globals.GET_LOW_VOLTAGE_CUTIN_HYS:{
                sendNextCommand(Globals.GET_HIGH_VOLTAGE_CUTOFF);
                return;
            }

            case Globals.GET_HIGH_VOLTAGE_CUTOFF:{
                sendNextCommand(Globals.GET_HIGH_VOLTAGE_CUTIN_HYS);
                return;
            }

            case Globals.GET_HIGH_VOLTAGE_CUTIN_HYS:{
                sendNextCommand(Globals.GET_RATED_CURRENT);
                return;
            }

            case Globals.GET_RATED_CURRENT:{
                sendNextCommand(Globals.GET_MAX_OUTPUT_CURRENT_PERCENT);
                return;
            }

            case Globals.GET_MAX_OUTPUT_CURRENT_PERCENT:{
                sendNextCommand(Globals.GET_MIN_OUTPUT_CURRENT);
                if(progressDialog != null){
                    progressDialog.dismiss();
                }
                return;
            }
        }
    }

    private void parseAboutData(String data){
        String signal = data.substring(4, 6);
        switch (signal){
            case Globals.SIGNAL_CARD_SERIAL_NUMBER:{
                String serialCount = data.substring(6, 8);
                if(serialCount.equals("00")){
                    cardSerialNum = "";
                }
                String serialVal = Globals.hexToAscii(data.substring(8));
                cardSerialNum += serialVal;
                if(serialCount.equals("30")){
                    card_serial_num.setText(cardSerialNum);
                }
                return;
            }
            case Globals.SIGNAL_CARD_PART_NUMBER:{
                String serialCount = data.substring(6, 8);
                if(serialCount.equals("00")){
                    cardPartNum = "";
                }
                String serialVal = Globals.hexToAscii(data.substring(8));
                cardPartNum += serialVal;
                if(serialCount.equals("10")){
                    card_part_num.setText(cardPartNum);
                }
                return;
            }
            case Globals.SIGNAL_SYSTEM_SERIAL_NUMBER:{
                String serialCount = data.substring(6, 8);
                if(serialCount.equals("00")){
                    systemSerialNum = "";
                }
                String serialVal = Globals.hexToAscii(data.substring(8));
                systemSerialNum += serialVal;
                if(serialCount.equals("30")){
                    system_serial_num.setText(systemSerialNum);
                }
                return;
            }
            case Globals.SIGNAL_SYSTEM_PART_NUMBER:{
                String serialCount = data.substring(6, 8);
                if(serialCount.equals("00")){
                    systemPartNum = "";
                }
                String serialVal = Globals.hexToAscii(data.substring(8));
                systemPartNum += serialVal;
                if(serialCount.equals("10")){
                    system_part_num.setText(systemPartNum);
                }
                return;
            }
        }
    }

    private void parseAboutCommands(String command){
        switch (command){
            case Globals.GET_CARD_SERIAL_NUMBER_01:{
                sendNextCommand(Globals.GET_CARD_SERIAL_NUMBER_02);
                return;
            }
            case Globals.GET_CARD_SERIAL_NUMBER_02:{
                sendNextCommand(Globals.GET_CARD_SERIAL_NUMBER_03);
                return;
            }
            case Globals.GET_CARD_SERIAL_NUMBER_03:{
                sendNextCommand(Globals.GET_CARD_SERIAL_NUMBER_04);
                return;
            }
            case Globals.GET_CARD_SERIAL_NUMBER_04:{
                sendNextCommand(Globals.GET_CARD_PART_NUMBER_01);
                return;
            }
            case Globals.GET_CARD_PART_NUMBER_01:{
                sendNextCommand(Globals.GET_CARD_PART_NUMBER_02);
                return;
            }
            case Globals.GET_CARD_PART_NUMBER_02:{
                sendNextCommand(Globals.GET_SYSTEM_SERIAL_NUMBER_01);
                return;
            }
            case Globals.GET_SYSTEM_SERIAL_NUMBER_01:{
                sendNextCommand(Globals.GET_SYSTEM_SERIAL_NUMBER_02);
                return;
            }
            case Globals.GET_SYSTEM_SERIAL_NUMBER_02:{
                sendNextCommand(Globals.GET_SYSTEM_SERIAL_NUMBER_03);
                return;
            }
            case Globals.GET_SYSTEM_SERIAL_NUMBER_03:{
                sendNextCommand(Globals.GET_SYSTEM_SERIAL_NUMBER_04);
                return;
            }
            case Globals.GET_SYSTEM_SERIAL_NUMBER_04:{
                sendNextCommand(Globals.GET_SYSTEM_PART_NUMBER_01);
                return;
            }
            case Globals.GET_SYSTEM_PART_NUMBER_01:{
                sendNextCommand(Globals.GET_SYSTEM_PART_NUMBER_02);
                return;
            }
            case Globals.GET_SYSTEM_PART_NUMBER_02:{
                sendNextCommand(Globals.GET_COMPLETE_SYSTEM_VERSION);
                return;
            }
            case Globals.GET_COMPLETE_SYSTEM_VERSION:{
                if(progressDialog != null){
                    progressDialog.dismiss();
                }
                return;
            }
        }
    }


    private void sendNextCommand(final String command){
        SetConfiguration.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Handler handle = new Handler();
                handle.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        writeCommand(command);
                    }
                }, 200);
            }
        });
    }

    public void saveAcConfigClicked(View v){
        int id = v.getId();
        Log.d("clicked_id", String.valueOf(id)+","+String.valueOf(R.id.btn_save_low_cut_off));
        switch (id){
            case R.id.btn_save_low_cut_off:{
                String val = low_voltage_cut_off.getText().toString();
                Float intVal = Float.parseFloat(val);
                if(Math.round(intVal) >= Globals.MIN_LOW_VOLT_CUTOFF_VAL && Math.round(intVal) <= Globals.MAX_LOW_VOLT_CUTOFF_VAL) {
                    String hexVal = String.format("%08X", Float.floatToRawIntBits(intVal));
                    writeCommand(Globals.SET_LOW_VOLTAGE_CUTOFF + hexVal);
                }else {
                    Toast.makeText(this, "Value should be between "+String.valueOf(Globals.MIN_LOW_VOLT_CUTOFF_VAL)+" and "+String.valueOf(Globals.MAX_LOW_VOLT_CUTOFF_VAL), Toast.LENGTH_LONG).show();
                }
                return;
            }

            case R.id.save_high_cut_off_volt:{
                String val = high_volt_cut_off.getText().toString();
                Float intVal = Float.parseFloat(val);
                if(Math.round(intVal) >= Globals.MIN_HIGH_VOLT_CUTOFF && Math.round(intVal) <= Globals.MAX_HIGH_VOLT_CUTOFF) {
                    String hexVal = String.format("%08X", Float.floatToRawIntBits(intVal));
                    writeCommand(Globals.SET_HIGH_VOLTAGE_CUTOFF + hexVal);
                }else {
                    Toast.makeText(this, "Value should be between "+String.valueOf(Globals.MIN_HIGH_VOLT_CUTOFF)+" and "+String.valueOf(Globals.MAX_HIGH_VOLT_CUTOFF), Toast.LENGTH_LONG).show();
                }
                return;
            }

            case R.id.save_rated_current:{
                String val = rated_current.getText().toString();
                Float intVal = Float.parseFloat(val);
                if(Math.round(intVal) >= Globals.MIN_RATED_CURRENT && Math.round(intVal) <= Globals.MAX_RATED_CURRENT) {
                    String hexVal = String.format("%08X", Float.floatToRawIntBits(intVal));
                    writeCommand(Globals.SET_RATED_CURRENT + hexVal);
                }else {
                    Toast.makeText(this, "Value should be between "+String.valueOf(Globals.MIN_RATED_CURRENT)+" and "+String.valueOf(Globals.MAX_RATED_CURRENT), Toast.LENGTH_LONG).show();
                }
                return;
            }

            case R.id.save_max_out_current:{
                String val = max_output_current.getText().toString();
                Float intVal = Float.parseFloat(val);
                if(Math.round(intVal) >= Globals.MIN_OUTPUT_CURRENT_PERCENT && Math.round(intVal) <= Globals.MAX_OUTPUT_CURRENT_PERCENT) {
                    String hexVal = String.format("%08X", Float.floatToRawIntBits(intVal));
                    writeCommand(Globals.SET_MAX_OUTPUT_CURRENT + hexVal);
                }else {
                    Toast.makeText(this, "Value should be between "+String.valueOf(Globals.MIN_OUTPUT_CURRENT_PERCENT)+" and "+String.valueOf(Globals.MAX_OUTPUT_CURRENT_PERCENT), Toast.LENGTH_LONG).show();
                }
                return;
            }

            case R.id.save_min_out_current:{
                String val = min_output_current.getText().toString();
                Float intVal = Float.parseFloat(val);
                if(Math.round(intVal) >= Globals.MAX_MIN_OUTPUT_CURRENT && Math.round(intVal) <= Globals.MIN_MIN_OUTPUT_CURRENT) {
                    String hexVal = String.format("%08X", Float.floatToRawIntBits(intVal));
                    writeCommand(Globals.SET_MIN_OUTPUT_CURRENT + hexVal);
                }else {
                    Toast.makeText(this, "Value should be between "+String.valueOf(Globals.MAX_MIN_OUTPUT_CURRENT)+" and "+String.valueOf(Globals.MIN_MIN_OUTPUT_CURRENT), Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void parseUpdateCommands(String data_written){
        if(data_written.equals("CC") && lastCommand.equals("getstatus_after_download")){
            dataIndex = 0;
            lastCommand = "first_data_packet_send";
            String dataPacket = dataHexArr.get(dataIndex);
            dataIndex++;
            String command = fillData(Globals.SEND_DATA_COMMAND+dataPacket);
            sendNextCommand(command);
        }else if(data_written.equals("CC") && lastCommand.equals("getstatus_after_sendData")){
            lastCommand = "reset_after_getstatus";
            String command = fillData(Globals.RESET_COMMAND);
            sendNextCommand(command);
        }else if (data_written.equals(Globals.ENTER_CARD_TO_BOOT_MODE)) {
            String command = fillData(Globals.PING_COMMAND);
            lastCommand = "ping_after_boot";
            sendNextCommand(command);
            bootRetry = 0;
        }
    }

    private void parseSWUpdateData(String data){
        Log.d("version_data", data);
        if(data.length() == 16){

            String signal = data.substring(4, 6);
            switch (signal) {
                case Globals.SIGNAL_COMPLETE_SYSTEM_VERSION: {
                    hardware_version = String.valueOf(Integer.parseInt(data.substring(8, 10), 16));
                    software_version = String.valueOf(Integer.parseInt(data.substring(10, 12), 16));
                    software_revision = String.valueOf(Integer.parseInt(data.substring(12, 14), 16));
                    test_revision = String.valueOf(Integer.parseInt(data.substring(14), 16));
                    current_version.setText(hardware_version + "." + software_version + "." + software_revision+"."+test_revision);
                    return;
                }

                case Globals.SIGNAL_BOOT_MODE: {

                    return;
                }
            }

        }else if(data.equals("00CC034040") && lastCommand.equals("getstatus_after_download")){
            writeCommand("CC");
        }else if(data.equals("00CC034040") && lastCommand.equals("getstatus_after_sendData")){
            writeCommand("CC");
        }else {
           if(data.equals(Globals.ACK_COMMAND)){
               if(lastCommand.equals("ping_after_boot")) {
                   Log.d("lastCommand", lastCommand);
                   //prepare download command and send
                   String hexLength = Integer.toHexString(softwareHexString.length() / 2);
                   String softwareLength = ("00000000" + hexLength).substring(hexLength.length());
                   String command = fillData(Globals.DOWNLOAD_COMMAND + Globals.START_ADDRESS + softwareLength);
                   lastCommand = "download_after_ping";
                   writeCommand(command);
               }else if(lastCommand.equals("download_after_ping")){
                   String command = fillData(Globals.GET_STATUS_COMMAND);
                   lastCommand = "getstatus_after_download";
                   dataIndex = 0;
                   writeCommand(command);
               }else if(lastCommand.equals("first_data_packet_send")){
                   if(dataIndex < dataHexArr.size()){
                       String dataPacket = dataHexArr.get(dataIndex);
                       dataIndex ++;
                       SetConfiguration.this.runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               update_text.setText("Updating...: "+String.valueOf((dataIndex*100)/hexLength)+"%");
                           }
                       });
                       String command = fillData(Globals.SEND_DATA_COMMAND+dataPacket);
                       writeCommand(command);

                   }else if(dataIndex == dataHexArr.size()){
                       String command = fillData(Globals.GET_STATUS_COMMAND);
                       lastCommand = "getstatus_after_sendData";
                       writeCommand(command);
                   }
               }else if(lastCommand.equals("reset_after_getstatus")){
                   Toast.makeText(SetConfiguration.this, "Software Updated Successfully", Toast.LENGTH_LONG).show();
                   finish();
               }
           }else if(data.equals(Globals.NACK_COMMAND)){
               update_text.setText("Update Failed please retry");
               update_btn.setText("Update");
           }
        }
    }

    private void checkUpdate(final String urlTag){
        callWebService = (CallWebService) new CallWebService(SetConfiguration.this, "{}", urlTag, new CallWebService.DataReceivedListener() {
            @Override
            public void onDataReceived(String data) {
                if(data != null && data.length() > 5){
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        String dataObject = jsonObject.getString("data");
                        JSONArray jsonArray = new JSONArray(dataObject);
                        String softData = jsonArray.get(0).toString();
                        JSONObject softObject = new JSONObject(softData);
                        softwareHexString = softObject.getString("software_content");
                        String versionNumber = softObject.getString("version_number");
                        String[] separated = versionNumber.split(Pattern.quote("."));
                       // Log.d("values_are", separated[0]+","+separated[1]+","+separated[2]+","+separated[3]);
                        if (separated.length == 4) {
                            if(hardware_version.equals(separated[0]) && software_version.equals(separated[1])) {
                                if (hardware_version.equals(separated[0]) &&
                                        software_version.equals(separated[1]) && (!software_revision.equals(separated[2]) || !test_revision.equals(separated[3]))) {
                                    update_info.setVisibility(View.VISIBLE);
                                    update_text.setText("Update Available: " + versionNumber);
                                } else {
                                    Toast.makeText(SetConfiguration.this, "Charger have updated software", Toast.LENGTH_LONG).show();
                                }
                            }else if(urlTag.equals(Globals.GET_SOFTWARE)){
                                checkUpdate(Globals.GET_SOFTWARE_ONE);
                            }else{
                                Toast.makeText(SetConfiguration.this, "No update available", Toast.LENGTH_LONG).show();
                            }
                        }else if(urlTag.equals(Globals.GET_SOFTWARE_ONE)){
                            Toast.makeText(SetConfiguration.this, "Software version is invalid", Toast.LENGTH_LONG).show();
                        }else if(urlTag.equals(Globals.GET_SOFTWARE)){
                            checkUpdate(Globals.GET_SOFTWARE_ONE);
                        }

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }

            }
        }).execute();
    }

    private void updateDevice(){
        double hexSize = softwareHexString.length();
        hexRemainder = (int) hexSize % 128;
        hexLength = (int)(hexSize/128);
        dataHexArr = new ArrayList<>();
        int index = 0;
        while(index < softwareHexString.length()){
            if(softwareHexString.substring(index).length() >= 128){
                dataHexArr.add(softwareHexString.substring(index, index+128));
                index += 128;
            }else if(softwareHexString.substring(index).length() >= 8){
                dataHexArr.add(softwareHexString.substring(index, index+8));
                index +=8;
            }
        }
        update_btn.setText("Updating...");
        writeCommand(Globals.ENTER_CARD_TO_BOOT_MODE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(bluetoothGatt != null){
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    private String fillData(String commandData){
        Integer sizeOfData = (commandData.length()/2)+2;
        String hexSize = ("00"+Integer.toHexString(sizeOfData)).substring(Integer.toHexString(sizeOfData).length());
        String crc = getCheckSum(commandData);
        String finalData = (hexSize+crc+commandData).toUpperCase();
        Log.d("final_command", finalData);
        return finalData;
    }

    private String getCheckSum(String checkData){
        byte[] strBytes = Globals.hexStringToByteArray(checkData);
        byte sum = 0;
        for(int i = 0; i < strBytes.length; i++){
            sum += strBytes[i];
        }
        String checkSumHex = Globals.byteToHex(sum);
        Log.d("check_sum", checkSumHex);
        return checkSumHex;
    }

    private void parseWifiCommand(String data_written){
        if(data_written.equals(Globals.GET_WIFI_SSID)){
            sendNextCommand(Globals.GET_WIFI_PASSWORD);
        }
    }

    private void parseWifiSetup(String data){

        String settingPrefix = data.substring(0, 7);
        if(settingPrefix.equals(Globals.SET_WIFI_SSID_PREFIX) && !data.substring(6, 8).equals("00")){
            Integer index = Integer.parseInt(data.substring(7, 8), 16);
            if(index < 8){
                sendNextCommand(Globals.SET_WIFI_SSID_PREFIX+String.valueOf(index+1)+wifiSSID.get(index));
            } else {
                sendNextCommand(Globals.SET_WIFI_PASS_PREFIX+"1"+wifiPassword.get(0));
            }
        }else if(settingPrefix.equals(Globals.SET_WIFI_PASS_PREFIX) && !data.substring(6, 8).equals("00")){
            Integer passIndex = Integer.parseInt(data.substring(7, 8), 16);
            if(passIndex < 8){
                sendNextCommand(Globals.SET_WIFI_PASS_PREFIX+String.valueOf(passIndex+1)+wifiPassword.get(passIndex));
            }else {
                progressDialog.dismiss();
                Toast.makeText(SetConfiguration.this, "Wifi Settings Updated Successfully", Toast.LENGTH_LONG).show();
                SetConfiguration.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn_save_wifi.setText("Save");
                    }
                });
            }

        }else {
            String signal = data.substring(4, 6);
            switch (signal) {
                case Globals.SIGNAL_WIFI_SSID: {
                    if (data.substring(6, 8).equals("00")) {
                        final String wifiName = Globals.hexToAscii(data.substring(8));
                        SetConfiguration.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                wifi_ssid.setText(wifiName.trim());
                            }
                        });
                    }
                    return;
                }

                case Globals.SIGNAL_WIFI_PASSWORD: {
                    if (data.substring(6, 8).equals("00")) {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        final String wifiPass = Globals.hexToAscii(data.substring(8));
                        SetConfiguration.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                wifi_password.setText(wifiPass.trim());
                            }
                        });
                    }
                    return;
                }
            }
        }
    }

    private void saveWifi(){
        String ssidHex = Globals.asciiToHex(wifi_ssid.getText().toString()).trim();
        String passHex = Globals.asciiToHex(wifi_password.getText().toString()).trim();
        if(ssidHex.length() > 0 && passHex.length() > 0) {
            if (ssidHex.length() > 64) {
                Toast.makeText(SetConfiguration.this, "SSID length is too long", Toast.LENGTH_LONG).show();
            } else if (passHex.length() > 64) {
                Toast.makeText(SetConfiguration.this, "Password length is too long", Toast.LENGTH_LONG).show();
            } else {
                progressDialog.show();
                String ssidHexFinal = (ssidHex + "0000000000000000000000000000000000000000000000000000000000000000").substring(0, 64);
                String passHexFinal = (passHex + "0000000000000000000000000000000000000000000000000000000000000000").substring(0, 64);
                int index = 0;
                wifiSSID = new ArrayList<>();
                while (index < ssidHexFinal.length()) {
                    wifiSSID.add(ssidHexFinal.substring(index, index + 8));
                    index += 8;
                }

                Log.d("ssid", String.valueOf(wifiSSID));

                index = 0;
                wifiPassword = new ArrayList<>();
                while (index < passHexFinal.length()) {
                    wifiPassword.add(passHexFinal.substring(index, index + 8));
                    index += 8;
                }
                btn_save_wifi.setText("Saving...");
                writeCommand(Globals.SET_WIFI_SSID_PREFIX + "1" + wifiSSID.get(0));
                Log.d("password", String.valueOf(wifiPassword));
            }
        }else{
            Toast.makeText(SetConfiguration.this, "SSID or Password cannot be empty", Toast.LENGTH_LONG).show();
        }
    }

    private void parseServerCommands(String command){
        switch (command) {
            case Globals.GET_SERVER_IP:
                sendNextCommand(Globals.GET_SERVER_PORT);
                break;
            case Globals.GET_SERVER_PORT:
                sendNextCommand(Globals.GET_SERVER_PATH);
                break;
            case Globals.GET_SERVER_PATH:
                sendNextCommand(Globals.GET_CHARGER_ID);
                break;
            case Globals.GET_CHARGER_ID:
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                break;
        }
    }

    private void parseServerData(String data){
        String data_prefix = data.substring(0, 6);
        if(data_prefix.equals(Globals.SET_SERVER_PORT_PREFIX) && !data.substring(6, 8).equals("00")){
            String hexIp = Globals.asciiToHex(ipVal);
            String completeVal = (hexIp+"00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000").substring(0, 128);
            Integer ipIndex = 0;
            ipArr = new ArrayList<>();
            while (ipIndex < completeVal.length()){
                ipArr.add(completeVal.substring(ipIndex, ipIndex+8));
                ipIndex += 8;
            }
            writeCommand(Globals.SET_SERVER_IP_PREFIX+"01"+ipArr.get(0));

        }else if(data_prefix.equals(Globals.SET_SERVER_IP_PREFIX) && !data.substring(6, 8).equals("00")){
            Integer pathIndex = Integer.parseInt(data.substring(6,8), 16);
            if(pathIndex < 16){
                String hexIndex = Integer.toHexString(pathIndex+1);
                String hexIn = ("00"+hexIndex).substring(hexIndex.length());
                writeCommand(Globals.SET_SERVER_IP_PREFIX+hexIn+ipArr.get(pathIndex));
            }else {
                String hexPath = Globals.asciiToHex(pathVal);
                String path_val = (hexPath + "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000").substring(0, 128);
                Integer index = 0;
                pathArr = new ArrayList<>();
                while (index < path_val.length()) {
                    pathArr.add(path_val.substring(index, index + 8));
                    index += 8;
                }
                writeCommand(Globals.SET_SERVER_PATH_PREFIX + "01" + pathArr.get(0));
            }
        }else if(data_prefix.equals(Globals.SET_SERVER_PATH_PREFIX) && !data.substring(6, 8).equals("00")){
            Integer pathIndex = Integer.parseInt(data.substring(6,8), 16);
            if(pathIndex < 16) {
                String pathHexIndex = Integer.toHexString(pathIndex+1);
                String hexIndex = ("00"+pathHexIndex).substring(pathHexIndex.length());
                writeCommand(Globals.SET_SERVER_PATH_PREFIX + hexIndex + pathArr.get(pathIndex));
            }else{
                String hexId = Globals.asciiToHex(chargerId);
                String id_val = (hexId+"0000000000000000000000000000000000000000000000000000000000000000").substring(0, 64);
                Integer idIndex = 0;
                idArr = new ArrayList<>();
                while (idIndex < id_val.length()){
                    idArr.add(id_val.substring(idIndex, idIndex+8));
                    idIndex += 8;
                }
                writeCommand(Globals.SET_CHARGER_ID_PREFIX+"01"+idArr.get(0));
            }
        }else if(data_prefix.equals(Globals.SET_CHARGER_ID_PREFIX) && !data.substring(6, 8).equals("00")){
            Integer setIdIndex = Integer.parseInt(data.substring(6, 8), 16);
            if(setIdIndex < 8){
                String setIdHex = Integer.toHexString(setIdIndex+1);
                String setIdHexIndex = ("00"+setIdHex).substring(setIdHex.length());
                writeCommand(Globals.SET_CHARGER_ID_PREFIX + setIdHexIndex + idArr.get(setIdIndex));
            }else{
                progressDialog.dismiss();
                Toast.makeText(SetConfiguration.this, "Server Settings Updated Successfully", Toast.LENGTH_LONG).show();
            }
        }else {
            String signal = data.substring(4, 6);
            switch (signal) {
                case Globals.SIGNAL_SERVER_IP: {
                    Log.d("ser_ip_hex", data.substring(8));
                    final String ip = Globals.hexToAscii(data.substring(8));
                    Log.d("ser_ip_ascii", ip);
                    SetConfiguration.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            server_ip.setText(ip.trim());
                        }
                    });
                    break;
                }

                case Globals.SIGNAL_SERVER_PORT: {
                    final String port = String.valueOf(Integer.parseInt(data.substring(12), 16));
                    SetConfiguration.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            server_port.setText(port.trim());
                        }
                    });
                    break;
                }

                case Globals.SIGNAL_SERVER_PATH: {
                    final String path = Globals.hexToAscii(data.substring(8)).trim();
                    SetConfiguration.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            server_path.setText(path);
                        }
                    });
                    break;
                }

                case Globals.SIGNAL_CHARGER_ID:{
                    final String chargerid = Globals.hexToAscii(data.substring(8)).trim();
                    SetConfiguration.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            charger_id.setText(chargerid);
                        }
                    });
                    break;
                }
            }
        }
    }

    private void saveServer(){
        ipVal = server_ip.getText().toString().trim();
        portVal = server_port.getText().toString().trim();
        pathVal = server_path.getText().toString().trim();
        chargerId = charger_id.getText().toString().trim();
        if(validate(server_ip) && validate(server_port) && validate(server_path) && validate(server_ip)){
            progressDialog.show();
            String hexPort = Integer.toHexString(Integer.parseInt(portVal));
            String serverPort = ("00000000"+hexPort).substring(hexPort.length());
            writeCommand(Globals.SET_SERVER_PORT_PREFIX+"01"+serverPort);
        }
    }


    private boolean validate(EditText editText){
        if(editText.getText().toString().trim().length() == 0){
            editText.setError("Required");
            return false;
        }
        return true;
    }


    private void parseScheduleCommands(String command){
        switch (command){
            case Globals.GET_CHARGE_KWH:{
                sendNextCommand(Globals.GET_CHARGE_TIME);
                return;
            }
            case Globals.GET_CHARGE_TIME:{
                sendNextCommand(Globals.GET_APPOINTMENT_CHARGE_DATE);
                return;
            }
            case Globals.GET_APPOINTMENT_CHARGE_DATE:{
                sendNextCommand(Globals.GET_APPOINTMENT_CHARGE_TIME);
                return;
            }
            case Globals.GET_APPOINTMENT_CHARGE_TIME:{
                if(progressDialog != null){
                    progressDialog.dismiss();
                }
            }
        }
    }

    private void parseScheduleData(String data){
        if(data.contains(Globals.SET_CHARGE_KWH_PREFIX)){
            writeCommand(Globals.START_CHARGING+chargerPassword);
            Toast.makeText(SetConfiguration.this, "KWH set successfully", Toast.LENGTH_LONG).show();
        }else if(data.contains(Globals.SET_CHARGE_TIME_PREFIX)) {
            if(appointBtn_clicked){
                String hexMin = Integer.toHexString(selected_minute);
                String finalHexMin = ("00"+hexMin).substring(hexMin.length());
                String hexHour = Integer.toHexString(selected_hour);
                String finalHexHour = ("00"+hexHour).substring(hexHour.length());
                writeCommand(Globals.SET_APPOINTMENT_CHARGE_TIME_PREFIX+finalHexMin+finalHexHour);
            }else{
                writeCommand(Globals.START_CHARGING+chargerPassword);
                Toast.makeText(SetConfiguration.this, "Timer set successfully", Toast.LENGTH_LONG).show();
            }
        }else if(data.contains(Globals.SET_APPOINTMENT_CHARGE_TIME_PREFIX)){
            String hexDate = Integer.toHexString(selected_date);
            String finalHexDate = ("00"+hexDate).substring(hexDate.length()).toUpperCase();
            String hexMonth = Integer.toHexString(selected_month);
            String finalHexMonth = ("00"+hexMonth).substring(hexMonth.length()).toUpperCase();
            String hexYear = Integer.toHexString(selected_year);
            String finalHexYear = ("00"+hexYear).substring(hexYear.length()).toUpperCase();
            writeCommand(Globals.SET_APPOINTMENT_CHARGE_DATE_PREFIX+finalHexDate+finalHexMonth+finalHexYear);
        }else if(data.contains(Globals.SET_APPOINTMENT_CHARGE_DATE_PREFIX)){
            writeCommand(Globals.ENABLE_APPOINTMENT);
        }else if(data.equals(Globals.ENABLE_APPOINTMENT)){
            Toast.makeText(SetConfiguration.this, "Appointment Set Successfully", Toast.LENGTH_LONG).show();
            finish();
        }else if(data.equals(Globals.START_CHARGING+chargerPassword)){
            finish();
        }else{
            String signal = data.substring(4, 6);
            switch (signal) {
                case Globals.SIGNAL_CHARGE_KWH: {
                    String valString = data.substring(8);
                    Integer chargeKwh = Math.round(Integer.parseInt(valString, 16)/100);
                    schedule_wattHour.setText(String.valueOf(chargeKwh));
                    return;
                }

                case Globals.SIGNAL_CHARGE_TIME: {
                    String valString = data.substring(12);
                    Integer chargeTime = Integer.parseInt(valString, 16);
                    schedule_time.setText(String.valueOf(chargeTime));
                    return;
                }
            }
        }
    }

    private void setTimeCharging(){
        Integer time = Integer.parseInt(schedule_time.getText().toString());
        String hexTime = Integer.toHexString(time);
        String finalHexTime = ("0000"+hexTime).substring(hexTime.length());
        writeCommand(Globals.SET_CHARGE_TIME_PREFIX+finalHexTime);
    }

    private void setKwhCharging(){
        Integer watt = Integer.parseInt(schedule_wattHour.getText().toString());
        if (watt >= 1) {
            String hexWatt = Integer.toHexString(watt * 100);
            String finalHexWatt = ("00000000" + hexWatt).substring(hexWatt.length());
            writeCommand(Globals.SET_CHARGE_KWH_PREFIX + finalHexWatt);
        }else{
            Toast.makeText(SetConfiguration.this, "Min value of KWH should be 1", Toast.LENGTH_LONG).show();
        }
    }

    private void setAppointment(){
        Integer time = Integer.parseInt(duration_min.getText().toString());
        String hexTime = Integer.toHexString(time);
        String finalHexTime = ("0000"+hexTime).substring(hexTime.length());
        writeCommand(Globals.SET_CHARGE_TIME_PREFIX+finalHexTime);
    }


    private void parseProtectionCommands(String command){
        if(command.equals(Globals.GET_ALARM_SETTINGS)){
            sendNextCommand(Globals.GET_NE_VOLTAGE_CUTOFF);
            if(progressDialog != null){
                progressDialog.dismiss();
            }
        }
    }

    private void parseProtectionData(String data){
        if(data.length() == 16){
            String signal = data.substring(4, 6);
            switch (signal){
                case Globals.SIGNAL_ALARM_SETTING:
                    alarmSettings = data;
                    String alarmSet = data.substring(12);
                    String binAlarm = Globals.hexToBinary(alarmSet);
                    Log.d("alarm_set", data);
                    alarmSettingBin = binAlarm;
                    String neStatus = binAlarm.substring(3, 4);
                    String meterAlarm = binAlarm.substring(8, 9);
                    String earthAlarm = binAlarm.substring(11, 12);
                    if(neStatus.equals("1")){
                        neVoltAlarm.setChecked(true);
                    }else{
                        neVoltAlarm.setChecked(false);
                    }

                    if(meterAlarm.equals("1")){
                        energyMeterAlarm.setChecked(true);
                    }else{
                        energyMeterAlarm.setChecked(false);
                    }

                    if(earthAlarm.equals("1")){
                        earthDiscAlarm.setChecked(true);
                    }else{
                        earthDiscAlarm.setChecked(false);
                    }
                    if(data.substring(6, 8).equals("01")){
                        Toast.makeText(SetConfiguration.this, "Alaram setting applied successfully", Toast.LENGTH_LONG).show();
                    }

                    return;

                case Globals.SIGNAL_NE_CUTOFF:
                    String neHex = data.substring(8);
                    Long i = Long.parseLong(neHex, 16);
                    final Float neVal = Float.intBitsToFloat(i.intValue());
                    if(data.substring(6, 8).equals("01")){
                        Toast.makeText(SetConfiguration.this, "N-E Voltage Cut-Off set successfully", Toast.LENGTH_LONG).show();
                    }
                    SetConfiguration.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            neVoltCutOffVal.setText(String.valueOf(neVal));
                        }
                    });
                    return;
            }
        }
    }

    private void showPublicModeAlert(){
        new AlertDialog.Builder(SetConfiguration.this)
                .setTitle("Title")
                .setMessage("Once changed to Public Mode, cannot revert to Private Mode.Do you want to continue??")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        writeCommand(Globals.SET_PUBLIC_MODE_PREFIX+"02");
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        public_mode.setChecked(false);
                    }
                }).show();
    }

    private void parseSystemCommands(String lastCommand){
        if(lastCommand.equals(Globals.GET_RFID_AUTH))  {
            sendNextCommand(Globals.GET_PUBLIC_PRIVATE);
        }
    }

    private void parseSystemData(String data){
        if(data.length() == 16){
            String signal = data.substring(4, 6);
            if(signal.equals(Globals.SIGNAL_RFID_AUTH)){
                final String rfidAuth = data.substring(14);
                final String mainRec = data.substring(10, 12);
                rfidAuthSet = data.substring(8);
                if(progressDialog != null){
                    progressDialog.dismiss();
                }
                if(data.substring(6,8).equals("01")){
                    Toast.makeText(SetConfiguration.this, "Settings applied successfully", Toast.LENGTH_LONG).show();
                }
                SetConfiguration.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(rfidAuth.equals("01")){
                            rfid_auth_switch.setChecked(true);
                        }else{
                            rfid_auth_switch.setChecked(false);
                        }

                        if(mainRec.equals("01")){
                            mains_recovery.setChecked(true);
                        }else{
                            mains_recovery.setChecked(false);
                        }
                    }
                });
            }else if(signal.equals(Globals.SIGNAL_FACTORY_RESET)){
                Toast.makeText(SetConfiguration.this, "Charger reset successfully", Toast.LENGTH_LONG).show();
            }else if(signal.equals(Globals.SIGNAL_PUBLIC_PRIVATE)){
                if(data.substring(15).equals("1") || data.substring(15).equals("0")){
                    public_mode.setChecked(false);
                }
                if(data.substring(15).equals("2")){
                    public_mode.setChecked(true);
                }

                if(data.substring(6, 8).equals("01")){
                    Toast.makeText(SetConfiguration.this, "Mode Set Successfully", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}