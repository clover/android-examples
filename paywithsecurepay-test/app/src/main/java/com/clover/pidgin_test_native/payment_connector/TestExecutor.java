package com.clover.pidgin_test_native.payment_connector;

import com.clover.paywithsecurepay_pidgin_test.PidginTestActivityLogger;
import com.clover.paywithsecurepay_pidgin_test.android_test.TestUtils;
import com.clover.paywithsecurepay_pidgin_test.android_test.models.CloverTestAction;
import com.clover.paywithsecurepay_pidgin_test.android_test.models.TestActionResult;
import com.clover.paywithsecurepay_pidgin_test.android_test.models.TestDeviceEventResponse;
import com.clover.paywithsecurepay_pidgin_test.android_test.models.TestExchangeRequest;
import com.clover.paywithsecurepay_pidgin_test.android_test.models.TestExchangeResponse;
import com.clover.paywithsecurepay_pidgin_test.models.TestAction;
import com.clover.remote.InputOption;
import com.clover.sdk.v3.base.Challenge;
import com.clover.sdk.v3.connector.IPaymentConnector;
import com.clover.sdk.v3.order.DisplayOrder;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.remotepay.AuthRequest;
import com.clover.sdk.v3.remotepay.CapturePreAuthRequest;
import com.clover.sdk.v3.remotepay.CloseoutRequest;
//import com.clover.sdk.v3.remotepay.CloverDeviceEvent;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.sdk.v3.remotepay.ManualRefundRequest;
import com.clover.sdk.v3.remotepay.MessageToActivity;
import com.clover.sdk.v3.remotepay.OpenCashDrawerRequest;
import com.clover.sdk.v3.remotepay.PreAuthRequest;
import com.clover.sdk.v3.remotepay.PrintJobStatusRequest;
import com.clover.sdk.v3.remotepay.PrintRequest;
import com.clover.sdk.v3.remotepay.ReadCardDataRequest;
import com.clover.sdk.v3.remotepay.RefundPaymentRequest;
import com.clover.sdk.v3.remotepay.RetrieveDeviceStatusRequest;
import com.clover.sdk.v3.remotepay.RetrievePaymentRequest;
import com.clover.sdk.v3.remotepay.RetrievePrintersRequest;
import com.clover.sdk.v3.remotepay.SaleRequest;
import com.clover.sdk.v3.remotepay.TipAdjustAuthRequest;
import com.clover.sdk.v3.remotepay.VerifySignatureRequest;
import com.clover.sdk.v3.remotepay.VoidPaymentRequest;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import junit.framework.Test;
import org.json.JSONException;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created by connor on 10/31/17.
 */
public class TestExecutor {

  // Generator Constants
  private static final String GEN_EXT_ID = "GEN_EXT_ID";

  // Request constants for non-wrapped request parameters
  private static final String CARD_ENTRY_METHODS = "cardEntryMethods";
  private static final String CHALLENGE = "challenge";
  private static final String MESSAGE = "message";
  private static final String ORDER_ID = "orderId";
  private static final String PAYMENT = "payment";
  private static final String PAYMENT_ID = "paymentId";
  private static final String PRINT_TEXT = "text";
  private static final String URL = "url";

  private final Map<String, JsonElement> storedValues;
  private final CloverTestAction action;
  //private final IPaymentConnector connector;
  private final TestLogPaymentConnector connector;
  //private final PaymentConnectorTestManager.TestConnector testConnector;
  private long delay = 0;
  private int responseTimeout = 30;
  private boolean waitForResponse = false;
  private static final Gson GSON = new GsonBuilder().create();

