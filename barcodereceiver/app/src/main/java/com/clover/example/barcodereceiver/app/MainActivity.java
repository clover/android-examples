package com.clover.example.barcodereceiver.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;


public class MainActivity extends Activity {
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
    unregisterReceiver(barcodeReceiver);
  }

  private void registerBarcodeScanner() {
    registerReceiver(barcodeReceiver, new IntentFilter("com.clover.stripes.BarcodeBroadcast"));
  }

  private void unregisterBarcodeScanner() {
    unregisterReceiver(barcodeReceiver);
  }

  private class BarcodeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (action.equals("com.clover.stripes.BarcodeBroadcast")) {
        String barcode = intent.getStringExtra("Barcode");
        if (barcode != null) {
           mTextView.setText("Barcode scanned : " + barcode);
        }
      }
    }
  }
}
