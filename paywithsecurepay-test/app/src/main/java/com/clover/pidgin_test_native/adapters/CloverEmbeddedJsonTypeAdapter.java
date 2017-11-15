package com.clover.pidgin_test_native.adapters;

import com.clover.sdk.JSONifiable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

/**
 * Created by connor on 11/1/17.
 */
public class CloverEmbeddedJsonTypeAdapter implements JsonDeserializer<JSONifiable>, JsonSerializer<JSONifiable> {

  private static final Gson GSON = new GsonBuilder().create();
  @Override
  public JSONifiable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
    String s = "";
    if (json.isJsonPrimitive()) {
      s = json.getAsJsonPrimitive().getAsString();
    } else if (json.isJsonObject()) {
      s = json.getAsJsonObject().toString();
    }

    if (typeOfT.getClass() == Class.class) {
      try {
        Constructor<? extends JSONifiable> ctor = ((Class)typeOfT).getConstructor(new Class[]{String.class});
        return ctor.newInstance(new Object[]{s});
      } catch (Exception var6) {
        var6.printStackTrace();
      }
    }

    return null;
  }

  @Override
  public JsonElement serialize(JSONifiable src, Type typeOfSrc, JsonSerializationContext context) {
    return GSON.fromJson(src.getJSONObject().toString(), JsonObject.class);
  }
}
