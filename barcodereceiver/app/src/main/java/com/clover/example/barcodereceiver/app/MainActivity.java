package com.clover.example.barcodereceiver.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Base64;
import android.widget.TextView;


public class MainActivity extends Activity {
    public static final String BARCODE_BROADCAST = "com.clover.BarcodeBroadcast";
    private final BarcodeReceiver barcodeReceiver = new BarcodeReceiver();
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.barcode);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBarcodeScanner();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterBarcodeScanner();
    }

    private void registerBarcodeScanner() {
        registerReceiver(barcodeReceiver, new IntentFilter(BARCODE_BROADCAST));
    }

    private void unregisterBarcodeScanner() {
        unregisterReceiver(barcodeReceiver);
    }

    private class BarcodeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BARCODE_BROADCAST)) {
                String barcode = intent.getStringExtra("Barcode");
                if (barcode != null) {
                    // Clover OrderIds are sometimes encoded to fit in the barcode (Mobile Printer especially)
                    // If it is not the full OrderId, then it will be a prefix that you can filter by.
                    barcode = BarcodeIdUtil.safeBase64toBase32(barcode);
                    mTextView.setText("Barcode scanned : " + barcode);
                }
            }
        }
    }
}

