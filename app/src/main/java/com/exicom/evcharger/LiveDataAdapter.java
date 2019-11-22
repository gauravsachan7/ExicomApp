package com.exicom.evcharger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class LiveDataAdapter extends BaseAdapter {
    Context context;
    ArrayList<HashMap<String, String>> liveDataArray;
    public LiveDataAdapter(Context context, Globals gInstance){
        this.context = context;
        this.liveDataArray = gInstance.getliveDataArray();
    }

    @Override
    public int getCount() {
        return liveDataArray.size();
    }

    @Override
    public Object getItem(int position) {
        return liveDataArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View customView = convertView;
        LiveDataViewHolder lvHolder = new LiveDataViewHolder();
        if(customView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            customView = inflater.inflate(R.layout.live_data_layout, null);
        }
        lvHolder.live_data_label = customView.findViewById(R.id.live_data_label);
        lvHolder.live_data_value = customView.findViewById(R.id.live_data_value);
        lvHolder.live_data_label.setText(liveDataArray.get(position).get("label"));
        lvHolder.live_data_value.setText(liveDataArray.get(position).get("value"));
        return customView;
    }


    public static class LiveDataViewHolder{
        TextView live_data_label, live_data_value;
    }
}
