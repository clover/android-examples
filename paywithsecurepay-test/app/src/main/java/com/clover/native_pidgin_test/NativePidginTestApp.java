package com.clover.native_pidgin_test;

import com.clover.common2.CommonApplication;

import android.app.Application;
import android.content.Context;

public class NativePidginTestApp extends CommonApplication {
  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    android.support.multidex.MultiDex.install(this);
  }
}
