package com.exicom.evcharger;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class AlarmLogAdapter extends BaseAdapter {

    Context context;
    ArrayList<HashMap<String, String>> logArray;
    public AlarmLogAdapter(Context context, Globals gInstance){
        this.context = context;
        this.logArray = gInstance.getAlarmLogArray();
    }

    @Override
    public int getCount() {
        return logArray.size();
    }

    @Override
    public Object getItem(int position) {
        return logArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View customView = convertView;
        AlarmLogViewHolder alHolder = new AlarmLogViewHolder();
        if(customView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            customView = inflater.inflate(R.layout.alarm_log_list_item, null);
        }
        alHolder.alarm_log_index = customView.findViewById(R.id.alarm_log_index);
        alHolder.alarm_log_time_date = customView.findViewById(R.id.alarm_log_time_date);
        alHolder.alarm_status = customView.findViewById(R.id.alarm_status);
        alHolder.alarm_name = customView.findViewById(R.id.alarm_name);
        alHolder.alarm_log_index.setText(String.valueOf(position+1));
        alHolder.alarm_log_time_date.setText(logArray.get(position).get("time_date"));
        alHolder.alarm_status.setText(logArray.get(position).get("alarmFlag"));
        alHolder.alarm_name.setText(logArray.get(position).get("alarmName"));
        return customView;
    }


    private class AlarmLogViewHolder{
        TextView alarm_log_index, alarm_log_time_date, alarm_status, alarm_name;
    }
}
