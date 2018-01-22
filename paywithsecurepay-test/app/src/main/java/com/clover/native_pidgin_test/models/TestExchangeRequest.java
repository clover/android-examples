package com.clover.native_pidgin_test.models;

import com.google.gson.JsonObject;

/**
 * Created by connor on 10/31/17.
 */
public class TestExchangeRequest {

  public enum Method {
    SALE,
    ACCEPT_SIGNATURE,
    REJECT_SIGNATURE,
    ACCEPT_PAYMENT,
    REJECT_PAYMENT,
    AUTH,
    PREAUTH,
    CAPTURE_PREAUTH,
    TIP_ADJUST,
    VOID_PAYMENT,
    REFUND_PAYMENT,
    MANUAL_REFUND,
    VAULT_CARD,
    CANCEL,
    CLOSEOUT,
    PRINT,
    RETRIEVE_PRINTERS,
    RETRIEVE_PRINT_JOB_STATUS,
    OPEN_CASH_DRAWER,
    PRINT_TEXT,
    PRINT_IMAGE_URL,
    SHOW_MESSAGE,
    SHOW_WELCOME,
    SHOW_THANK_YOU,
    DISPLAY_RECEIPT_OPTIONS,
    SHOW_DISPLAY_ORDER,
    REMOVE_DISPLAY_ORDER,
    INVOKE_INPUT_OPTION,
    RESET,
    RETRIEVE_PENDING_PAYMENTS,
    READ_CARD_DATA,
    SEND_ACTIVITY_MESSAGE,
    START_ACTIVITY,
    DEVICE_STATUS,
    RETRIEVE_PAYMENT
  }

  public Method method;
  //  public Object param;
  public JsonObject payload;
}
