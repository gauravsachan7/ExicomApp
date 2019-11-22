package com.exicom.evcharger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class LiveAlarmListAdapter extends BaseAdapter {

    Context context;
    ArrayList<HashMap<String, String>> alarmArray;
    public LiveAlarmListAdapter(Context context, Globals gInstance){
        this.context = context;
        this.alarmArray = gInstance.getAlarmList();
    }

    @Override
    public int getCount() {
        return alarmArray.size();
    }

    @Override
    public Object getItem(int position) {
        return alarmArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View customView = convertView;
        LiveAlarmViewHolder lvHolder = new LiveAlarmViewHolder();
        if(customView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            customView = inflater.inflate(R.layout.live_alarm_item, null);
        }
        lvHolder.index = customView.findViewById(R.id.alarm_index);
        lvHolder.alarm_name = customView.findViewById(R.id.alarm_name);
        lvHolder.index.setText(String.valueOf(position+1));
        lvHolder.alarm_name.setText(alarmArray.get(position).get("name"));
        return customView;
    }


    public static class LiveAlarmViewHolder{
        TextView index, alarm_name;
    }
}
