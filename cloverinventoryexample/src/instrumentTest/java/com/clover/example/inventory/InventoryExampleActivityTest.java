package com.clover.example.inventory;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.clover.example.inventory.InventoryExampleActivityTest \
 * com.clover.example.inventory.tests/android.test.InstrumentationTestRunner
 */
public class InventoryExampleActivityTest extends ActivityInstrumentationTestCase2<InventoryExampleActivity> {

    public InventoryExampleActivityTest() {
        super("com.clover.example.inventory", InventoryExampleActivity.class);
    }

}
