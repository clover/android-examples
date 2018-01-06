package com.clover.native_pidgin_test;

import com.clover.common2.CommonApplication;

import android.app.Application;
import android.content.Context;

public class NativePidginTestApp extends CommonApplication {
  public final static String LINE_SEPARATOR = System.getProperty("line.separator");//$NON-NLS-1$
  public final static String TAG = "AndroidLogCollector";//$NON-NLS-1$
  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    android.support.multidex.MultiDex.install(this);
  }
}
