package com.exicom.evcharger;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.Myholder> {
    List<DataHandler> dataModelArrayList;
    Context context;
    CallWebService callWebService;
    public DeviceListAdapter(List<DataHandler> dataModelArrayList, Context context) {
        this.dataModelArrayList = dataModelArrayList;
        this.context = context;
    }

    @Override
    public Myholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_array_list,null);
        return new Myholder(view);
    }

    @Override
    public void onBindViewHolder(Myholder holder, final int position) {
        final DataHandler dataHandler = dataModelArrayList.get(position);
        holder.charger_serial_no.setText(dataHandler.getCharger_serial_no());
        holder.charger_nickname.setText(dataHandler.getNickName());
        holder.device_listitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),MainActivity.class);
                intent.putExtra("charger_serial_no",dataHandler.getCharger_serial_no());
                intent.putExtra("charger_nickname", dataHandler.getNickName());
                intent.putExtra("charger_password", dataHandler.getClient_certificate());
                intent.putExtra("charger_id", dataHandler.getId());
                v.getContext().startActivity(intent);
            }
        });
        holder.device_listitem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                final NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                if(info != null && info.isConnected()) {
                    String user_id = Globals.getUserId(context);
                    String[] options;
                    boolean isAdmin = false;
                    if (dataHandler.getCreated_by().equals(user_id)) {
                        options = new String[]{"Delete", "Edit Name"};
                        isAdmin = true;
                    }else{
                        options = new String[]{"Delete"};
                        isAdmin = false;
                    }
                    final boolean isDeviceAdmin = isAdmin;
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Select");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // the user clicked on colors[which]
                                if (which == 0) {
                                    showDeleteConfrmation(dataHandler.getId(), position, isDeviceAdmin);
                                } else if (which == 1) {
                                    Intent intent = new Intent(context, SaveNewDevice.class);
                                    intent.putExtra("charger_name", dataHandler.getCharger_serial_no());
                                    intent.putExtra("charger_id", dataHandler.getId());
                                    intent.putExtra("flag", "edit");
                                    context.startActivity(intent);
                                }
                            }
                        });
                        builder.show();

                }else{
                    Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
    }

    public void showDeleteConfrmation(final String id, final int pos, final boolean isDeviceAdmin){
        new AlertDialog.Builder(context)
                .setTitle("Title")
                .setMessage("Do you really want to delete the device from your account?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(isDeviceAdmin) {
                            deleteDevice(id, pos);
                        }else{
                            removeDevice(id, pos);
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void deleteDevice(String device_id, final Integer position){
        callWebService = (CallWebService) new CallWebService(context, "", Globals.DELETE_DEVICE + device_id, new CallWebService.DataReceivedListener() {
            @Override
            public void onDataReceived(String data) {
                Toast.makeText(context, "Charger deleted from account successfully", Toast.LENGTH_SHORT).show();
                SqliteHelper sb = new SqliteHelper(context);
                sb.deleteItem(dataModelArrayList.get(position).getCharger_serial_no());
                dataModelArrayList.clear();
                dataModelArrayList = sb.getData();
                notifyDataSetChanged();
            }
        }).execute();
    }

    private void removeDevice(String device_id, final Integer position){
        callWebService = (CallWebService) new CallWebService(context, "", Globals.REMOVE_DEVICE_USER + "/" + Globals.getUserId(context) + "/" + device_id, new CallWebService.DataReceivedListener() {
            @Override
            public void onDataReceived(String data) {
                Log.d("Device", data);
                Toast.makeText(context, "Charger deleted from account successfully", Toast.LENGTH_SHORT).show();
                SqliteHelper sb = new SqliteHelper(context);
                sb.deleteItem(dataModelArrayList.get(position).getCharger_serial_no());
                dataModelArrayList.clear();
                dataModelArrayList = sb.getData();
                notifyDataSetChanged();
            }
        }).execute();
    }

    @Override
    public int getItemCount() {
        return dataModelArrayList.size();
    }

    class Myholder extends RecyclerView.ViewHolder{
        TextView charger_serial_no, charger_nickname;
        LinearLayout device_listitem;
        public Myholder(View itemView) {
            super(itemView);
            charger_serial_no = itemView.findViewById(R.id.charger_serial_no);
            charger_nickname = itemView.findViewById(R.id.charger_nickname);
            device_listitem = itemView.findViewById(R.id.device_listitem);
        }
    }
}
