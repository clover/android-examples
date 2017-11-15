package com.clover.pidgin_test_native.payment_connector;

import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import com.clover.common.analytics.ALog;
import com.clover.common2.payments.PayIntent;
import com.clover.connector.sdk.v3.PaymentConnector;
import com.clover.connector.sdk.v3.PaymentV3Connector;
import com.clover.paywithsecurepay_pidgin_test.MainActivity;
import com.clover.paywithsecurepay_pidgin_test.PidginTestActivityLogger;
import com.clover.paywithsecurepay_pidgin_test.android_test.models.CloverTestAction;
import com.clover.paywithsecurepay_pidgin_test.android_test.remote_terminal_kiosk.RemoteControlClient;
import com.clover.paywithsecurepay_pidgin_test.models.TestAction;
import com.clover.paywithsecurepay_pidgin_test.models.TestCase;
import com.clover.remote.InputOption;
import com.clover.remote.KeyPress;
import com.clover.remote.TxState;
import com.clover.remote.UiState;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.order.DisplayOrder;
import com.clover.remote.order.action.OrderActionResponse;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v3.connector.IDeviceConnectorListener;
import com.clover.sdk.v3.connector.IPaymentConnector;
import com.clover.sdk.v3.connector.IPaymentConnectorListener;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.VoidReason;
import com.clover.sdk.v3.payments.Credit;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.printer.PrintCategory;
import com.clover.sdk.v3.printer.Printer;
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
import com.clover.sdk.v3.remotepay.SaleRequest;
import com.clover.sdk.v3.remotepay.SaleResponse;
import com.clover.sdk.v3.remotepay.TipAdded;
import com.clover.sdk.v3.remotepay.TipAdjustAuthResponse;
import com.clover.sdk.v3.remotepay.VaultCardResponse;
import com.clover.sdk.v3.remotepay.VerifySignatureRequest;
import com.clover.sdk.v3.remotepay.VoidPaymentResponse;
import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.JsonElement;

/**
 * Created by connor on 10/31/17.
 */
public class PaymentConnectorTestManager {

  private static final String APP_ID = "com.clover.remote.test-";
  private static final String POS_NAME = "Clover Remote Pay Java Test Harness";
  private static final String DEVICE_NAME = "Clover Device";
  private static final String AUTH_TOKEN = "AUTH_TOKEN";
  protected Account account = null;
  private final Preferences preferences = Preferences.userNodeForPackage(PaymentConnectorTestManager.class);
  private ScheduledThreadPoolExecutor executor;
  private TestSecurePayClient securePayClient;
  private RemoteControlClient remoteControlClient;
  private List<Throwable> threadExceptions = new ArrayList<>();
  private TestLogPaymentConnector testConnector;


  public void execute(List<TestCase> testCases, Context context, Account account) {


    try {
      TestSecurePayClient spc = setUpSecurePayClient(context);
      TestResponsePaymentConnectorListener paymentConnectorListener = new TestResponsePaymentConnectorListener();
      testConnector = new TestLogPaymentConnector(context, account, paymentConnectorListener);
      paymentConnectorListener.setPaymentConnector(testConnector);
      testConnector.setSecurePayClient(spc);

      executor = new ScheduledThreadPoolExecutor(2);

      if(testCases != null) {
        for(TestCase testCase : testCases) {
          runTest(testCase, testConnector);
        }
      }

      executor.shutdownNow();
    }catch (Exception e) {
      PidginTestActivityLogger.appendLnToLog(e.getMessage());
    }
  }

