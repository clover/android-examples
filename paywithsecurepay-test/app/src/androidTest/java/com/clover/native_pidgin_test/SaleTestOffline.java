package com.clover.native_pidgin_test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class SaleTestOffline {
  private UiDevice device;
  private final long LONG_WAIT = 20000L;
  private Object lock = new Object();
  @Test
  public void testA() {
    device = UiDevice.getInstance(getInstrumentation());
    if(device != null) {
      System.out.println("Device is connected");
    }

    //final String TARGET_PKG = InstrumentationRegistry.getTargetContext().getPackageName();
    final String TARGET_PKG = getInstrumentation().getTargetContext().getPackageName();
    System.out.println("Target package: " + TARGET_PKG);
    //Context context = InstrumentationRegistry.getContext();
    Context context = getInstrumentation().getContext();

    Bundle bundle = new Bundle();
    bundle.putString("configs", "config.properties");
    bundle.putString("tests", "station2_sale.json");
    final Intent intent = context.getPackageManager().getLaunchIntentForPackage(TARGET_PKG);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
    intent.putExtras(bundle);
    context.startActivity(intent);
    String pkg = getInstrumentation().getContext().getPackageName();
    System.out.println("Package: " + pkg);
    boolean ss = device.wait(Until.gone(By.pkg(getInstrumentation().getContext().getPackageName())), LONG_WAIT);
    device.wait(Until.hasObject(By.pkg("com.clover.payment.executor.station.secure")), LONG_WAIT);


    boolean sale = device.wait(Until.hasObject(By.res(device.getCurrentPackageName(), "swipe_card_icon")), LONG_WAIT);
    System.out.println("Card swipe fragment present: " + sale);

    boolean processing = device.wait(Until.hasObject(By.res(device.getCurrentPackageName(), "layout_processing")), LONG_WAIT);
    System.out.println("Processing fragment present: " + processing);


    device.wait(Until.hasObject(By.text("CONTINUE")), LONG_WAIT);

    UiObject2 continueButton = device.findObject(By.text("CONTINUE"));
    continueButton.click();


    device.wait(Until.hasObject(By.res(device.getCurrentPackageName(), "print_receipt")), LONG_WAIT);
    //System.out.println("Found package: " + some + " " + device.getCurrentPackageName());
    System.out.println("Found class: " + device.getClass());
    device.drag(800, 250, 1200, 260, 2);
    UiObject2 clickDone = device.findObject(By.text("Done"));
    clickDone.click();



    device.wait(Until.hasObject(By.text("Print Customer Receipt").clazz("android.widget.Button")), LONG_WAIT);
    //device.waitForWindowUpdate()
    //wait();
    UiObject2 customerReceipt = device.findObject(By.text("Print Customer Receipt"));
    customerReceipt.click();

    UiObject2 doneButton = device.findObject(By.text("Done"));

    doneButton.click();

  }





}
