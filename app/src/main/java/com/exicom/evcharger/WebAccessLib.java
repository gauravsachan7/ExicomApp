package com.exicom.evcharger;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebAccessLib {

    private static final String WEB_ACCESS_DEBUG_TAG = "WEBACCESSLIB";
    private static Context context;

    public WebAccessLib(Context context) {
        this.context = context;
    }

    public String sendHTTPRequestUsingPost(String url, String urlParameters ) throws Exception {

        Log.d("Url_Parameters",urlParameters+" "+url);
        URL apiurl = new URL(url);
        HttpURLConnection uc = (HttpURLConnection) apiurl.openConnection();
        String line;
        StringBuffer jsonString = new StringBuffer();

        uc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        if(!(url.contains(Globals.LOGIN_TAG) || url.contains(Globals.SIGNUP_TAG))){
            uc.setRequestProperty("Authorization", "JWT "+Globals.getUserToken(context));
        }
        if(urlParameters.equals("")){
            uc.setRequestMethod("GET");
        }else{
            uc.setRequestMethod("POST");
        }

        uc.setDoInput(true);
        uc.setInstanceFollowRedirects(false);
        uc.connect();
        if(!urlParameters.equals("")) {
            OutputStreamWriter writer = new OutputStreamWriter(uc.getOutputStream(), "UTF-8");
            writer.write(urlParameters);
            writer.close();
        }
        try {
            BufferedReader br;
            Log.d("respose_code", String.valueOf(uc.getResponseCode()));


            if(uc.getResponseCode() == 401){
                br = new BufferedReader(new InputStreamReader(uc.getErrorStream()));
                Log.d("respose_errr", String.valueOf(uc.getErrorStream()));
            }else{
                br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            }

            while((line = br.readLine()) != null){
                jsonString.append(line);
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        uc.disconnect();
        return jsonString.toString();
    }
}
