package com.exicom.evcharger;

import android.content.Context;
import android.content.Intent;
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


public class SignUp extends AppCompatActivity {
    Toolbar toolbar;
    EditText f_name,user_email, phone, pass,re_pass;
    Button signUp;
    TextView login;
    CallWebService callWebService;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().hide();
        f_name = findViewById(R.id.f_name);
        user_email = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        re_pass = findViewById(R.id.sign_up_re_password);
        login  = findViewById(R.id.login);
        signUp = findViewById(R.id.signup);
        phone = findViewById(R.id.mobile);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    protected void registerUser(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if(info != null && info.isConnected()) {
            String name = f_name.getText().toString();
            final String email = user_email.getText().toString();
            String password = pass.getText().toString();
            String re_password = re_pass.getText().toString();
            String mobile = phone.getText().toString();
            String role = "{\"id\": 2, \"name\": \"Customer\"}";

            if (isNotEmpty(name, f_name) && isNotEmpty(email, user_email) && isNotEmpty(password, pass) && isNotEmpty(re_password, re_pass) && isNotEmpty(mobile, phone)) {
                if (password.equals(re_password)) {
                    JSONObject signUpJson = new JSONObject();
                    try {
                        signUpJson.put("email", email);
                        signUpJson.put("name", name);
                        signUpJson.put("password", password);
                        signUpJson.put("role", role);
                        signUpJson.put("mobile", mobile);
                        callWebService = (CallWebService) new CallWebService(SignUp.this, signUpJson + "", Globals.SIGNUP_TAG, new CallWebService.DataReceivedListener() {
                            @Override
                            public void onDataReceived(String data) {
                                Log.d("response_received", String.valueOf(data));
                                if (data != null) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(data);
                                        String status = jsonObject.getString("status");
                                        String message = jsonObject.getString("msg");
                                        if (status.equals("1")) {
                                            Toast.makeText(SignUp.this, "User Registered Successfully, please login to continue", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(SignUp.this, Login.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            user_email.setError(message);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).execute();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    re_pass.setError("Password does not match");
                }
            }
        }else{
            Toast.makeText(SignUp.this, "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }

    public boolean isNotEmpty(CharSequence target, EditText editText) {
        if (TextUtils.isEmpty(target)) {
            editText.setError("The Item Can't be Empty");
            return false;
        }
        return true;
    }
}