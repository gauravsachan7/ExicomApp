package com.exicom.evcharger;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class SaveNewDevice extends AppCompatActivity {

    EditText client_dev_no, nickname;
    Button saveCharger;
    CallWebService callWebService;
    SqliteHelper db;
    String flag = "";
    String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_charger);
        getSupportActionBar().setTitle("Provide Nickname");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        client_dev_no = findViewById(R.id.client_dev_no);
        nickname = findViewById(R.id.nickname);
        saveCharger = findViewById(R.id.saveCharger);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String device_number  = bundle.getString("charger_name");
            flag = bundle.getString("flag");
            client_dev_no.setText(device_number);
        }
        if(flag.equals("save")) {
            saveFunction();
        }else if(flag.equals("edit")){
            deviceId = bundle.getString("charger_id");
            editFunction();
        }
    }

    private void saveFunction(){
        saveCharger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String device_number = client_dev_no.getText().toString();
                if(!checkDevice(device_number)){
                    String nick_name = nickname.getText().toString();
                    if(isNotEmpty(nick_name, nickname)) {
                        saveDeviceToServer();
                    }
                }else {
                    Toast.makeText(SaveNewDevice.this, "Device already added", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void editFunction(){
        saveCharger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDevice();
            }
        });
    }

    public void updateDevice(){
        final String nick_name = nickname.getText().toString();
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if(info != null && info.isConnected()) {
            JSONObject updateDeviceJson = new JSONObject();
            try {
                updateDeviceJson.put("device_id", deviceId);
                updateDeviceJson.put("nickname", nick_name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            callWebService = (CallWebService) new CallWebService(SaveNewDevice.this, updateDeviceJson + "", Globals.UPDATE_DEVICE_NICKNAME, new CallWebService.DataReceivedListener() {
                @Override
                public void onDataReceived(String data) {
                    Log.d("received_data", data);
                    try {
                        JSONObject devObject = new JSONObject(data);
                        if (devObject.getString("status").equals("1")) {
                            Toast.makeText(SaveNewDevice.this, "Name updated successfully", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(SaveNewDevice.this, "Update failed, please try later", Toast.LENGTH_LONG).show();
                        }
                        Intent intent = new Intent(SaveNewDevice.this, DeviceList.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).execute();
        }
    }

    public void saveDeviceToServer(){
        final String device_number = client_dev_no.getText().toString();
        final String nick_name = nickname.getText().toString();
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if(info != null && info.isConnected()){
            JSONObject addDeviceJson = new JSONObject();
            try{
                String created_by = Globals.getUserId(SaveNewDevice.this);
                addDeviceJson.put("client_dev_no", device_number);
                addDeviceJson.put("client_sw_no", device_number);
                addDeviceJson.put("created_by", created_by);
                addDeviceJson.put("nickname", nick_name);
                addDeviceJson.put("user", created_by);
                Log.d("callingwebser", addDeviceJson+"");

            }catch (JSONException e) {
                e.printStackTrace();
            }
            callWebService = (CallWebService) new CallWebService(SaveNewDevice.this, addDeviceJson + "", Globals.ADD_DEVICE_TAG, new CallWebService.DataReceivedListener() {
                @Override
                public void onDataReceived(String data) {
                    Log.d("received_data", data);
                    try {
                        JSONObject devObject = new JSONObject(data);
                        if(devObject.getString("status").equals("1")){
                            String client_certificate = devObject.get("client_certificate").toString();
                            addSqlite(device_number, client_certificate, nick_name);
                        } else if(devObject.getString("status").equals("2")){
                            Log.d("device_owner", devObject.getString("device_owner"));
                            showAlreadyExisting(devObject.getString("device_owner"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).execute();
        }
    }

    public boolean isNotEmpty(CharSequence target,EditText editText) {
        if (TextUtils.isEmpty(target)) {
            editText.setError("The Item Can't be Empty");
            return false;
        }
        return true;
    }

    public boolean checkDevice(String device_name){
        db = new SqliteHelper(SaveNewDevice.this);
        return db.checkDevice(device_name);
    }

    public void addSqlite(String device_number, String client_certificate, String nick_name){
        db = new SqliteHelper(SaveNewDevice.this);
        long id = db.addDevice(device_number, device_number, nick_name, Globals.getUserId(SaveNewDevice.this), client_certificate);
        Log.d("ïnserted_id", String.valueOf(id));
        Intent intent = new Intent(SaveNewDevice.this, DeviceList.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void showAlreadyExisting(final String device_owner){
        new AlertDialog.Builder(this)
            .setTitle("Device already registered")
            .setMessage("Do you want to request owner of this device to grant access ?")

            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    createAccessRequest(device_owner);
                }
            })
            .setNegativeButton(android.R.string.no, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    public void createAccessRequest(String device_owner){
        final String device_number = client_dev_no.getText().toString();
        final String nick_name = nickname.getText().toString();
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if(info != null && info.isConnected()){
            JSONObject addDeviceJson = new JSONObject();
            try{
                String user_id = Globals.getUserId(SaveNewDevice.this);
                String user_email = Globals.getUserEmail(SaveNewDevice.this);
                String user_name = Globals.getUserName(SaveNewDevice.this);
                addDeviceJson.put("device_number", device_number);
                addDeviceJson.put("device_owner", device_owner);
                addDeviceJson.put("user_id", user_id);
                addDeviceJson.put("user_email", user_email);
                addDeviceJson.put("user_name", user_name);
                Log.d("callingwebser", addDeviceJson+"");

            }catch (JSONException e) {
                e.printStackTrace();
            }
            callWebService = (CallWebService) new CallWebService(SaveNewDevice.this, addDeviceJson + "", Globals.CREATE_ACCESS_REQUEST_TAG, new CallWebService.DataReceivedListener() {
                @Override
                public void onDataReceived(String data) {
                    Log.d("received_data", data);
                    try {
                        JSONObject devObject = new JSONObject(data);
                        if(devObject.getString("status").equals("1")){
                            Toast.makeText(SaveNewDevice.this, devObject.getString("msg"), Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(SaveNewDevice.this, DeviceList.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else if(devObject.getString("status").equals("0")){
                            Toast.makeText(SaveNewDevice.this, devObject.getString("msg"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).execute();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
