package com.clover.cfp.examples;

import android.os.Bundle;

import com.clover.cfp.examples.R;
import com.clover.cfp.activity.CloverCFPActivity;

public class CarouselActivity extends CloverCFPActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_carousel);


  }

  @Override
  protected void onMessage(String payload) {

  }

}
