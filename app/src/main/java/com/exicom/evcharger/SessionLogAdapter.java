package com.exicom.evcharger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class SessionLogAdapter extends BaseAdapter {

    Context context;
    ArrayList<HashMap<String, String>> logArray;
    public SessionLogAdapter(Context context, Globals gInstance){
        this.context = context;
        this.logArray = gInstance.getSessionLogArray();
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
        SessionLogListViewHolder slHolder = new SessionLogListViewHolder();
        if(customView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            customView = inflater.inflate(R.layout.session_log_list_item, null);
        }
        slHolder.session_log_index = customView.findViewById(R.id.session_log_index);
        slHolder.session_log_start_time = customView.findViewById(R.id.session_log_start_time);
        slHolder.session_log_stop_time = customView.findViewById(R.id.session_log_stop_time);
        slHolder.session_unit = customView.findViewById(R.id.session_unit);

        slHolder.session_log_index.setText(String.valueOf(position+1));
        slHolder.session_log_start_time.setText(logArray.get(position).get("start_time"));
        slHolder.session_log_stop_time.setText(logArray.get(position).get("stop_time"));
        slHolder.session_unit.setText(logArray.get(position).get("kwh_unit"));
        return customView;
    }

    private class SessionLogListViewHolder{
        TextView session_log_index, session_log_start_time, session_log_stop_time, session_unit;
    }
}
