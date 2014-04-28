package com.clover.example.customerfacinguiexample;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;


public class MainActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);

    setLayout(getResources().getConfiguration());
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    setLayout(newConfig);
  }

  private void setLayout(Configuration newConfig) {
    if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
      // Disable the system and action bar
      findViewById(android.R.id.content).getRootView().setSystemUiVisibility(getResources().getInteger(R.integer.CLOVER_CUSTOMER_UI_VISIBILITY));
      getActionBar().hide();
      setContentView(R.layout.activity_main_customer);
    }
    else {
      findViewById(android.R.id.content).getRootView().setSystemUiVisibility(View.VISIBLE);
      getActionBar().show();
      setContentView(R.layout.activity_main);
    }
  }
}