  public TestExecutor(CloverTestAction action, TestLogPaymentConnector testConnector, Map<String, JsonElement> storedValues) {

    //this.testConnector = testConnector;
    this.storedValues = storedValues;
    this.connector = testConnector;
    this.action = action;

    try {
      this.delay = (action.parameters.has("delay")) ? action.parameters.getLong("delay") : delay;
      this.responseTimeout = (action.parameters.has("responseTimeout")) ? action.parameters.getInt("responseTimeout") : responseTimeout;

      if (action.parameters.has("waitForResponse")) {

        this.waitForResponse = action.parameters.getBoolean("waitForResponse");

      } else if (action.response != null) {
        this.waitForResponse = true;
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }

  }


  public boolean executeAction() {
    // Wait for a delay, if any
    if (delay > 0) {
      try {
        Thread.sleep(delay);
      } catch (Exception ex) {
        // NO-OP
      }
    }

    // Record request time
    action.result = new TestActionResult();
    action.result.requestTime = new Date();

    // execute before actions
    for (TestAction beforeAction : action.before) {
      TestExecutor beforeExecutor = new TestExecutor((CloverTestAction) beforeAction, connector, storedValues);
      // Update the listener with the current executor
      connector.getListener().setTestExecutor(beforeExecutor);
      if (!beforeExecutor.executeAction()) {
        throw new TestExecutionException();
      }
    }

    connector.getListener().setTestExecutor(this);

    int actionIterations = 1;
    try {
      actionIterations = action.parameters.getInt("iterations");
    } catch (JSONException e) {
      e.printStackTrace();
    }

    for (int i = 0; i < actionIterations; i++) {

      if (executeRequest(action.request.method, action.request.payload)) {
        // Wait for response if a response is defined
        if (action.response != null && waitForResponse) {
          synchronized (connector) {
            try {
              if (responseTimeout > 0) {
                // Wait for the specified time
                long timeout = TimeUnit.SECONDS.toMillis(responseTimeout);
                connector.wait(TimeUnit.SECONDS.toMillis(responseTimeout));
              } else {
                // Wait indefinitely
                connector.wait();
              }
            } catch (InterruptedException ex) {
              // NO-OP
            }
          }

          if (action.result.responseTime == null) {
            action.result.pass = false;
            action.result.reason = "Response Timeout";
          }
        }
      } else {
        action.result.pass = false;
        action.result.reason = "Error executing Method: " + action.request.method;
      }

      showActionResults(action);
    }

    // execute after actions
    for (TestAction afterAction : action.after) {
      TestExecutor afterExecutor = new TestExecutor((CloverTestAction)afterAction, connector, storedValues);
      // Update the listener with the current executor
      connector.getListener().setTestExecutor(afterExecutor);
      if (!afterExecutor.executeAction()) {
        throw new TestExecutionException();
      }
    }

    connector.getListener().setTestExecutor(this);
    return action.result.pass;
  }

  private JsonObject resolve(JsonObject object) {

    if (object == null) {
      return null;
    }

    // Return a copy of the object with all fields resolved
    JsonObject out = new JsonObject();
    for (Map.Entry<String, JsonElement> item : object.entrySet()) {
      out.add(item.getKey(), resolveElement(item.getValue()));
    }
    return out;
  }

  private JsonElement resolveElement(JsonElement element) {

    if (element.isJsonPrimitive()) {
      // Element is a primitive...traverse fields to see if we need to resolve
      JsonPrimitive primitive = element.getAsJsonPrimitive();
      String stringValue = primitive.getAsString();
      if (stringValue.startsWith("$:")) {
        //  Value is populated from a stored variable
        JsonElement storedValue = storedValues.get(stringValue.substring(2));
        if (storedValue != null) {
          // todo
        }
        return storedValue == null ? JsonNull.INSTANCE : storedValue;
      } else if (stringValue.startsWith("#:")) {
        //  Value is populated from a function
        String function = stringValue.substring(2);
        if (function.equals(GEN_EXT_ID)) {
          return new JsonPrimitive(TestUtils.getNextId());
        }
        System.err.println("Unsupported function: " + function);
        return JsonNull.INSTANCE;
      }
      // Just use the existing value
      return element;
    } else if (element.isJsonObject()) {
      // Entry is an object...recursively traverse
      return resolve(element.getAsJsonObject());
    } else if (element.isJsonArray()) {
      // Array of elements...iterate and process
      JsonArray array = element.getAsJsonArray();
      JsonArray copy = new JsonArray();
      for (JsonElement arrayItem : array) {
        copy.add(resolveElement(arrayItem));
      }
      return copy;
    }
    // Just add the straight up object
    return element;
  }

  private boolean executeRequest(TestExchangeRequest.Method method, JsonObject payload) {

    try {
      // Process request JSON
      JsonObject resolvedRequest = resolve(payload);

      // Execute request
      switch (method) {
        case SALE:
          connector.sale((SaleRequest) TestUtils.swapObjects(resolvedRequest, method));
          break;

        case ACCEPT_SIGNATURE:
          connector.acceptSignature((VerifySignatureRequest) TestUtils.swapObjects(resolvedRequest, method));
          break;

        case REJECT_SIGNATURE:
          connector.rejectSignature((VerifySignatureRequest) TestUtils.swapObjects(resolvedRequest, method));
          break;

        case ACCEPT_PAYMENT:
          connector.acceptPayment((Payment) TestUtils.swapObjects(resolvedRequest, method));
          break;

        case REJECT_PAYMENT:
          connector.rejectPayment(TestUtils.getValue(resolvedRequest, PAYMENT, Payment.class, (Payment) null),
              TestUtils.getValue(resolvedRequest, CHALLENGE, Challenge.class, (Challenge) null));
          break;

        case AUTH:
          connector.auth((AuthRequest) TestUtils.swapObjects(resolvedRequest, method));
          break;

        case PREAUTH:
          connector.preAuth((PreAuthRequest) TestUtils.swapObjects(resolvedRequest, method));
          break;

        case CAPTURE_PREAUTH:
          connector.capturePreAuth((CapturePreAuthRequest) TestUtils.swapObjects(resolvedRequest, method));
          break;

        case TIP_ADJUST:
          connector.tipAdjustAuth((TipAdjustAuthRequest) TestUtils.swapObjects(resolvedRequest, method));
          break;

        case VOID_PAYMENT:
          connector.voidPayment((VoidPaymentRequest) TestUtils.swapObjects(resolvedRequest, method));
          break;

        case REFUND_PAYMENT:
          connector.refundPayment((RefundPaymentRequest) TestUtils.swapObjects(resolvedRequest, method));
          break;

        case MANUAL_REFUND:
          connector.manualRefund((ManualRefundRequest) TestUtils.swapObjects(resolvedRequest, method));
          break;

        case VAULT_CARD:
          connector.vaultCard(TestUtils.getValue(resolvedRequest, CARD_ENTRY_METHODS, Integer.class, (Integer) null));
          break;

        case CLOSEOUT:
          connector.closeout((CloseoutRequest) TestUtils.swapObjects(resolvedRequest, method));
          break;

        case RETRIEVE_PENDING_PAYMENTS:
          connector.retrievePendingPayments();
          break;

        case READ_CARD_DATA:
          connector.readCardData((ReadCardDataRequest) TestUtils.swapObjects(resolvedRequest, method));
          break;

        case RETRIEVE_PAYMENT:
          connector.retrievePayment((RetrievePaymentRequest) TestUtils.swapObjects(resolvedRequest, method));
          break;

        default:
          System.err.println("Unsupported method type: " + method);
          return false;
      }
      return true;
    } catch (Exception ex) {
      System.err.println("Error executing request: " + method);
      ex.printStackTrace();
    }
    return false;
  }

  private void showActionResults(CloverTestAction action) {

    DateFormat dateFormat = DateFormat.getDateTimeInstance();
    PidginTestActivityLogger.appendLnToLog("Action name: " + action.name);
    String result = "Request: " + action.request.method +
                    "  Start: " + dateFormat.format(action.result.requestTime) +
                    "  Stop: " + (action.result.responseTime != null ? dateFormat.format(action.result.responseTime) : "<unknown>") +
                    "  Result: " + action.result.pass +
                    (action.result.pass ? "" : "  Reason: " + action.result.reason);

    PidginTestActivityLogger.appendLnToLog(result);
    System.out.println(result);
  }

  public void processResult(TestExchangeResponse.Method method, Object result) {

    // Perform response processing based upon the provided response object
    if (action.response != null && action.response.method == method) {
      if (waitForResponse) {
        action.result.responseTime = new Date();
      }

      JsonObject jsonResult = TestUtils.GSON.toJsonTree(result).getAsJsonObject();

      // Care about the result...process
      try {
        processResult(jsonResult, action.response.payload);
      } catch (Exception ex) {
        System.err.println("Error processing result");
        ex.printStackTrace();
      }

      if (action.response.store != null) {
        // Store any result values as defined in the result
        try {
          for (Map.Entry<String, JsonElement> item : action.response.store.entrySet()) {
            storeResult(jsonResult.get(item.getKey()), item);
          }
        } catch (Exception ex) {
          System.err.println("Error storing results");
          ex.printStackTrace();
        }
      }

      if (waitForResponse) {
        // Response processed...generate notification
        synchronized (connector) {
          connector.notifyAll();
        }
      }
    }
  }

  private void storeResult(JsonElement result, Map.Entry<String, JsonElement> storedValue) {

    if (result == null) {
      System.err.println("No result for storedValue key: " + storedValue.getKey());
      return;
    }

    JsonElement value = storedValue.getValue();
    if (value.isJsonPrimitive()) {
      // A primitive indicates an element that should be stored, based upon the key provided
      String storedValueKey = value.getAsString();
      storedValues.put(value.getAsString(), result);
    } else if (value.isJsonObject()) {
      if (!result.isJsonObject()) {
        System.err.println("Invalid result [Object Expected] for storedValue key: " + storedValue.getKey());
        return;
      }

      JsonObject resultObject = result.getAsJsonObject();
      for (Map.Entry<String, JsonElement> item : value.getAsJsonObject().entrySet()) {
        storeResult(resultObject.get(item.getKey()), item);
      }
    }
  }

  private void processResult(JsonObject result, JsonObject expected) {

    for (Map.Entry<String, JsonElement> entry : expected.entrySet()) {
      JsonElement expectedElement = entry.getValue();
      JsonElement resultElement = result.get(entry.getKey());
      if (expectedElement.isJsonPrimitive()) {
        String target = expectedElement.getAsString();
        if (resultElement != null) {
          // Result is not null
          if (target == null) {
            // Result should be specifically null
            action.result.pass = false;
            action.result.reason = "Expected value = null; Actual value = " + resultElement.toString();
          } else if (!target.equals("*")) {
            // Target is not null or a wildcard
            if (target.startsWith("$:")) {
              // Target is actually a stored variable...resolve
              JsonElement storedValue = storedValues.get(target.substring(2));
              if (storedValue == null) {
                action.result.pass = false;
                action.result.reason = "Cannot resolve target value = " + target + "; Actual value = " + resultElement.toString();
                continue;
              } else if (!storedValue.isJsonPrimitive()) {
                action.result.pass = false;
                action.result.reason = "Cannot resolve target value [not a primitive] = " + target + "; Actual value = " + resultElement.toString();
                continue;
              }
              target = storedValue.getAsString();
            }

            // should match
            String resultString = resultElement.getAsString();
            if (!target.equals(resultString)) {
              action.result.pass = false;
              action.result.reason = "Expected value = " + target + "; Actual value = " + resultString;
            }
          }
          // Target is wildcard...don't care what it is, just can't be null
        } else if (target != null) {
          // Result is null, and target is not null
          action.result.pass = false;
          action.result.reason = "Expected value = " + target + "; Actual value = null";
        }
      } else if (expectedElement.isJsonObject()) {
        processResult((JsonObject) resultElement, (JsonObject) expectedElement);
      }
    }
  }

  public boolean confirmPaymentChallenge(String name) {

    if (action != null && action.deviceRequests != null) {
      Map<String, String> confirmMappings = action.deviceRequests.paymentConfirmation;
      if (confirmMappings != null && "REJECT".equals(confirmMappings.get(name))) {
        return false;
      }
    }
    // Accept by default
    return true;
  }

  public boolean acceptSignature() {

    if (action != null && action.deviceRequests != null && "REJECT".equals(action.deviceRequests.signatureVerification)) {
      return false;
    }
    // Accept by default
    return true;
  }

  public void processDeviceEvent(CloverDeviceEvent event) {

    if (action != null && action.inputOptions != null) {
      TestDeviceEventResponse response = action.inputOptions.get(event.getEventState());
      if (response != null) {
        InputOption option = response.inputOption;
        if (option == null) {
          if (response.method != null) {
            executeRequest(response.method, response.response);
          }
        } else {
          if (option.keyPress == null) {
            // No keypress specified...derive from inputs
            Pattern pattern = Pattern.compile(option.description);
            InputOption[] eventOptions = event.getInputOptions();
            option = null;
            for (InputOption eventOption : eventOptions) {
              if (pattern.matcher(eventOption.description).find()) {
                option = eventOption;
                break;
              }
            }
          }
          if (option != null) {
            connector.getSecurePayClient().doKeyPress(option.keyPress);
          }
        }
      }
    }
  }

}
