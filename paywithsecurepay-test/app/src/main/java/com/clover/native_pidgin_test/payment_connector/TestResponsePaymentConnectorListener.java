package com.clover.native_pidgin_test.payment_connector;

import com.clover.pidgin_test_native_lib.MainActivity;
import com.clover.pidgin_test_native_lib.PidginTestActivityLogger;
import com.clover.native_pidgin_test.models.TestExchangeResponse;
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
    PidginTestActivityLogger.appendToTableView("onPreAuthResponse called", true);
    testExecutor.processResult(TestExchangeResponse.Method.onPreAuthResponse, response);
  }

  @Override
  public void onAuthResponse(AuthResponse response) {
    PidginTestActivityLogger.appendToTableView("onAuthResponse called", true);
    testExecutor.processResult(TestExchangeResponse.Method.onAuthResponse, response);
  }

  @Override
  public void onTipAdjustAuthResponse(TipAdjustAuthResponse response) {
    PidginTestActivityLogger.appendToTableView("onTipAdjustAuthResponse called", true);
    testExecutor.processResult(TestExchangeResponse.Method.onTipAdjustAuthResponse, response);
  }

  @Override
  public void onCapturePreAuthResponse(CapturePreAuthResponse response) {
    PidginTestActivityLogger.appendToTableView("onCapturePreAuthResponse called", true);
    testExecutor.processResult(TestExchangeResponse.Method.onCapturePreAuthResponse, response);
  }

  @Override
  public void onVerifySignatureRequest(VerifySignatureRequest request) {
    PidginTestActivityLogger.appendToTableView("onVerifySignatureRequest called", true);
    if(testExecutor.acceptSignature()) {
      paymentConnector.acceptSignature(request);
    }
    else {
      paymentConnector.rejectSignature(request);
    }
  }

  @Override
  public void onConfirmPaymentRequest(ConfirmPaymentRequest request) {
    PidginTestActivityLogger.appendToTableView("onConfirmPaymentRequest called", true);
    //accept by default
    paymentConnector.acceptPayment(request.getPayment());
  }

  @Override
  public void onSaleResponse(SaleResponse response) {
    PidginTestActivityLogger.appendToTableView("onSaleResponse called", true);
    testExecutor.processResult(TestExchangeResponse.Method.onSaleResponse, response);
  }

  @Override
  public void onManualRefundResponse(ManualRefundResponse response) {
    PidginTestActivityLogger.appendToTableView("onManualRefundResponse called", true);
    testExecutor.processResult(TestExchangeResponse.Method.onManualRefundResponse, response);
  }

  @Override
  public void onRefundPaymentResponse(RefundPaymentResponse response) {
    PidginTestActivityLogger.appendToTableView("onRefundPaymentResponse called", true);
    testExecutor.processResult(TestExchangeResponse.Method.onRefundPaymentResponse, response);
  }

  @Override
  public void onTipAdded(TipAdded tipAdded) {
    PidginTestActivityLogger.appendToTableView("onTipAdded called", true);
    testExecutor.processResult(TestExchangeResponse.Method.onTipAdded, tipAdded);
  }

  @Override
  public void onVoidPaymentResponse(VoidPaymentResponse response) {
    PidginTestActivityLogger.appendToTableView("onVoidPaymentResponse called", true);
    testExecutor.processResult(TestExchangeResponse.Method.onVoidPaymentResponse, response);
  }

  @Override
  public void onVaultCardResponse(VaultCardResponse response) {
    PidginTestActivityLogger.appendToTableView("onVaultCardResponse called", true);
    //response.setSuccess(false);
    testExecutor.processResult(TestExchangeResponse.Method.onVaultCardResponse, response);
  }

  @Override
  public void onRetrievePendingPaymentsResponse(RetrievePendingPaymentsResponse retrievePendingPaymentResponse) {
    PidginTestActivityLogger.appendToTableView("onRetrievePendingPaymentsResponse called", true);
    testExecutor.processResult(TestExchangeResponse.Method.onRetrievePendingPaymentsResponse, retrievePendingPaymentResponse);
  }

  @Override
  public void onReadCardDataResponse(ReadCardDataResponse response) {
    PidginTestActivityLogger.appendToTableView("onReadCardDataResponse called", true);
    testExecutor.processResult(TestExchangeResponse.Method.onReadCardDataResponse, response);
  }

  @Override
  public void onCloseoutResponse(CloseoutResponse response) {
    PidginTestActivityLogger.appendToTableView("onCloseoutResponse called", true);
    testExecutor.processResult(TestExchangeResponse.Method.onCloseoutResponse, response);
  }

  @Override
  public void onRetrievePaymentResponse(RetrievePaymentResponse response) {
    PidginTestActivityLogger.appendToTableView("onRetrievePaymentResponse called", true);
    testExecutor.processResult(TestExchangeResponse.Method.onRetrievePaymentResponse, response);
  }

  @Override
  public void onDeviceDisconnected() {

  }

  @Override
  public void onDeviceConnected() {

  }


}
