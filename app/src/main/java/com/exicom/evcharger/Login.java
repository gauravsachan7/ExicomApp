package com.exicom.evcharger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity {
    Globals gInstance;
    Toolbar toolbar;
    EditText email,pass;
    Button login;
    TextView signUp;
    CallWebService callWebService;
    String responseMenu, message;
    String userToken = null;
    String userId;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        email = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        login  = findViewById(R.id.login);
        signUp = findViewById(R.id.signup);
        gInstance = Globals.getInstance(Login.this);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
                finish();
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        if(!Globals.getUserEmail(this).equals("") && !Globals.getPassword(this).equals("")){
            email.setText(Globals.getUserEmail(this));
            pass.setText(Globals.getPassword(this));
            loginUser();
        }

    }

    public void loginUser(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if(info != null && info.isConnected()) {

            final String user_id = email.getText().toString();
            final String password = pass.getText().toString();
            if (isNotEmpty(user_id, email) && isNotEmpty(password, pass)) {
                JSONObject loginJson = new JSONObject();
                try {
                    loginJson.put("email", user_id);
                    loginJson.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                callWebService = (CallWebService) new CallWebService(Login.this, loginJson + "", Globals.LOGIN_TAG, new CallWebService.DataReceivedListener() {
                    @Override
                    public void onDataReceived(String data) {
                        String user_email = "";
                        String user_name = "";
                        Log.d("response_received", data);
                        if (data != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(data);
                                responseMenu = jsonObject.getString("token");
                                if (responseMenu != null) {
                                    userToken = jsonObject.getString("token");
                                    String user = jsonObject.getString("user");
                                    JSONObject userObject = new JSONObject(user);
                                    userId = userObject.getString("id");
                                    user_email = userObject.getString("email");
                                    String provider = userObject.getString("providers");
                                    JSONObject detailObject = new JSONObject(provider);
                                    String local = detailObject.getString("local");
                                    JSONObject nameObject = new JSONObject(local);
                                    user_name = nameObject.getString("name");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(Login.this, "Invalid Username or Password", Toast.LENGTH_LONG).show();
                            }

                            if (responseMenu != null) {
                                SharedPreferences.Editor editor = getSharedPreferences(gInstance.userloginSP, MODE_PRIVATE).edit();
                                editor.putString(gInstance.SP_TAG_USERTOKEN, userToken);
                                editor.putString(gInstance.SP_TAG_USER_ID, userId);
                                editor.putString(gInstance.SP_TAG_EMAIL, user_email);
                                editor.putString(gInstance.SP_TAG_NAME, user_name);
                                editor.putString(gInstance.SP_TAG_PASSWORD, password);
                                editor.apply();
                                Intent intent = new Intent(Login.this, DeviceList.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }else{
                            Toast.makeText(Login.this, "Invalid Username or Password", Toast.LENGTH_LONG).show();
                        }
                    }
                }).execute();
            }

        }else{
            Toast.makeText(Login.this, "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }

    public boolean isNotEmpty(CharSequence target, EditText editText) {
        if (TextUtils.isEmpty(target)) {
            editText.setError("The Item Can't be Empty");
            return false;
        }
        return true;
    }

    public boolean isPasswordValid(String editextText, EditText editext) {
        boolean flag;
        if(TextUtils.isEmpty(editextText)) {
            editext.setError("The Item Can't be Empty");
            flag=false;
        } else {
            if(editextText.length()>=6) {
                flag=true;
            }
            else {
                editext.setError("Password Must Be Greater Then 5 Digit");
                flag=false;
            }
        }
        return  flag;
    }

}

