package com.clover.native_pidgin_test.adapters;

import com.clover.pidgin_test_native_lib.adapters.JSONObjectTypeAdapter;
import com.clover.native_pidgin_test.models.CloverTestAction;
import com.clover.native_pidgin_test.models.DeviceRequest;
import com.clover.native_pidgin_test.models.TestDeviceEventResponse;
import com.clover.native_pidgin_test.models.TestExchangeRequest;
import com.clover.native_pidgin_test.models.TestExchangeResponse;
import com.clover.pidgin_test_native_lib.models.TestAction;
import com.clover.remote.InputOption;
import com.clover.remote.KeyPress;
import com.clover.remote.client.messages.CloverDeviceEvent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by connor on 11/1/17.
 */
public class CloverTestActionTypeAdapter implements JsonDeserializer<CloverTestAction>{
  @Override
  public CloverTestAction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

    JsonObject jsonObject = json.getAsJsonObject();
    Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(JSONObject.class, new JSONObjectTypeAdapter()).create();

    JsonArray beforeArray = removeBefores(jsonObject);
    JsonArray afterArray = removeAfters(jsonObject);

    JsonObject testActionContext = jsonObject.getAsJsonObject("context");
    CloverTestAction out = gson.fromJson(json, CloverTestAction.class);

    out.request = deserializeRequest(testActionContext, gson);
    out.inputOptions = deserializeInputOptions(testActionContext, gson);
    out.deviceRequests = deserializeDeviceRequest(testActionContext, gson);

    JsonObject testActionAssert = jsonObject.getAsJsonObject("assert");
    out.response = deserializeResponse(testActionAssert, gson);

    out.before = getCloverTestActionList(beforeArray, typeOfT, context);
    out.after = getCloverTestActionList(afterArray, typeOfT, context);

    return out;
  }

  private JsonArray removeBefores(JsonObject testAction) {

    if (testAction.has("before")) {

      return (JsonArray) testAction.remove("before");
    }
    return null;
  }

  private JsonArray removeAfters(JsonObject testAction) {

    if (testAction.has("after")) {

      return (JsonArray) testAction.remove("after");
    }
    return null;
  }

  private TestExchangeRequest deserializeRequest(JsonObject testActionContext, Gson gson) {

    if (testActionContext != null && testActionContext.has("request")) {

      JsonObject requestObject = testActionContext.getAsJsonObject("request");

      TestExchangeRequest out = new TestExchangeRequest();
      out.method = TestExchangeRequest.Method.valueOf(requestObject.get("method").getAsString());
      out.payload = requestObject.getAsJsonObject("payload");
      return out;
    }

    return null;

  }

  private Map<CloverDeviceEvent.DeviceEventState, TestDeviceEventResponse>  deserializeInputOptions(JsonObject testActionContext, Gson gson) {

    if (testActionContext != null && testActionContext.has("inputOptions")) {

      JsonArray inputOptions = testActionContext.getAsJsonArray("inputOptions");

      Map<CloverDeviceEvent.DeviceEventState, TestDeviceEventResponse> out = new HashMap<>();
      //List<InputOption> out = new ArrayList<>();
      for (JsonElement item : inputOptions){
        if (item.isJsonObject()) {
          JsonObject jsonObject = item.getAsJsonObject();
          String eventState = getString(jsonObject, "on");
          String select = getString(jsonObject, "select");
          String description = getString(jsonObject, "description");
          String method = getString(jsonObject, "method");

          try {
            TestDeviceEventResponse response = new TestDeviceEventResponse();

            CloverDeviceEvent.DeviceEventState state = CloverDeviceEvent.DeviceEventState.valueOf(eventState);
            KeyPress keyPress = select != null ? KeyPress.valueOf(select) : null;
            if (description == null) {
              description = select;
            }

            if (keyPress != null || description != null) {
              response.inputOption = new InputOption(keyPress, description);
            } else if (method != null) {
              response.method = TestExchangeRequest.Method.valueOf(method);
              JsonElement responseObject = jsonObject.get("response");
              response.response = responseObject != null && responseObject.isJsonObject() ? responseObject.getAsJsonObject() : null;
            }

            out.put(state, response);
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      }

      return out;
    }

    return Collections.emptyMap();
  }

  private String getString(JsonObject object, String key) {

    JsonElement jsonValue = object.get(key);
    if (jsonValue != null && !jsonValue.isJsonNull()) {
      return jsonValue.getAsString();
    }
    return null;
  }

  private DeviceRequest deserializeDeviceRequest(JsonObject testActionContext, Gson gson) {

    if (testActionContext != null && testActionContext.has("deviceRequests")) {

      JsonObject deviceRequest = testActionContext.getAsJsonObject("deviceRequests");
      return gson.fromJson(deviceRequest, DeviceRequest.class);
    }

    return null;
  }

  private TestExchangeResponse deserializeResponse(JsonObject testActionAssertion, Gson gson) {

    if (testActionAssertion != null && testActionAssertion.has("response")) {

      JsonObject responseObject = testActionAssertion.getAsJsonObject("response");

      TestExchangeResponse out = new TestExchangeResponse();
      out.method = TestExchangeResponse.Method.valueOf(responseObject.get("method").getAsString());
      out.payload = responseObject.getAsJsonObject("payload");
      out.store = responseObject.getAsJsonObject("store");

      return out;
    }

    return null;
  }


  private List<CloverTestAction> getCloverTestActionList(JsonArray actionArray, Type typeOfT, JsonDeserializationContext context) {

    if (actionArray == null) {
      return new ArrayList<>();
    }

    List<CloverTestAction> actions = new ArrayList<>(actionArray.size());

    for (JsonElement actionElement : actionArray) {
      CloverTestAction action = (CloverTestAction) deserialize(actionElement, typeOfT, context);
      actions.add(action);
    }

    return actions;
  }

}
