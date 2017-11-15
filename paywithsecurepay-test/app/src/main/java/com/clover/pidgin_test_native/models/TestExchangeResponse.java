package com.clover.pidgin_test_native.models;

import com.google.gson.JsonObject;

/**
 * Created by connor on 11/1/17.
 */
public class TestExchangeResponse {

  public enum Method {
    onDeviceActivityStart,
    onDeviceActivityEnd,
    onDeviceError,
    onPreAuthResponse,
    onAuthResponse,
    onTipAdjustAuthResponse,
    onCapturePreAuthResponse,
    onCloseoutResponse,
    onSaleResponse,
    onManualRefundResponse,
    onRefundPaymentResponse,
    onTipAdded,
    onVoidPaymentResponse,
    onVaultCardResponse,
    onPrintJobStatusResponse,
    onRetrievePrintersResponse,
    onPrintManualRefundReceipt,
    onPrintManualRefundDeclineReceipt,
    onPrintPaymentReceipt,
    onPrintPaymentDeclineReceipt,
    onPrintPaymentMerchantCopyReceipt,
    onPrintRefundPaymentReceipt,
    onRetrievePendingPaymentsResponse,
    onReadCardDataResponse,
    onMessageFromActivity,
    onCustomActivityResponse,
    onRetrieveDeviceStatusResponse,
    onResetDeviceResponse,
    onRetrievePaymentResponse
  }

  public Method method;
  public JsonObject payload;
  public JsonObject store;
}
