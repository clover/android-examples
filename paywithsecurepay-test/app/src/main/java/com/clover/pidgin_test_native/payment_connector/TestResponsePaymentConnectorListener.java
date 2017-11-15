package com.clover.pidgin_test_native.payment_connector;

import com.clover.paywithsecurepay_pidgin_test.MainActivity;
import com.clover.paywithsecurepay_pidgin_test.PidginTestActivityLogger;
import com.clover.paywithsecurepay_pidgin_test.android_test.models.TestExchangeResponse;
import com.clover.sdk.v3.base.Challenge;
import com.clover.sdk.v3.connector.IPaymentConnector;
import com.clover.sdk.v3.connector.IPaymentConnectorListener;
import com.clover.sdk.v3.order.VoidReason;
import com.clover.sdk.v3.payments.Credit;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.remotepay.AuthResponse;
import com.clover.sdk.v3.remotepay.CapturePreAuthResponse;
import com.clover.sdk.v3.remotepay.CloseoutResponse;
import com.clover.sdk.v3.remotepay.ConfirmPaymentRequest;
import com.clover.sdk.v3.remotepay.ManualRefundResponse;
import com.clover.sdk.v3.remotepay.PreAuthResponse;
import com.clover.sdk.v3.remotepay.ReadCardDataResponse;
import com.clover.sdk.v3.remotepay.RefundPaymentResponse;
import com.clover.sdk.v3.remotepay.RetrievePaymentResponse;
import com.clover.sdk.v3.remotepay.RetrievePendingPaymentsResponse;
import com.clover.sdk.v3.remotepay.SaleResponse;
import com.clover.sdk.v3.remotepay.TipAdded;
import com.clover.sdk.v3.remotepay.TipAdjustAuthResponse;
import com.clover.sdk.v3.remotepay.VaultCardResponse;
import com.clover.sdk.v3.remotepay.VerifySignatureRequest;
import com.clover.sdk.v3.remotepay.VoidPaymentResponse;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by connor on 10/31/17.
 */
public class TestResponsePaymentConnectorListener implements IPaymentConnectorListener  {

  private IPaymentConnector paymentConnector;
  private  TestExecutor testExecutor;
  //private TestSecurePayClient securePayClient;

  /*public TestResponsePaymentConnectorListener(IPaymentConnector paymentConnector) {
    this.paymentConnector = paymentConnector;
  }*/
  public void setPaymentConnector(IPaymentConnector paymentConnector) {this.paymentConnector = paymentConnector;}
  public void setTestExecutor(TestExecutor executor) {
    this.testExecutor = executor;
  }
  public TestExecutor getTestExecutor() {return testExecutor;}

  @Override
  public void onPreAuthResponse(PreAuthResponse response) {
    PidginTestActivityLogger.appendLnToLog("onPreAuthResponse called");
    testExecutor.processResult(TestExchangeResponse.Method.onPreAuthResponse, response);
  }

  @Override
  public void onAuthResponse(AuthResponse response) {
    PidginTestActivityLogger.appendLnToLog("onAuthResponse called");
    testExecutor.processResult(TestExchangeResponse.Method.onAuthResponse, response);
  }

  @Override
  public void onTipAdjustAuthResponse(TipAdjustAuthResponse response) {
    PidginTestActivityLogger.appendLnToLog("onTipAdjustAuthResponse called");
    testExecutor.processResult(TestExchangeResponse.Method.onTipAdjustAuthResponse, response);
  }

  @Override
  public void onCapturePreAuthResponse(CapturePreAuthResponse response) {
    PidginTestActivityLogger.appendLnToLog("onCapturePreAuthResponse called");
    testExecutor.processResult(TestExchangeResponse.Method.onCapturePreAuthResponse, response);
  }

  @Override
  public void onVerifySignatureRequest(VerifySignatureRequest request) {
    PidginTestActivityLogger.appendLnToLog("onVerifySignatureRequest called");
    if(testExecutor.acceptSignature()) {
      paymentConnector.acceptSignature(request);
    }
    else {
      paymentConnector.rejectSignature(request);
    }
  }

  @Override
  public void onConfirmPaymentRequest(ConfirmPaymentRequest request) {
    PidginTestActivityLogger.appendLnToLog("onConfirmPaymentRequest called");
    //accept by default
    paymentConnector.acceptPayment(request.getPayment());
  }

  @Override
  public void onSaleResponse(SaleResponse response) {
    PidginTestActivityLogger.appendLnToLog("onSaleResponse called");
    testExecutor.processResult(TestExchangeResponse.Method.onSaleResponse, response);
  }

  @Override
  public void onManualRefundResponse(ManualRefundResponse response) {
    PidginTestActivityLogger.appendLnToLog("onManualRefundResponse called");
    testExecutor.processResult(TestExchangeResponse.Method.onManualRefundResponse, response);
  }

  @Override
  public void onRefundPaymentResponse(RefundPaymentResponse response) {
    PidginTestActivityLogger.appendLnToLog("onRefundPaymentResponse called");
    testExecutor.processResult(TestExchangeResponse.Method.onRefundPaymentResponse, response);
  }

  @Override
  public void onTipAdded(TipAdded tipAdded) {
    PidginTestActivityLogger.appendLnToLog("onTipAdded called");
    testExecutor.processResult(TestExchangeResponse.Method.onTipAdded, tipAdded);
  }

  @Override
  public void onVoidPaymentResponse(VoidPaymentResponse response) {
    PidginTestActivityLogger.appendLnToLog("onVoidPaymentResponse called");
    testExecutor.processResult(TestExchangeResponse.Method.onVoidPaymentResponse, response);
  }

  @Override
  public void onVaultCardResponse(VaultCardResponse response) {
    PidginTestActivityLogger.appendLnToLog("onVaultCardResponse called");
    //response.setSuccess(false);
    testExecutor.processResult(TestExchangeResponse.Method.onVaultCardResponse, response);
  }

  @Override
  public void onRetrievePendingPaymentsResponse(RetrievePendingPaymentsResponse retrievePendingPaymentResponse) {
    PidginTestActivityLogger.appendLnToLog("onRetrievePendingPaymentsResponse called");
    testExecutor.processResult(TestExchangeResponse.Method.onRetrievePendingPaymentsResponse, retrievePendingPaymentResponse);
  }

  @Override
  public void onReadCardDataResponse(ReadCardDataResponse response) {
    PidginTestActivityLogger.appendLnToLog("onReadCardDataResponse called");
    testExecutor.processResult(TestExchangeResponse.Method.onReadCardDataResponse, response);
  }

  @Override
  public void onCloseoutResponse(CloseoutResponse response) {
    PidginTestActivityLogger.appendLnToLog("onCloseoutResponse called");
    testExecutor.processResult(TestExchangeResponse.Method.onCloseoutResponse, response);
  }

  @Override
  public void onRetrievePaymentResponse(RetrievePaymentResponse response) {
    PidginTestActivityLogger.appendLnToLog("onRetrievePaymentResponse called");
    testExecutor.processResult(TestExchangeResponse.Method.onRetrievePaymentResponse, response);
  }

  @Override
  public void onDeviceDisconnected() {

  }

  @Override
  public void onDeviceConnected() {

  }


}