  private TestSecurePayClient setUpSecurePayClient(Context context) {

    if(securePayClient == null) {
      securePayClient = new TestSecurePayClient(context) {
        @Override
        protected void onTxState(TxState txState) {

        }

        @Override
        protected void onUiState(UiState uiState, String uiText, UiState.UiDirection uiDirection, InputOption... inputOptions) {
          PidginTestActivityLogger.appendLnToLog("onUiState called");
          ALog.i(this, "ui state: %s, ui name: %s, ui direction: %s, input options: %s", uiState, uiText, uiDirection, Arrays.toString(inputOptions));
          remoteControlClient.doUiState(uiState, uiText, uiDirection, inputOptions);

          //There are two UiDirections (EXIT, ENTER) that have the same InputOptions, so we filter on ENTER
          if(uiDirection == UiState.UiDirection.ENTER) {
            TestExecutor testExecutor = testConnector.getListener().getTestExecutor();
            //translate UiState, InputOptions to CloverDeviceEvent so that we can use testExecutor.processDeviceEvent without further changes
            CloverDeviceEvent cloverDeviceEvent = new CloverDeviceEvent();
            String temp = uiState.name();
            CloverDeviceEvent.DeviceEventState des = CloverDeviceEvent.DeviceEventState.valueOf(temp);
            cloverDeviceEvent.setEventState(des);
            cloverDeviceEvent.setInputOptions(inputOptions);
            testExecutor.processDeviceEvent(cloverDeviceEvent);
          }

        }

        @Override
        protected void onPaymentVoided(Payment payment, VoidReason voidReason) {

        }

        @Override
        public void onPrintMerchantCopy(Payment payment) {

        }

        @Override
        public void onPrintDecline(Payment payment, String reason) {

        }

        @Override
        public void onPrintDecline(Credit credit, String reason) {

        }

        @Override
        public void onCashbackSelected(long cashbackAmount) {

        }

        @Override
        public void onPartialAuth(long partialAuthAmount) {

        }
      };
      securePayClient.register();
    }


    if(remoteControlClient == null) {
      remoteControlClient = new RemoteControlClient(context) {
        @Override
        protected void onActivityRequest(String action, String payload, boolean nonBlocking, boolean forceLaunch) {
          PidginTestActivityLogger.appendLnToLog("ActivityRequest");
        }

        @Override
        protected void onMessageToActivity(String action, String payload) {
          PidginTestActivityLogger.appendLnToLog("MessageToActivity");
        }

        @Override
        protected void onTxStartRequest(PayIntent payIntent, Order order, boolean suppressOnScreenTips) {

        }

        @Override
        protected void onTxStartRequest(PayIntent payIntent, Order order, String requestInfo) {

        }

        @Override
        protected void onKeyPress(KeyPress keyPress) {
          PidginTestActivityLogger.appendLnToLog("onKeyPress");
        }

        @Override
        protected void onVoidPayment(Payment payment, VoidReason reason, String packageName) {

        }

        @Override
        protected void onShowWelcome() {

        }

        @Override
        protected void onGetPrinters(PrintCategory category) {

        }

        @Override
        protected void onGetPrintjobStatus(String externalPrintjobId) {

        }

        @Override
        protected void onShowOrder(DisplayOrder order, Object operation, boolean isOrderModificationSupported) {

        }

        @Override
        protected void onShowThankYou() {

        }

        @Override
        protected void onSignatureVerified(Payment payment, boolean verified) {

        }

        @Override
        protected void onTerminalMessage(String text, Integer durationInMillis, Integer messageId) {

        }

        @Override
        protected void onBreak() {

        }

        @Override
        protected void onPrintText(String externalPrintjobId, Printer printer, List<String> textLines) {

        }

        @Override
        protected void onShowPaymentReceiptOptions(String orderId, String paymentId) {

        }

        @Override
        protected void onShowPaymentReceiptOptionsV2(String orderId, String paymentId, boolean disableCloverPrinting) {

        }

        @Override
        protected void onPrintImage(String externalPrintjobId, Printer printer, Bitmap bitmap) {

        }

        @Override
        protected void onPrintImage(String externalPrintjobId, Printer printer, String urlString) {

        }

        @Override
        protected void onOrderModified(OrderActionResponse orderActionResponse) {

        }

        @Override
        protected void onRefundRequest(String orderId, String paymentId, long amount) {

        }

        @Override
        protected void onRefundRequestV2(String orderId, String paymentId, long amount, boolean fullRefund, boolean disableCloverPrinting, boolean disableReceiptSelection) {

        }

        @Override
        protected void onOpenCashdrawer(Printer printer, String reason) {

        }

        @Override
        protected void onTipAdjust(String orderId, String paymentId, long tipAmount) {

        }

        @Override
        protected void onVaultCard(int cardEntryMethods) {
          PidginTestActivityLogger.appendLnToLog("VaultCard");
        }

        @Override
        protected void onCardData(PayIntent payIntent) {

        }

        @Override
        protected void onCapturePreAuth(String paymentId, long amount, long tipAmount) {

        }

        @Override
        protected void onCloseout(boolean allowOpenTabs, String batchId) {

        }

        @Override
        protected void onPaymentConfirmed(Payment payment) {

        }

        @Override
        protected void onPaymentRejected(Payment payment, VoidReason voidReason) {

        }

        @Override
        protected void onRetrievePendingPayments() {

        }

        @Override
        protected void onRetrievePayment(String externalPaymentId) {

        }

        @Override
        protected void onRetrieveDeviceStatusRequest(boolean sendLastMessage, boolean internal, int count) {

        }
      };
      remoteControlClient.doReadyToProcessMessages();

    }

    return securePayClient;
  }



