package com.clover.pidgin_test_native.payment_connector;

import com.clover.paywithsecurepay_pidgin_test.LstrNester;
import com.clover.paywithsecurepay_pidgin_test.adapters.JSONObjectTypeAdapter;
import com.clover.paywithsecurepay_pidgin_test.PidginConfig;
import com.clover.paywithsecurepay_pidgin_test.android_test.adapters.CloverTestActionTypeAdapter;
import com.clover.paywithsecurepay_pidgin_test.models.TestAction;
import com.clover.paywithsecurepay_pidgin_test.models.TestCase;

import android.accounts.Account;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
  public void nestAndTest(PidginConfig pidginConfig, Account account) {

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
}
