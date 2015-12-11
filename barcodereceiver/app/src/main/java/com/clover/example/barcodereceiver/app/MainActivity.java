package com.clover.example.barcodereceiver.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class MainActivity extends Activity implements CompoundButton.OnCheckedChangeListener {
    public static final String BARCODE_BROADCAST = "com.clover.BarcodeBroadcast";
    private final BarcodeReceiver barcodeReceiver = new BarcodeReceiver();
    private TextView mTextView;
    private CheckBox checkBox;
    private String scannedBarcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.barcode);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(this);
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

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        displayBarcode();
    }

    private void displayBarcode() {
        if (scannedBarcode == null)
            return;

        String barcode = scannedBarcode;
        // Clover OrderIds are sometimes encoded to fit in the barcode (Mini/Mobile Printer specifically)
        // If it is not the full OrderId, then it will be a prefix that you can filter by.
        if (checkBox.isChecked())
            barcode = BarcodeIdUtil.safeBase64toBase32(barcode);

        mTextView.setText(getString(R.string.barcode_scanned,barcode));
    }

    private class BarcodeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BARCODE_BROADCAST)) {
                String barcode = intent.getStringExtra("Barcode");
                if (barcode != null) {
                    scannedBarcode = barcode;
                    displayBarcode();
                }
            }
        }
    }
}

