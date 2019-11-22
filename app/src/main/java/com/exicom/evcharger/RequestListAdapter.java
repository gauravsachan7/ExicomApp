package com.exicom.evcharger;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestListAdapter extends BaseAdapter {

    Context context;
    JSONArray guestList;
    CallWebService callWebService;
    public RequestListAdapter(Context context, JSONArray guestList){
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
        RequestListViewHolder gHolder = new RequestListViewHolder();
        if(customView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            customView = inflater.inflate(R.layout.guest_request_list_item, null);
        }
        gHolder.request_device_name = customView.findViewById(R.id.request_device_name);
        gHolder.request_grant_status = customView.findViewById(R.id.request_grant_status);
        gHolder.device_admin_email = customView.findViewById(R.id.device_admin_email);
        try {
            final JSONObject devObject = (JSONObject) guestList.get(position);
            Log.d("hello_request", devObject.getString("device_number"));
            gHolder.request_device_name.setText(devObject.getString("device_number"));
            gHolder.device_admin_email.setText(devObject.getString("owner_email"));
            if(devObject.getString("status").equals("0")) {
                gHolder.request_grant_status.setText("Pending");
            }else{
                if(devObject.getString("status").equals("1")){
                    gHolder.request_grant_status.setText("Granted");
                }else{
                    gHolder.request_grant_status.setText("Denied");
                }
            }
        }catch (JSONException e){

        }
        return customView;
    }




    public static class RequestListViewHolder{
        TextView device_admin_email, request_device_name, request_grant_status;
    }
}
