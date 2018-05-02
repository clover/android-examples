/*
 * Copyright (C) 2018 Clover Network, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clover.cfp.examples;

import com.clover.cfp.examples.R;
import com.clover.cfp.activity.CloverCFPActivity;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class NFCExampleActivity extends CloverCFPActivity {

  private NfcAdapter mAdapter;
  private PendingIntent mPendingIntent;
  private AlertDialog mDialog;

  private String TAG = "NFCExampleActivity";

  private String getHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < bytes.length; ++i) {
      int b = bytes[i] & 0xff;
      if (b < 0x10) {
        sb.append('0');
      }
      sb.append(Integer.toHexString(b));
      if (i >= 0) {
        sb.append(" ");
      }
    }
    return sb.toString();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "In onCreate");

    setContentView(R.layout.activity_nfc);
    // Necessary for Customer Facing user experiences
    setSystemUiVisibility();

    ImageView image = new ImageView(this);
    image.setImageResource(R.drawable.nfc_sign_48);

    AlertDialog.Builder builder =
        new AlertDialog.Builder(this).
            setMessage(R.string.intro_message).
            setCancelable(false).
            setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                setResultAndFinish(RESULT_CANCELED, "CANCELED");
              }
            }).
            setView(image);
    mDialog = builder.create();

    mAdapter = NfcAdapter.getDefaultAdapter(this);
    if (mAdapter == null) {
      showMessage(R.string.error, R.string.no_nfc);
      setResultAndFinish(Activity.RESULT_CANCELED, getText(R.string.no_nfc).toString());
      return;
    }
    setContentView(R.layout.activity_nfc);

    mPendingIntent = PendingIntent.getActivity(this, 0,
        new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
  }

  @Override
  protected void onMessage(String payload) {

  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.d(TAG, "In onResume");
    if (mAdapter != null) {
      if (!mAdapter.isEnabled()) {
        Log.e(TAG, "NFC not enabled");
        setResultAndFinish(RESULT_CANCELED, "NFC not enabled");
      }
      mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
      mDialog.show();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (mAdapter != null) {
      mAdapter.disableForegroundDispatch(this);
      mDialog.hide();
    }
  }

  private void resolveIntent(Intent intent) {
    String action = intent.getAction();
    if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
      Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
      if (tag != null) {
        String serialNumber = getHex(tag.getId());
        setResultAndFinish(RESULT_OK, serialNumber);
      }
    }
  }

  @Override
  public void onNewIntent(Intent intent) {
    Log.d(TAG, "In onNewIntent");
    setIntent(intent);
    resolveIntent(intent);
  }

  public void showMessage(int title, int message) {
    mDialog.setTitle(title);
    mDialog.setMessage(getText(message));
    mDialog.show();
  }

  public void setSystemUiVisibility() {
    getWindow().getDecorView().setSystemUiVisibility(
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_LOW_PROFILE
        | View.SYSTEM_UI_FLAG_FULLSCREEN
        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
  }

}
