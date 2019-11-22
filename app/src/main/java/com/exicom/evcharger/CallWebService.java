package com.exicom.evcharger;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;

public class CallWebService extends AsyncTask <Void, Void, Void> {
    String jsonString, responseMenu, message;
    ProgressDialog progressDialog;
    Globals gInstance;
    Context context;
    String argsJson;
    String tag;
    String userToken = null;

    public interface DataReceivedListener {
        void onDataReceived(String data);
    }

    public DataReceivedListener dataReceived = null;

    CallWebService(Context context, String argsJson, String tag, DataReceivedListener dataReceived ){
        this.context = context;
        this.argsJson = argsJson;
        this.tag = tag;
        this.dataReceived = dataReceived;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.progressdialog);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String urlParameters = argsJson;
        try {
                jsonString = gInstance.webAccessLibObj.sendHTTPRequestUsingPost(gInstance.URL + tag, urlParameters);

            if (jsonString != null) {
            } else {
                Log.e("ERROR ", "Couldn't get any data from the url");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        progressDialog.dismiss();
        if (jsonString != null) {
            Log.d("Response_Menu",jsonString);
            dataReceived.onDataReceived(jsonString);
        }else{
            Toast.makeText(context, "Server not reponding", Toast.LENGTH_LONG).show();
        }
    }
}
