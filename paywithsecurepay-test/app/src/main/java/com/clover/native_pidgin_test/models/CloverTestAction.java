package com.clover.native_pidgin_test.models;

import com.clover.pidgin_test_native_lib.models.TestAction;
import com.clover.remote.UiState;
import com.clover.remote.client.messages.CloverDeviceEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by connor on 10/31/17.
 */
public class CloverTestAction extends TestAction<CloverTestAction> {

  public TestExchangeRequest request;
  public TestExchangeResponse response;
  public DeviceRequest deviceRequests;
  public Map<CloverDeviceEvent.DeviceEventState, TestDeviceEventResponse> inputOptions = new HashMap<>();
  //public List<com.clover.remote.InputOption> inputOptions = new ArrayList<>();
  public TestActionResult result;
}