  public void runTest(final TestCase testCase, final TestLogPaymentConnector connector) {

    List<ScheduledFuture> futures = new LinkedList<>();
    Thread testThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          if(testCase.parameters.has("resetDevice")  && testCase.parameters.getBoolean("resetDevice")) {
            //do nothing, paymentConnector does not have resetDevice
          }
          // Create result cache for test case
          Map<String, JsonElement> resultCache = new HashMap<>();

          // Execute the pre-test exchanges
          try {

            int iterations = (testCase.parameters.has("iterations")) ? testCase.parameters.getInt("iterations") : 1;
            for (int i = 0; i < iterations; i++) {
              // Execute each exchange in the test case
              for (TestAction action : testCase.testActions) {
                System.out.println("Executing test action");
                executeAction((CloverTestAction) action, connector, resultCache);
              }

              // If there is a delay between test executions
              int delayBetween = (testCase.parameters.has("delayBetweenExecutions")) ? testCase.parameters.getInt("delayBetweenExecutions") : 0;
              if (delayBetween > 0) {
                try {
                  Thread.sleep(delayBetween);
                } catch (Exception ex) {
                  // NO-OP
                }
              }

              // Dispose the connector and reinitialize if dispose flag is set
              if (testCase.parameters.has("disposeBetweenExecutions") && testCase.parameters.getBoolean("disposeBetweenExecutions")) {
                //connector.initializeConnection();
              }
            }

            System.out.println("=========== Test Case [" + testCase.tags.getString("id") + "-" + testCase.name + "] executed successfully =========== ");

          } catch (TestExecutionException ex) {
            System.err.println("=========== Test Case [" + testCase.tags.getString("id") + "-" + testCase.name + "] failed =========== ");
            PidginTestActivityLogger.appendLnToLog("=========== Test Case [" + testCase.tags.getString("id") + "-" + testCase.name + "] failed =========== ");
            connector.getSecurePayClient().doBreak();
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });

    futures.add(executor.schedule(testThread, 100, TimeUnit.MILLISECONDS));

    boolean complete = false;
    while(!complete) {
      int doneCount =0;
      for(ScheduledFuture future : futures) {
        if(future.isDone()) {
          doneCount++;
        }
      }
      complete = (doneCount == futures.size());

      try {
        Thread.sleep(1000);
      } catch (Exception e) {

      }
    }

  }


  private void executeAction(CloverTestAction action, TestLogPaymentConnector testConnector, Map<String, JsonElement> resultCache) {

    TestExecutor executor = new TestExecutor(action, testConnector, resultCache);
    // Update the listener with the current executor
    testConnector.listener.setTestExecutor(executor);
    if (!executor.executeAction()) {
      throw new TestExecutionException();
    }
  }


}
