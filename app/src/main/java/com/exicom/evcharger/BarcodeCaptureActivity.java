package com.exicom.evcharger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class BarcodeCaptureActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    ZXingScannerView mScannerView;
    EditText serial_number;
    Button connect_device;
    ZXingScannerView zscanner;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);
        getSupportActionBar().setTitle("Scan QR Code");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        serial_number = findViewById(R.id.serial_num);
        connect_device = findViewById(R.id.connect_device);
        zscanner  = findViewById(R.id.zxscan);
        mScannerView = new ZXingScannerView(BarcodeCaptureActivity.this);
        serial_number.setHintTextColor(getResources().getColor(R.color.colorWhite));
        zscanner.addView(mScannerView);

        connect_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(serial_number.getText()) && validQrCode(serial_number.getText().toString())) {
                    Log.d("barcode","barcode");
                    Log.d(" Raw_result", String.valueOf(serial_number.getText()));
                    Intent intent = new Intent(BarcodeCaptureActivity.this, SaveNewDevice.class);
                    intent.putExtra("charger_name", String.valueOf(serial_number.getText()));
                    intent.putExtra("flag", "save");
                    startActivity(intent);
                    finish();
                }else{
                    serial_number.setError("Serial number invalid");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {
        Log.d("barcode","barcode");
        Log.d(" Raw_result",result.getText());
        if(result.getText().length() == 24 && result.getText().substring(8,9).equals("#")) {
            Intent intent = new Intent(BarcodeCaptureActivity.this, SaveNewDevice.class);
            intent.putExtra("charger_name", result.getText());
            intent.putExtra("flag", "save");
            startActivity(intent);
            finish();
        }else {
            Toast.makeText(BarcodeCaptureActivity.this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    protected boolean validQrCode(String qrCode){
        if(qrCode.length() == 24 && qrCode.substring(8, 9).equals("#")){
            return true;
        }
        return false;
    }

}