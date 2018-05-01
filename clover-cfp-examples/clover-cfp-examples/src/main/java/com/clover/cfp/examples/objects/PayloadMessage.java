package com.clover.cfp.examples.objects;

import com.clover.remote.message.ByteArrayToBase64TypeAdapter;
import com.clover.remote.message.CloverJSONifiableTypeAdapter;
import com.clover.sdk.JSONifiable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Created by glennbedwell on 5/4/17.
 */
public class PayloadMessage {
  public final String payloadClassName;
  public final MessageType messageType;
  private static final Gson GSON;

  static {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeHierarchyAdapter(JSONifiable.class, new CloverJSONifiableTypeAdapter());
    builder.registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter());
    GSON = builder.create();
  }

  private static final JsonParser PARSER = new JsonParser();

  public PayloadMessage(String payloadClassName, MessageType messageType) {
    if (payloadClassName == null || payloadClassName.isEmpty()) {
      this.payloadClassName = "PayloadMessage";
    } else {
      this.payloadClassName = payloadClassName;
    }
    this.messageType = messageType;
  }

  public String toJsonString() {
    return GSON.toJson(this, this.getClass());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + toJsonString();
  }

  public static PayloadMessage fromJsonString(String m) {
    JsonElement je = PARSER.parse(m);
    JsonObject jo = je.getAsJsonObject();
    String payloadClassName = jo.get("payloadClassName").getAsString();
    Class<? extends PayloadMessage> cls = null;
    try {
      cls = (Class<? extends PayloadMessage>)Class.forName("com.clover.cfp.examples.objects." + payloadClassName);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return GSON.fromJson(jo, cls);
  }

}
