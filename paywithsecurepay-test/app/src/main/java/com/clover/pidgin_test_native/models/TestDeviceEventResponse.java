package com.clover.pidgin_test_native.models;




import com.clover.remote.InputOption;

import com.google.gson.JsonObject;

/**
 * Created by connor on 11/1/17.
 */
public class TestDeviceEventResponse {
  public InputOption inputOption;
  public TestExchangeRequest.Method method;
  public JsonObject response;
}
