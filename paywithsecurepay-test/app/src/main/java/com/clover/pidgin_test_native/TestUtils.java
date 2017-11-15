package com.clover.pidgin_test_native;/*
 * Copyright (C) 2017 Clover Network, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.clover.common2.Signature2;
import com.clover.paywithsecurepay_pidgin_test.PidginTestActivityLogger;
import com.clover.paywithsecurepay_pidgin_test.android_test.adapters.CloverEmbeddedJsonTypeAdapter;
import com.clover.paywithsecurepay_pidgin_test.android_test.models.TestExchangeRequest;
import com.clover.sdk.JSONifiable;
import com.clover.sdk.v3.base.Signature;
import com.clover.sdk.v3.remotepay.AuthRequest;
import com.clover.sdk.v3.remotepay.CapturePreAuthRequest;
import com.clover.sdk.v3.remotepay.CloseoutRequest;
import com.clover.sdk.v3.remotepay.ManualRefundRequest;
import com.clover.sdk.v3.remotepay.PreAuthRequest;
import com.clover.sdk.v3.remotepay.PrintRequest;
import com.clover.sdk.v3.remotepay.ReadCardDataRequest;
import com.clover.sdk.v3.remotepay.RefundPaymentRequest;
import com.clover.sdk.v3.remotepay.RetrievePaymentRequest;
import com.clover.sdk.v3.remotepay.SaleRequest;
import com.clover.sdk.v3.remotepay.TipAdjustAuthRequest;
import com.clover.sdk.v3.remotepay.VerifySignatureRequest;
import com.clover.sdk.v3.remotepay.VoidPaymentRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TestUtils {
  private static final SecureRandom random = new SecureRandom();
  private static final char[] vals = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z'}; // Crockford's base 32 chars

  public static final Gson GSON;
  static {
    GsonBuilder builder = new GsonBuilder();

    builder.registerTypeHierarchyAdapter(JSONifiable.class, new CloverEmbeddedJsonTypeAdapter());

    GSON = builder.create();
  }

  private TestUtils() {
    // Private constructor for utility class
  }

  public static String getNextId() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 13; i++) {
      int idx = random.nextInt(vals.length);
      sb.append(vals[idx]);
    }
    return sb.toString();
  }

  public static void setFieldValue(Field field, Object object, Object value) {
    if (field != null) {
      try {
        field.setAccessible(true);

        field.set(object, value);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  public static <T> T getValue(JsonObject object, Class<T> clazz) {
    return object == null ? null : GSON.fromJson(object, clazz);
  }

  public static <T> T getValue(JsonObject object, String key, Class<T> clazz, T defaultValue) {
    if (object == null) {
      return defaultValue;
    }

    JsonElement value = object.get(key);
    if (value == null) {
      return defaultValue;
    }

    return getValue(value, clazz);
  }

  private static <T> T getValue(JsonElement value, Class<T> clazz) {
    if (value.isJsonPrimitive()) {
      if (clazz == Boolean.class) {
        return clazz.cast(value.getAsBoolean());
      } else if (clazz == Long.class) {
        return clazz.cast(value.getAsLong());
      } else if (clazz == Integer.class) {
        return clazz.cast(value.getAsInt());
      } else if (clazz == Short.class) {
        return clazz.cast(value.getAsShort());
      } else if (clazz == String.class) {
        return clazz.cast(value.getAsString());
      } else  if (clazz == Double.class) {
        return clazz.cast(value.getAsDouble());
      } else  if (clazz == Float.class) {
        return clazz.cast(value.getAsFloat());
      } else  if (clazz == Byte.class) {
        return clazz.cast(value.getAsByte());
      } else  if (clazz == Character.class) {
        return clazz.cast(value.getAsCharacter());
      } else  if (clazz == BigInteger.class) {
        return clazz.cast(value.getAsBigInteger());
      } else  if (clazz == BigDecimal.class) {
        return clazz.cast(value.getAsBigDecimal());
      }
      return null;
    } else if (value.isJsonNull()) {
      return null;
    } else if (value.isJsonObject()) {
      return GSON.fromJson(value, clazz);
    }
    return null;
  }

  public static <T> List<T> getValue(JsonObject object, String key, Class<T> clazz, List<T> defaultValue) {
    if (object == null) {
      return defaultValue;
    }

    JsonElement value = object.get(key);
    if (value == null) {
      return defaultValue;
    }

    if (value.isJsonArray()) {
      List<T> out = new LinkedList<>();
      for (JsonElement item : value.getAsJsonArray()) {
        out.add(getValue(item, clazz));
      }
      return out;
    } else if (value.isJsonNull()) {
      return null;
    } else {
      return Collections.singletonList(getValue(value, clazz));
    }
  }

  /**
   * import com.clover.sdk.v3.remotepay.* objects are nonserializable so we need remote-pay messages to be
   * the objects being serialized and then transferring the data from that object to the equivalent v3 object
    * @param payload
   * @param method
   * @return
   */
  public static Object swapObjects(JsonObject payload, TestExchangeRequest.Method method) {
    switch (method) {

      case SALE:
        com.clover.remote.client.messages.SaleRequest request = getValue(payload, com.clover.remote.client.messages.SaleRequest.class);
        SaleRequest returnRequest = new SaleRequest();
        if(request.getAllowOfflinePayment() != null) {
          returnRequest.setAllowOfflinePayment(request.getAllowOfflinePayment());
        }
        if(request.getApproveOfflinePaymentWithoutPrompt() != null) {
          returnRequest.setApproveOfflinePaymentWithoutPrompt(request.getApproveOfflinePaymentWithoutPrompt());
        }
        if(request.getDisableCashback() != null) {
          returnRequest.setDisableCashback(request.getDisableCashback());
        }
        if(request.getDisableTipOnScreen() != null) {
          returnRequest.setDisableTipOnScreen(request.getDisableTipOnScreen());
        }
        if(request.getAutoAcceptPaymentConfirmations() != null) {
          returnRequest.setAutoAcceptPaymentConfirmations(request.getAutoAcceptPaymentConfirmations());
        }
        if(request.getTippableAmount() != null) {
          returnRequest.setTippableAmount(request.getTippableAmount());
        }
        if(request.getTipAmount() != null) {
          returnRequest.setTipAmount(request.getTipAmount());
        }
        if(request.getTaxAmount() != null) {
          returnRequest.setTaxAmount(request.getTaxAmount());
        }
        if(request.getForceOfflinePayment() != null) {
          returnRequest.setForceOfflinePayment(request.getForceOfflinePayment());
        }
        if(request.getExternalId() != null) {
          returnRequest.setExternalId(request.getExternalId());
        }
        if(request.getAmount() != 0) {
          returnRequest.setAmount(request.getAmount());
        }
        if(request.getVaultedCard() != null) {
          returnRequest.setVaultedCard(request.getVaultedCard());
        }
        return returnRequest;


      case AUTH:
        com.clover.remote.client.messages.AuthRequest authRequest = getValue(payload, com.clover.remote.client.messages.AuthRequest.class);
        AuthRequest returnAuthRequest = new AuthRequest();
        if(authRequest.getDisableCashback() != null) {
          returnAuthRequest.setDisableCashback(authRequest.getDisableCashback());
        }
        if(authRequest.getTaxAmount() != null) {
          returnAuthRequest.setTaxAmount(authRequest.getTaxAmount());
        }
        if(authRequest.getTippableAmount() != null) {
          returnAuthRequest.setTippableAmount(authRequest.getTippableAmount());
        }
        if(authRequest.getAllowOfflinePayment() != null) {
          returnAuthRequest.setAllowOfflinePayment(authRequest.getAllowOfflinePayment());
        }
        if(authRequest.getApproveOfflinePaymentWithoutPrompt() != null) {
          returnAuthRequest.setApproveOfflinePaymentWithoutPrompt(authRequest.getApproveOfflinePaymentWithoutPrompt());
        }
        if(authRequest.getForceOfflinePayment() != null) {
          returnAuthRequest.setForceOfflinePayment(authRequest.getForceOfflinePayment());
        }
        if(authRequest.getExternalId() != null) {
          returnAuthRequest.setExternalId(authRequest.getExternalId());
        }
        if(authRequest.getAmount() != 0) {
          returnAuthRequest.setAmount(authRequest.getAmount());
        }

        return returnAuthRequest;


      case PREAUTH:
        com.clover.remote.client.messages.PreAuthRequest preAuthRequest = getValue(payload, com.clover.remote.client.messages.PreAuthRequest.class);
        PreAuthRequest returnPreAuthRequest = new PreAuthRequest();
        if(preAuthRequest.getExternalId() != null) {
          returnPreAuthRequest.setExternalId(preAuthRequest.getExternalId());
        }
        if(preAuthRequest.getAmount() != 0) {
          returnPreAuthRequest.setAmount(preAuthRequest.getAmount());
        }
        return returnPreAuthRequest;

      case ACCEPT_SIGNATURE: case REJECT_SIGNATURE:
        com.clover.remote.client.messages.VerifySignatureRequest verifySignatureRequest = getValue(payload, com.clover.remote.client.messages.VerifySignatureRequest.class);
        VerifySignatureRequest returnVerfiySignatureRequest = new VerifySignatureRequest();
        if(verifySignatureRequest.getPayment() != null) {
          returnVerfiySignatureRequest.setPayment(verifySignatureRequest.getPayment());
        }
        if(verifySignatureRequest.getSignature() != null) {
          returnVerfiySignatureRequest.setSignature(null);
        }
        return returnVerfiySignatureRequest;

      case CAPTURE_PREAUTH:
        com.clover.remote.client.messages.CapturePreAuthRequest capturePreAuthRequest = getValue(payload, com.clover.remote.client.messages.CapturePreAuthRequest.class);
        CapturePreAuthRequest capturePreAuthRequest1 = new CapturePreAuthRequest();

        if(capturePreAuthRequest.getAmount() != 0) {
          capturePreAuthRequest1.setAmount(capturePreAuthRequest.getAmount());
        }
        if(capturePreAuthRequest.getPaymentID() != null) {
          capturePreAuthRequest1.setPaymentId(capturePreAuthRequest.getPaymentID());
        }
        if(capturePreAuthRequest.getTipAmount() != 0) {
          capturePreAuthRequest1.setTipAmount(capturePreAuthRequest.getTipAmount());
        }
        return capturePreAuthRequest1;

      case TIP_ADJUST:
        com.clover.remote.client.messages.TipAdjustAuthRequest tipAdjustAuthRequest = getValue(payload, com.clover.remote.client.messages.TipAdjustAuthRequest.class);
        TipAdjustAuthRequest tipAdjustAuthRequest1 = new TipAdjustAuthRequest();

        if(tipAdjustAuthRequest.getOrderId() != null) {
          tipAdjustAuthRequest1.setOrderId(tipAdjustAuthRequest.getOrderId());
        }
        if(tipAdjustAuthRequest.getPaymentId() != null) {
          tipAdjustAuthRequest1.setPaymentId(tipAdjustAuthRequest.getPaymentId());
        }
        if(tipAdjustAuthRequest.getTipAmount() != 0) {
          tipAdjustAuthRequest1.setTipAmount(tipAdjustAuthRequest.getTipAmount());
        }
        return tipAdjustAuthRequest1;

      case VOID_PAYMENT:
        com.clover.remote.client.messages.VoidPaymentRequest voidPaymentRequest = getValue(payload, com.clover.remote.client.messages.VoidPaymentRequest.class);
        VoidPaymentRequest voidPaymentRequest1 = new VoidPaymentRequest();

        if(voidPaymentRequest.getPaymentId() != null) {
          voidPaymentRequest1.setPaymentId(voidPaymentRequest.getPaymentId());
        }
        if(voidPaymentRequest.getOrderId() != null) {
          voidPaymentRequest1.setOrderId(voidPaymentRequest.getOrderId());
        }
        if(voidPaymentRequest.getVoidReason() != null) {
          voidPaymentRequest1.setVoidReason(voidPaymentRequest.getVoidReason());
        }
        if(voidPaymentRequest.getEmployeeId() != null) {
          voidPaymentRequest1.setEmployeeId(voidPaymentRequest.getEmployeeId());
        }
        return voidPaymentRequest1;

      case REFUND_PAYMENT:
        com.clover.remote.client.messages.RefundPaymentRequest refundPaymentRequest = getValue(payload, com.clover.remote.client.messages.RefundPaymentRequest.class);
        RefundPaymentRequest refundPaymentRequest1 = new RefundPaymentRequest();
        if(refundPaymentRequest.getPaymentId() != null) {
          refundPaymentRequest1.setPaymentId(refundPaymentRequest.getPaymentId());
        }
        if(refundPaymentRequest.getOrderId() != null) {
          refundPaymentRequest1.setOrderId(refundPaymentRequest.getOrderId());
        }
        if(refundPaymentRequest.getAmount() != 0) {
          refundPaymentRequest1.setAmount(refundPaymentRequest.getAmount());
        }
        if(refundPaymentRequest.isFullRefund()) {
          refundPaymentRequest1.setFullRefund(refundPaymentRequest.isFullRefund());
        }
        return refundPaymentRequest1;

      case MANUAL_REFUND:
        com.clover.remote.client.messages.ManualRefundRequest refundRequest = getValue(payload, com.clover.remote.client.messages.ManualRefundRequest.class);
        ManualRefundRequest refundRequest1 = new ManualRefundRequest();
        if(refundRequest.getExternalId() != null) {
          refundRequest1.setExternalId(refundRequest.getExternalId());
        }
        if(refundRequest.getAmount() != 0) {
          refundRequest1.setAmount(refundRequest.getAmount());
        }
        return refundRequest1;

      case CLOSEOUT:
        CloseoutRequest closeoutRequest1 = new CloseoutRequest();
        com.clover.remote.client.messages.CloseoutRequest closeoutRequest = getValue(payload, com.clover.remote.client.messages.CloseoutRequest.class);
        if(closeoutRequest.isAllowOpenTabs()) {
          closeoutRequest1.setAllowOpenTabs(true);
        }
        if(closeoutRequest.getBatchId() != null) {
          closeoutRequest1.setBatchId(closeoutRequest.getBatchId());
        }
        return closeoutRequest1;

      case RETRIEVE_PAYMENT:
        RetrievePaymentRequest retrievePaymentRequest1 = new RetrievePaymentRequest();
        com.clover.remote.client.messages.RetrievePaymentRequest retrievePaymentRequest = getValue(payload, com.clover.remote.client.messages.RetrievePaymentRequest.class);
        if(retrievePaymentRequest.getExternalPaymentId() != null) {
          retrievePaymentRequest1.setExternalPaymentId(retrievePaymentRequest.getExternalPaymentId());
        }
        return retrievePaymentRequest1;

      case READ_CARD_DATA:
        ReadCardDataRequest readCardDataRequest1 = new ReadCardDataRequest();
        com.clover.remote.client.messages.ReadCardDataRequest readCardDataRequest = getValue(payload, com.clover.remote.client.messages.ReadCardDataRequest.class);
        if(readCardDataRequest.getCardEntryMethods() != null) {
          readCardDataRequest1.setCardEntryMethods(readCardDataRequest.getCardEntryMethods());
        }
        if(readCardDataRequest.isForceSwipePinEntry()) {
          readCardDataRequest1.setIsForceSwipePinEntry(true);
        }
        return readCardDataRequest1;

        default:
          PidginTestActivityLogger.appendLnToLog("Error executing request: " + method);
          return null;
    }
  }
}
