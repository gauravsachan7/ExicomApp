package com.exicom.evcharger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class GuestAccessList extends AppCompatActivity {
    ListView guest_list, request_list;
    CallWebService callWebService;
    GuestListAdapter guestListAdapter;
    RequestListAdapter requestListAdapter;
    Button recieved_request, sent_request;
    LinearLayout layout_guest_list, layout_request_list;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_guest_access_layout);
        getSupportActionBar().setTitle("Guest Access");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
        guest_list = findViewById(R.id.guest_list);
        request_list = findViewById(R.id.request_list);
        recieved_request = findViewById(R.id.recieved_request);
        sent_request = findViewById(R.id.sent_request);
        layout_guest_list = findViewById(R.id.layout_guest_list);
        layout_request_list = findViewById(R.id.layout_request_list);
        recieved_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_request_list.setVisibility(View.GONE);
                layout_guest_list.setVisibility(View.VISIBLE);
            }
        });
        sent_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_guest_list.setVisibility(View.GONE);
                layout_request_list.setVisibility(View.VISIBLE);
            }
        });
        getGuestAccessList();
        getRequestList();
    }

    protected void getGuestAccessList(){
        JSONObject guestAccessJson = new JSONObject();
        try{
            String user_id = Globals.getUserId(GuestAccessList.this);
            guestAccessJson.put("user_id", user_id);
        }catch (JSONException e) {
            e.printStackTrace();
        }
        callWebService = (CallWebService) new CallWebService(GuestAccessList.this, guestAccessJson + "", Globals.GUEST_ACCESS_LIST, new CallWebService.DataReceivedListener() {
            @Override
            public void onDataReceived(String data) {
                Log.d("Guest_received", data);
                try {
                    JSONObject dataObject = new JSONObject(data);
                    Integer status = dataObject.getInt("status");
                    if(status == 1){
                        String reqArr = dataObject.getString("data");
                        JSONArray dataArray = new JSONArray(reqArr);
                        if(dataArray.length() > 0) {
                            guestListAdapter = new GuestListAdapter(GuestAccessList.this, dataArray);
                            guest_list.setAdapter(guestListAdapter);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).execute();
    }

    protected void getRequestList(){
        JSONObject requestObj = new JSONObject();
        try{
            String user_id = Globals.getUserId(GuestAccessList.this);
            requestObj.put("request_user_id", user_id);
        }catch (JSONException e){
            e.printStackTrace();
        }
        callWebService = (CallWebService) new CallWebService(GuestAccessList.this, requestObj + "", Globals.REQUEST_LIST, new CallWebService.DataReceivedListener() {
            @Override
            public void onDataReceived(String data) {
                Log.d("requests_list", data);
                try{
                    JSONObject dataObject = new JSONObject(data);
                    Integer status = dataObject.getInt("status");
                    if(status == 1){
                        String reqArr = dataObject.getString("data");
                        JSONArray dataArray = new JSONArray(reqArr);
                        if(dataArray.length() > 0){
                            requestListAdapter = new RequestListAdapter(GuestAccessList.this, dataArray);
                            request_list.setAdapter(requestListAdapter);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).execute();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
