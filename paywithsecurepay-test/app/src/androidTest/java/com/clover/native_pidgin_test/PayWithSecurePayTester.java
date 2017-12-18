package com.clover.native_pidgin_test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.test.InstrumentationTestCase;
import android.view.accessibility.AccessibilityNodeInfo;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

@LargeTest
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PayWithSecurePayTester {
  private UiDevice device;
  private final long LONG_WAIT = 20000000L;
  private boolean called = false;

  @Test
  public void testA() throws Exception {
    if(!called) {
      //super.setUp();
      //injectInstrumentation(getInstrumentation());
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
      //device.wait(Until.hasObject(By.pkg("com.clover.payment.executor.station.secure")), LONG_WAIT);
      device.wait(Until.hasObject(By.pkg("com.clover.payment.executor.station.secure")), LONG_WAIT);
      System.out.println("Package is gone; " + ss);
      System.out.println("New packge: " + device.getCurrentPackageName());
      called = true;
    }

  }

  @Test
  public void testAA() throws Exception {
    if(device != null) {
      boolean sale = device.wait(Until.hasObject(By.res(device.getCurrentPackageName(), "swipe_card_icon")), LONG_WAIT);
      System.out.println("Card swipe fragment present: " + sale);

      boolean processing = device.wait(Until.hasObject(By.res(device.getCurrentPackageName(), "layout_processing")), LONG_WAIT);
      System.out.println("Processing fragment present: " + processing);
      //boolean processingGone = device.wait(Until.gone(By.res(device.getCurrentPackageName(), "layout_processing")), LONG_WAIT);
      //System.out.println("Processing fragment gone: " + processingGone);

    }
  }

  /*@Test
  public void testAMini() {
    if(device != null) {

    }
  }*/


  @Test
  public void testAAA() throws Exception {

    if(device != null) {
      boolean some = device.wait(Until.hasObject(By.pkg("com.clover.payment.executor.station.secure")), LONG_WAIT);
      //device.wait(Until.hasObject(By.res(device.getCurrentPackageName(), "signhere")), LONG_WAIT);
      device.wait(Until.hasObject(By.res(device.getCurrentPackageName(), "print_receipt")), LONG_WAIT);
      System.out.println("Found package: " + some + " " + device.getCurrentPackageName());
      System.out.println("Found class: " + device.getClass());
      device.drag(800, 450, 1200, 460, 2);
      UiObject2 clickDone = device.findObject(By.text("Done"));
      clickDone.click();
    }
    else {
      System.out.println("Device is null");
    }


  }

  @Test
  public void testB() throws Exception {

    System.out.println("TestB Current package name: " + device.getCurrentPackageName());
    System.out.println("Launcher package name: " + device.getLauncherPackageName());
    System.out.println("Product name: " +device.getProductName());
    device.wait(Until.hasObject(By.text("Print Customer Receipt").clazz("android.widget.Button")), LONG_WAIT);
    //device.waitForWindowUpdate()
    //wait();
    UiObject2 customerReceipt = device.findObject(By.text("Print Customer Receipt"));
    customerReceipt.click();

    UiObject2 doneButton = device.findObject(By.text("Done"));

    doneButton.click();
  }




  /*@Test
  public void testB() throws Exception {
    device.wait(Until.hasObject(By.text("Credit").clazz("android.Widget.Button")), LONG_WAIT);

    UiObject2 credit = device.findObject(By.text("Credit"));

    credit.click();
  }*/






}
