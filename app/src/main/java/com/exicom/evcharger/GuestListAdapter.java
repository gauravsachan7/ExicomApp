package com.exicom.evcharger;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class GuestListAdapter extends BaseAdapter {

    Context context;
    JSONArray guestList;
    CallWebService callWebService;
    public GuestListAdapter(Context context, JSONArray guestList){
        this.context = context;
        this.guestList = guestList;
    }

    @Override
    public int getCount() {
        return guestList.length();
    }

    @Override
    public Object getItem(int position) {
        try {
            return guestList.get(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View customView = convertView;
        GuestListViewHolder gHolder = new GuestListViewHolder();
        if(customView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            customView = inflater.inflate(R.layout.guest_access_list_item, null);
        }
        gHolder.device_name = customView.findViewById(R.id.device_name);
        gHolder.guest_name = customView.findViewById(R.id.guest_name);
        gHolder.guest_email = customView.findViewById(R.id.guest_email);
        gHolder.grant_status = customView.findViewById(R.id.grant_status);
        gHolder.grant_access = customView.findViewById(R.id.grant_access);
        gHolder.deny_access = customView.findViewById(R.id.deny_access);


        try {
            final JSONObject devObject = (JSONObject) guestList.get(position);
            gHolder.device_name.setText(devObject.getString("device_number"));
            gHolder.guest_name.setText(devObject.getString("user_name"));
            gHolder.guest_email.setText(devObject.getString("user_email"));
            if(devObject.getString("status").equals("0")) {
                gHolder.grant_status.setVisibility(View.GONE);
                gHolder.deny_access.setVisibility(View.VISIBLE);
                gHolder.grant_access.setVisibility(View.VISIBLE);

                gHolder.grant_access.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setAccess(devObject, 1);
                    }
                });

                gHolder.deny_access.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setAccess(devObject, 0);
                    }
                });
            }else{
                gHolder.deny_access.setVisibility(View.GONE);
                gHolder.grant_access.setVisibility(View.GONE);
                gHolder.grant_status.setVisibility(View.VISIBLE);
                if(devObject.getString("status").equals("1")){
                    gHolder.grant_status.setText("Granted");
                }else{
                    gHolder.grant_status.setText("Denied");
                }
            }
        }catch (JSONException e){

        }
        return customView;
    }

    private void setAccess(JSONObject devObject, Integer status){
        JSONObject guestAccessJson = new JSONObject();
        try{
            guestAccessJson.put("user_id", devObject.getString("user_id"));
            guestAccessJson.put("device_number", devObject.getString("device_number"));
            guestAccessJson.put("request_id", devObject.getString("id"));
            String tag = "";
            if(status == 1){
                tag = Globals.GRANT_ACCESS;
            }else{
                tag = Globals.DENY_ACCESS;
            }
            callWebService = (CallWebService) new CallWebService(context, guestAccessJson + "", tag, new CallWebService.DataReceivedListener() {
                @Override
                public void onDataReceived(String data) {
                    if(data != null){
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_LONG).show();
                            ((Activity)context).finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).execute();
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public static class GuestListViewHolder{
        TextView device_name, guest_email, guest_name, grant_status;
        Button grant_access, deny_access;
    }
}


