package com.clover.native_pidgin_test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.test.InstrumentationTestCase;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PayWithSecurePayTester extends InstrumentationTestCase {

  private UiDevice device;
  private final long LONG_WAIT = 20000L;
  @Before
  public void setUp() throws Exception {
    super.setUp();
    injectInstrumentation(InstrumentationRegistry.getInstrumentation());
    device = UiDevice.getInstance(getInstrumentation());
    if(device != null) {
      System.out.println("Device is connected");
    }

    final String TARGET_PKG = InstrumentationRegistry.getTargetContext().getPackageName();
    System.out.println("Target package: " + TARGET_PKG);
    Context context = InstrumentationRegistry.getContext();
    Bundle bundle = new Bundle();
    bundle.putString("configs", "config.properties");
    bundle.putString("tests", "test_station2.json");
    final Intent intent = context.getPackageManager().getLaunchIntentForPackage(TARGET_PKG);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
    intent.putExtras(bundle);
    context.startActivity(intent);

    device.wait(Until.hasObject(By.pkg(TARGET_PKG).depth(0)), LONG_WAIT);


  }


  @Test
  public void testA() throws Exception {

    if(this.getInstrumentation() == null) {
      System.out.println("Instrumentation is null!!!!!!!");
    }
    else {
      System.out.println("instrumentation is just fine.");
    }
    if(device != null) {
      //InstrumentationRegistry.getInstrumentation().
      device.wait(Until.hasObject(By.text("Sign here").clazz("android.widget.TextView")), LONG_WAIT);
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
