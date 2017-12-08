package com.clover.native_pidgin_test.payment_connector;

import com.clover.pidgin_test_native_lib.LstrNester;
import com.clover.pidgin_test_native_lib.adapters.JSONObjectTypeAdapter;
import com.clover.pidgin_test_native_lib.PidginConfig;
import com.clover.native_pidgin_test.adapters.CloverTestActionTypeAdapter;
import com.clover.pidgin_test_native_lib.models.TestAction;
import com.clover.pidgin_test_native_lib.models.TestCase;

import android.accounts.Account;
import android.app.Activity;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by connor on 10/31/17.
 */
public class PaymentConnectorLstrNester implements LstrNester {

  public static final String CONFIG_IPS = "clover.ips";
  public static final String CONFIG_WS_SCHEMA = "ws.schema";
  public static final String CONFIG_WS_PORT = "ws.port";

  /**
   * Convert PidginConfig into test objects and execute testing.
   * @param pidginConfig as the pidgin config
   */
  public void nestAndTest(PidginConfig pidginConfig, Account account, Activity activity) {

    // convert test case JSON object in to List<TestCase>
    List<TestCase> testCases = loadTests(pidginConfig.getTestCases());


    // execute test
    new PaymentConnectorTestManager().execute(testCases, pidginConfig.getContext(), account);
  }

  private List<TestCase> loadTests(JSONObject[] testCases) {


    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeHierarchyAdapter(JSONObject.class, new JSONObjectTypeAdapter());
    builder.registerTypeHierarchyAdapter(TestAction.class, new CloverTestActionTypeAdapter());

    List<TestCase> testCaseList = new ArrayList<>(testCases.length);
    for (JSONObject testCase : testCases) {

      TestCase tc = builder.create().fromJson(testCase.toString(), TestCase.class);
      testCaseList.add(tc);
    }

    return testCaseList;
  }


  @Override
  public void nestAndTest(PidginConfig pidginConfig) {

  }

  @Override
  public void nestAndTest(PidginConfig pidginConfig, Account account) {

    // convert test case JSON object in to List<TestCase>
    List<TestCase> testCases = loadTests(pidginConfig.getTestCases());


    // execute test
    new PaymentConnectorTestManager().execute(testCases, pidginConfig.getContext(), account);

  }
}
