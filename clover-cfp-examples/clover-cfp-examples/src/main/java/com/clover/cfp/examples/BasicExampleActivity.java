package com.clover.cfp.examples;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.clover.cfp.activity.CFPConstants;
import com.clover.cfp.activity.CloverCFPActivity;

public class BasicExampleActivity extends CloverCFPActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    int orientation = this.getResources().getConfiguration().orientation;
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      setContentView(R.layout.activity_basic_example_portrait);
      findViewById(R.id.basic_example_port_layout).setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
          return true;
        }
      });
    }
    else{
      setContentView(R.layout.activity_basic_example);
      findViewById(R.id.basic_example_land_layout).setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
          return true;
        }
      });
    }
  }

  @Override
  protected void onMessage(String payload) {

  }

  @Override protected void onResume() {
    super.onResume();
    String payload = getIntent().getStringExtra(CFPConstants.EXTRA_PAYLOAD);
    ((TextView)findViewById(R.id.payload)).setText(payload);
  }

  public void finishClicked(View view) {
    String payload = ((EditText)findViewById(R.id.resultPayload)).getText().toString();
    setResultAndFinish(RESULT_OK, payload);
  }
}