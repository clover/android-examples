package com.clover.native_pidgin_test.remote_terminal_kiosk;

import com.clover.common.analytics.ALog;
import com.clover.common2.Signature2;
import com.clover.common2.payments.PayIntent;
import com.clover.pidgin_test_native_lib.PidginTestActivityLogger;
import com.clover.remote.Challenge;
import com.clover.remote.ErrorCode;
import com.clover.remote.ExternalDeviceState;
import com.clover.remote.ExternalDeviceStateData;
import com.clover.remote.ExternalDeviceSubState;
import com.clover.remote.InputOption;
import com.clover.remote.KeyPress;
import com.clover.remote.QueryStatus;
import com.clover.remote.ResultStatus;
import com.clover.remote.TxStartResponseResult;
import com.clover.remote.TxState;
import com.clover.remote.UiState;
import com.clover.remote.message.TxStartRequestMessage;
import com.clover.remote.order.DisplayOrder;
import com.clover.remote.order.action.AddDiscountAction;
import com.clover.remote.order.action.AddLineItemAction;
import com.clover.remote.order.action.OrderActionResponse;
import com.clover.remote.order.action.RemoveDiscountAction;
import com.clover.remote.order.action.RemoveLineItemAction;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.VoidReason;
import com.clover.sdk.v3.pay.PaymentRequestCardDetails;
import com.clover.sdk.v3.payments.Batch;
import com.clover.sdk.v3.payments.Credit;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.payments.Refund;
import com.clover.sdk.v3.payments.VaultedCard;
import com.clover.sdk.v3.printer.PrintCategory;
import com.clover.sdk.v3.printer.Printer;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by connor on 11/6/17.
 */
public abstract class RemoteControlClient extends RemoteControl {
  public RemoteControlClient() {
    super(null);
  }

  public RemoteControlClient(Context context) {
    super(context);
  }

  @Override
  protected IntentFilter getIntentFilter() {
    return null;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    super.onReceive(context, intent);

    if (RemoteConstants.ACTION_V1_TX_START_REQUEST.equals(intent.getAction())) {
      PayIntent payIntent = new PayIntent.Builder().intent(intent).build();
      Order order = intent.getParcelableExtra(RemoteConstants.EXTRA_ORDER);
      boolean suppressOnScreenTips = intent.getBooleanExtra(RemoteConstants.EXTRA_SUPPRESS_ON_SCREEN_TIPS, false);
      onTxStartRequest(payIntent, order, suppressOnScreenTips);
    } else if (RemoteConstants.ACTION_V2_TX_START_REQUEST.equals(intent.getAction())) {
      PayIntent payIntent = new PayIntent.Builder().intent(intent).build();
      Order order = intent.getParcelableExtra(RemoteConstants.EXTRA_ORDER);
      String requestInfo = intent.getStringExtra(RemoteConstants.EXTRA_REQUEST_INFO);
      onTxStartRequest(payIntent, order, requestInfo);
    } else if (RemoteConstants.ACTION_V1_KEYPRESS.equals(intent.getAction())) {
      KeyPress keyPress = intent.getParcelableExtra(RemoteConstants.EXTRA_KEYPRESS);
      onKeyPress(keyPress);
    } else if (RemoteConstants.ACTION_V1_VOID_PAYMENT.equals(intent.getAction())) {
      Payment payment = intent.getParcelableExtra(RemoteConstants.EXTRA_PAYMENT);
      VoidReason voidReason = intent.getParcelableExtra(RemoteConstants.EXTRA_VOID_REASON);
      String packageName = intent.getStringExtra(RemoteConstants.EXTRA_PACKAGE_NAME);
      onVoidPayment(payment, voidReason, packageName);
    } else if (RemoteConstants.ACTION_V1_SHOW_ORDER.equals(intent.getAction())) {
      DisplayOrder order = intent.getParcelableExtra(RemoteConstants.EXTRA_DISPLAY_ORDER);
      boolean isOrderModificationSupported = intent.getBooleanExtra(RemoteConstants.EXTRA_SUPPORTS_ORDER_MODIFICATION, false);
      Object operation = intent.getParcelableExtra(RemoteConstants.EXTRA_ORDER_OPERATION);
      onShowOrder(order, operation, isOrderModificationSupported);
    } else if (RemoteConstants.ACTION_V1_SIGNATURE_VERIFIED.equals(intent.getAction())) {
      Payment payment = intent.getParcelableExtra(RemoteConstants.EXTRA_PAYMENT);
      boolean verified = intent.getBooleanExtra(RemoteConstants.EXTRA_SIGNATURE_VERIFIED, false);
      onSignatureVerified(payment, verified);

    } else if (RemoteConstants.ACTION_V1_PAYMENT_REJECTED.equals(intent.getAction())) {
      Payment payment = intent.getParcelableExtra(RemoteConstants.EXTRA_PAYMENT);
      VoidReason voidReason = intent.getParcelableExtra(RemoteConstants.EXTRA_VOID_REASON);
      onPaymentRejected(payment, voidReason);
    } else if (RemoteConstants.ACTION_V1_PAYMENT_CONFIRMED.equals(intent.getAction())) {
      Payment payment = intent.getParcelableExtra(RemoteConstants.EXTRA_PAYMENT);
      onPaymentConfirmed(payment);

    } else if (RemoteConstants.ACTION_V1_TERMINAL_MESSAGE.equals(intent.getAction())) {
      String text = intent.getStringExtra(RemoteConstants.EXTRA_TEXT);
      Integer duration = (Integer) intent.getSerializableExtra(RemoteConstants.EXTRA_MSG_DURATION);
      Integer messageId = (Integer) intent.getSerializableExtra(RemoteConstants.EXTRA_MSG_ID);
      onTerminalMessage(text, duration, messageId);
    } else if (RemoteConstants.ACTION_V1_BREAK.equals(intent.getAction())) {
      onBreak();
    } else if (RemoteConstants.ACTION_V1_PRINT_TEXT.equals(intent.getAction())) {
      List<String> textLines = intent.getStringArrayListExtra(RemoteConstants.EXTRA_TEXT_LINES);
      String externalPrintjobId = intent.getStringExtra(RemoteConstants.EXTRA_PRINTJOB_EXT_ID);
      Printer printer = intent.getParcelableExtra(RemoteConstants.EXTRA_PRINTJOB_PRINTER);
      onPrintText(externalPrintjobId, printer, textLines);
    } else if (RemoteConstants.ACTION_V1_SHOW_PAYMENT_RECEIPT_OPTIONS.equals(intent.getAction())) {
      String orderId = intent.getStringExtra(RemoteConstants.EXTRA_ORDER_ID);
      String paymentId = intent.getStringExtra(RemoteConstants.EXTRA_PAYMENT_ID);
      onShowPaymentReceiptOptions(orderId, paymentId);
    } else if (RemoteConstants.ACTION_V2_SHOW_PAYMENT_RECEIPT_OPTIONS.equals(intent.getAction())) {
      String orderId = intent.getStringExtra(RemoteConstants.EXTRA_ORDER_ID);
      String paymentId = intent.getStringExtra(RemoteConstants.EXTRA_PAYMENT_ID);
      boolean disableCloverPrinting = intent.getBooleanExtra(RemoteConstants.EXTRA_DISABLE_CLOVER_PRINTING, false);
      onShowPaymentReceiptOptionsV2(orderId, paymentId, disableCloverPrinting);
    } else if (RemoteConstants.ACTION_V1_PRINT_IMAGE.equals(intent.getAction())) {
      String externalPrintjobId = intent.getStringExtra(RemoteConstants.EXTRA_PRINTJOB_EXT_ID);
      Printer printer = intent.getParcelableExtra(RemoteConstants.EXTRA_PRINTJOB_PRINTER);
      if (intent.hasExtra(RemoteConstants.EXTRA_PNG)) {
        byte[] png = intent.getByteArrayExtra(RemoteConstants.EXTRA_PNG);
        Bitmap b = BitmapFactory.decodeByteArray(png, 0, png.length);
        onPrintImage(externalPrintjobId, printer, b);
      } else if (intent.hasExtra(RemoteConstants.EXTRA_IMG_URL)) {
        onPrintImage(externalPrintjobId, printer, intent.getStringExtra(RemoteConstants.EXTRA_IMG_URL));
      }
    } else if (RemoteConstants.ACTION_V1_SHOW_WELCOME.equals(intent.getAction())) {
      onShowWelcome();
    } else if (RemoteConstants.ACTION_V1_GET_PRINTERS_REQUEST.equals(intent.getAction())) {
      PrintCategory category = intent.getParcelableExtra(RemoteConstants.EXTRA_PRINTER_CATEGORY);
      onGetPrinters(category);
    } else if (RemoteConstants.ACTION_V1_PRINT_JOB_STATUS_REQUEST.equals(intent.getAction())) {
      String externalPrintjobId = intent.getStringExtra(RemoteConstants.EXTRA_PRINTJOB_EXT_ID);
      onGetPrintjobStatus(externalPrintjobId);
    } else if (RemoteConstants.ACTION_V1_SHOW_THANK_YOU.equals(intent.getAction())) {
      onShowThankYou();
    } else if (RemoteConstants.ACTION_V1_ORDER_ACTION_RESPONSE.equals(intent.getAction())) {
      OrderActionResponse orderActionResponse = intent.getParcelableExtra(RemoteConstants.EXTRA_ORDER_ACTION_RESPONSE);
      onOrderModified(orderActionResponse);
    } else if (RemoteConstants.ACTION_V1_REFUND.equals(intent.getAction())) {
      long amount = intent.getLongExtra(RemoteConstants.EXTRA_REFUND_AMOUNT, 0);
      String paymentId = intent.getStringExtra(RemoteConstants.EXTRA_PAYMENT_ID);
      String orderId = intent.getStringExtra(RemoteConstants.EXTRA_ORDER_ID);
      onRefundRequest(orderId, paymentId, amount);
    } else if (RemoteConstants.ACTION_V2_REFUND.equals(intent.getAction())) {
      boolean fullRefund = intent.getBooleanExtra(RemoteConstants.EXTRA_IS_FULL_REFUND, false);
      long amount = intent.getLongExtra(RemoteConstants.EXTRA_REFUND_AMOUNT, 0);
      String paymentId = intent.getStringExtra(RemoteConstants.EXTRA_PAYMENT_ID);
      String orderId = intent.getStringExtra(RemoteConstants.EXTRA_ORDER_ID);
      boolean disableCloverPrinting = intent.getBooleanExtra(RemoteConstants.EXTRA_DISABLE_CLOVER_PRINTING, false);
      boolean disableReceiptSelection = intent.getBooleanExtra(RemoteConstants.EXTRA_DISABLE_RECEIPT_SELECTION, false);
      onRefundRequestV2(orderId, paymentId, amount, fullRefund, disableCloverPrinting, disableReceiptSelection);
    } else if (RemoteConstants.ACTION_V1_OPEN_CASH_DRAWER.equals(intent.getAction())) {
      String reason = intent.getStringExtra(RemoteConstants.EXTRA_OPEN_CASH_DRAWER_REASON);
      Printer printer = intent.getParcelableExtra(RemoteConstants.EXTRA_PRINTER);
      onOpenCashdrawer(printer, reason);
    } else if (RemoteConstants.ACTION_V1_TIP_ADJUST.equals(intent.getAction())) {
      String paymentId = intent.getStringExtra(RemoteConstants.EXTRA_PAYMENT_ID);
      String orderId = intent.getStringExtra(RemoteConstants.EXTRA_ORDER_ID);
      long tipAmount = intent.getLongExtra(RemoteConstants.EXTRA_TIP_AMOUNT, 0);
      onTipAdjust(orderId, paymentId, tipAmount);
    } else if (RemoteConstants.ACTION_V1_VAULT_CARD_REQUEST.equals(intent.getAction())) {
      int cardEntryMethods = intent.getIntExtra(RemoteConstants.EXTRA_CARD_ENTRY_METHODS, 0);
      onVaultCard(cardEntryMethods);
    } else if (RemoteConstants.ACTION_V1_CARD_DATA_REQUEST.equals(intent.getAction())) {
      PayIntent payIntent = new PayIntent.Builder().intent(intent).build();
      onCardData(payIntent);
    } else if (RemoteConstants.ACTION_V1_CAPTURE_PREAUTH_REQUEST.equals(intent.getAction())) {
      long amount = intent.getLongExtra(RemoteConstants.EXTRA_AMOUNT, 0);
      long tipAmount = intent.getLongExtra(RemoteConstants.EXTRA_TIP_AMOUNT, 0);
      String paymentId = intent.getStringExtra(RemoteConstants.EXTRA_PAYMENT_ID);
      onCapturePreAuth(paymentId, amount, tipAmount);
    } else if (RemoteConstants.ACTION_V1_CLOSEOUT_REQUEST.equals(intent.getAction())) {
      boolean allowOpenTabs = intent.getBooleanExtra(RemoteConstants.EXTRA_ALLOW_OPEN_TABS, false);
      String batchId = intent.getStringExtra(RemoteConstants.EXTRA_BATCH_ID);
      onCloseout(allowOpenTabs, batchId);
    } else if (RemoteConstants.ACTION_V1_RETRIEVE_PENDING_PAYMENTS.equals(intent.getAction())) {
      onRetrievePendingPayments();
    } else if (RemoteConstants.ACTION_V1_RETRIEVE_PAYMENT.equals(intent.getAction())) {
      String externalPaymentId = intent.getStringExtra(RemoteConstants.EXTRA_EXTERNAL_PAYMENT_ID);
      onRetrievePayment(externalPaymentId);
    } else if (RemoteConstants.ACTION_V1_ACTIVITY_REQUEST.equals(intent.getAction())) {
      String action = intent.getStringExtra(RemoteConstants.EXTRA_ACTION);
      String payload = intent.getStringExtra(RemoteConstants.EXTRA_PAYLOAD);
      boolean nonBlocking = intent.getBooleanExtra(RemoteConstants.EXTRA_NON_BLOCKING, false);
      boolean forceLaunch = intent.getBooleanExtra(RemoteConstants.EXTRA_FORCE_LAUNCH, false);
      onActivityRequest(action, payload, nonBlocking, forceLaunch);
    } else if (RemoteConstants.ACTION_V1_RETRIEVE_DEVICE_STATUS_REQUEST.equals(intent.getAction())) {
      boolean sendLastMessage = intent.getBooleanExtra(RemoteConstants.EXTRA_DEVICE_STATUS_REQUEST_SENDLASTMESSAGE, false);
      boolean internal = intent.getBooleanExtra(RemoteConstants.EXTRA_DEVICE_STATUS_INTERNAL, false);
      int count = intent.getIntExtra(RemoteConstants.EXTRA_DEVICE_STATUS_INTERNAL_COUNT, 1);
      onRetrieveDeviceStatusRequest(sendLastMessage, internal, count);
    } else if (RemoteConstants.ACTION_V1_MESSAGE_TO_ACTIVITY.equals(intent.getAction())) {
      String action = intent.getStringExtra(RemoteConstants.EXTRA_ACTION);
      String payload = intent.getStringExtra(RemoteConstants.EXTRA_PAYLOAD);
      onMessageToActivity(action, payload);
    }

    onMessage();

  }


  //
  // actions
  //

  public void doTxState(TxState txState) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_TX_STATE);
    intent.putExtra(RemoteConstants.EXTRA_TX_STATE, (Parcelable) txState);
    sendRemoteTerminalBroadcast(intent);
  }


  public void doUiState(UiState uiState, String uiText, UiState.UiDirection uiDirection, InputOption... inputOptions) {


  }


  public void doRemoteError(final Throwable throwable,
                            final String errorMessage,
                            final Integer errorCode) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_REMOTE_ERROR);
    intent.putExtra(RemoteConstants.EXTRA_THROWABLE, throwable);
    intent.putExtra(RemoteConstants.EXTRA_ERROR_MSG, errorMessage);
    intent.putExtra(RemoteConstants.EXTRA_ERROR_CODE, errorCode);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doFinishOk(Payment payment, Signature2 signature, String requestInfo) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_FINISH_OK);
    intent.putExtra(RemoteConstants.EXTRA_PAYMENT, payment);
    intent.putExtra(RemoteConstants.EXTRA_SIGNATURE, signature);
    intent.putExtra(RemoteConstants.EXTRA_REQUEST_INFO, requestInfo);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doVerifySignature(Payment payment, Signature2 signature) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_VERIFY_SIGNATURE);
    intent.putExtra(RemoteConstants.EXTRA_PAYMENT, payment);
    intent.putExtra(RemoteConstants.EXTRA_SIGNATURE, signature);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doConfirmPayment(Payment payment, List<Challenge> challenges) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_CONFIRM_PAYMENT);
    intent.putExtra(RemoteConstants.EXTRA_PAYMENT, payment);
    if (!(challenges instanceof Serializable)) {
      challenges = new ArrayList<>(challenges);
    }
    intent.putExtra(RemoteConstants.EXTRA_CHALLENGES, (Serializable) challenges);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doFinishOk(Credit credit) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_FINISH_OK);
    intent.putExtra(RemoteConstants.EXTRA_CREDIT, credit);
    intent.putExtra(RemoteConstants.EXTRA_REQUEST_INFO, TxStartRequestMessage.CREDIT_REQUEST);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doFinishOk(Refund refund) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_FINISH_OK);
    intent.putExtra(RemoteConstants.EXTRA_REFUND, refund);
    intent.putExtra(RemoteConstants.EXTRA_REQUEST_INFO, TxStartRequestMessage.REFUND_REQUEST);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doFinishCancel(String requestInfo) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_FINISH_CANCEL);
    intent.putExtra(RemoteConstants.EXTRA_REQUEST_INFO, requestInfo);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doTipAdded(long tipAmount) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_ADD_TIP);
    intent.putExtra(RemoteConstants.EXTRA_TIP_AMOUNT, tipAmount);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doCashbackSelected(long cashbackAmount) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_CASHBACK_SELECTED);
    intent.putExtra(RemoteConstants.EXTRA_CASHBACK_AMOUNT, cashbackAmount);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doPartialAuth(long partialAuthAmount) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_PARTIAL_AUTH);
    intent.putExtra(RemoteConstants.EXTRA_PARTIAL_AUTH_AMOUNT, partialAuthAmount);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doPaymentVoided(Payment payment, VoidReason voidReason) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_PAYMENT_VOIDED);
    intent.putExtra(RemoteConstants.EXTRA_PAYMENT, payment);
    intent.putExtra(RemoteConstants.EXTRA_VOID_REASON, (Parcelable) voidReason);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doPrint(Payment payment, Order order) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_PRINT_PAYMENT);
    intent.putExtra(RemoteConstants.EXTRA_PAYMENT, payment);
    if (order != null) {
      intent.putExtra(RemoteConstants.EXTRA_ORDER, order);
    }
    sendRemoteTerminalBroadcast(intent);
  }

  public void doPrint(Payment payment, Refund refund, Order order) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_PRINT_REFUND);
    intent.putExtra(RemoteConstants.EXTRA_PAYMENT, payment);
    intent.putExtra(RemoteConstants.EXTRA_REFUND, refund);
    if (order != null) {
      intent.putExtra(RemoteConstants.EXTRA_ORDER, order);
    }
    sendRemoteTerminalBroadcast(intent);
  }

  public void doPrint(Credit credit) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_PRINT_CREDIT);
    intent.putExtra(RemoteConstants.EXTRA_CREDIT, credit);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doPrintDecline(Payment payment, String reason) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_PRINT_PAYMENT_DECLINE);
    intent.putExtra(RemoteConstants.EXTRA_PAYMENT, payment);
    intent.putExtra(RemoteConstants.EXTRA_DECLINE_REASON, reason);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doPrintDecline(Credit credit, String reason) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_PRINT_CREDIT_DECLINE);
    intent.putExtra(RemoteConstants.EXTRA_CREDIT, credit);
    intent.putExtra(RemoteConstants.EXTRA_DECLINE_REASON, reason);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doPrintMerchantCopy(Payment payment) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_PRINT_PAYMENT_MERCHANT_COPY);
    intent.putExtra(RemoteConstants.EXTRA_PAYMENT, payment);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doModifyOrder(AddDiscountAction addDiscountAction) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_MODIFY_ORDER);
    intent.putExtra(RemoteConstants.EXTRA_ADD_DISCOUNT_ACTION, addDiscountAction);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doModifyOrder(RemoveDiscountAction removeDiscountAction) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_MODIFY_ORDER);
    intent.putExtra(RemoteConstants.EXTRA_REMOVE_DISCOUNT_ACTION, removeDiscountAction);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doModifyOrder(AddLineItemAction addLineItemAction) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_MODIFY_ORDER);
    intent.putExtra(RemoteConstants.EXTRA_ADD_LINE_ITEM_ACTION, addLineItemAction);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doModifyOrder(RemoveLineItemAction removeLineItemAction) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_MODIFY_ORDER);
    intent.putExtra(RemoteConstants.EXTRA_REMOVE_LINE_ITEM_ACTION, removeLineItemAction);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doTxStartResponse(TxStartResponseResult result, Order order, String externalPaymentId, String requestInfo) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_TX_START_RESPONSE);
    if (order != null) {
      intent.putExtra(RemoteConstants.EXTRA_ORDER, order);
    }
    if (requestInfo != null) {
      intent.putExtra(RemoteConstants.EXTRA_REQUEST_INFO, requestInfo);
    }
    intent.putExtra(RemoteConstants.EXTRA_SUCCESS, result == TxStartResponseResult.SUCCESS);
    intent.putExtra(RemoteConstants.EXTRA_EXTERNAL_PAYMENT_ID, externalPaymentId);
    intent.putExtra(RemoteConstants.EXTRA_TX_START_RESPONSE_RESULT, result);

    sendRemoteTerminalBroadcast(intent);
  }

  public void doActivityResponse(int resultCode, String activityName, String payload, String reason) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_ACTIVITY_RESPONSE);
    intent.putExtra(RemoteConstants.EXTRA_RESULT_CODE, resultCode);
    intent.putExtra(RemoteConstants.EXTRA_PAYLOAD, payload);
    intent.putExtra(RemoteConstants.EXTRA_ACTION, activityName);
    intent.putExtra(RemoteConstants.EXTRA_FAIL_REASON, reason);

    sendRemoteTerminalBroadcast(intent);
  }

  public void doMessageFromActivity(String activityName, String payload) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_MESSAGE_FROM_ACTIVITY);
    intent.putExtra(RemoteConstants.EXTRA_PAYLOAD, payload);
    intent.putExtra(RemoteConstants.EXTRA_ACTION, activityName);
    sendRemoteTerminalBroadcast(intent);
  }



  public void doRefundResponse(String orderId, String paymentId, Refund refund, ErrorCode reason, String msg, TxState txState) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_REFUND_RESPONSE);
    intent.putExtra(RemoteConstants.EXTRA_REFUND, refund);
    intent.putExtra(RemoteConstants.EXTRA_ORDER_ID, orderId);
    intent.putExtra(RemoteConstants.EXTRA_PAYMENT_ID, paymentId);
    intent.putExtra(RemoteConstants.EXTRA_ERROR_CODE, (Parcelable) reason);
    intent.putExtra(RemoteConstants.EXTRA_ERROR_MSG, msg);
    intent.putExtra(RemoteConstants.EXTRA_TX_STATE, (Parcelable) txState);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doMessageComplete(int id) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_MSG_COMPLETE);
    intent.putExtra(RemoteConstants.EXTRA_MSG_ID, id);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doTipAdjustResponse(String orderId, String paymentId, long tipAmount, boolean success) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_TIP_ADJUST_RESPONSE);
    intent.putExtra(RemoteConstants.EXTRA_TIP_AMOUNT, tipAmount);
    intent.putExtra(RemoteConstants.EXTRA_ORDER_ID, orderId);
    intent.putExtra(RemoteConstants.EXTRA_PAYMENT_ID, paymentId);
    intent.putExtra(RemoteConstants.EXTRA_SUCCESS, success);
    sendRemoteTerminalBroadcast(intent);
  }

  /**
   * Used to resolve the case where the protocol receives a discovery message, and responds to it,
   * then a TX_START is received, but the broadcast of the message has no listeners yet. I found that
   * the kiosk was still in the process of initializing, and had not yet registered interest in the
   * message.
   */
  public void doReadyToProcessMessages() {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_KIOSK_READY);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doVaultCardResponse(ResultStatus status, String reason, VaultedCard card) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_VAULT_CARD_RESPONSE);
    intent.putExtra(RemoteConstants.EXTRA_CARD, card);
    intent.putExtra(RemoteConstants.EXTRA_RESULT_STATUS, (Parcelable) status);
    intent.putExtra(RemoteConstants.EXTRA_FAIL_REASON, reason);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doCardDataResponse(ResultStatus status, String reason, PaymentRequestCardDetails details) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_CARD_DATA_RESPONSE);
    intent.putExtra(RemoteConstants.EXTRA_CARD_DETAILS, details);
    intent.putExtra(RemoteConstants.EXTRA_RESULT_STATUS, (Parcelable) status);
    intent.putExtra(RemoteConstants.EXTRA_FAIL_REASON, reason);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doCapturePreAuthResponse(ResultStatus status,
                                       String reason,
                                       String paymentId,
                                       long amount,
                                       long tipAmount) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_CAPTURE_PREAUTH_RESPONSE);
    intent.putExtra(RemoteConstants.EXTRA_RESULT_STATUS, (Parcelable) status);
    intent.putExtra(RemoteConstants.EXTRA_FAIL_REASON, reason);
    intent.putExtra(RemoteConstants.EXTRA_PAYMENT_ID, paymentId);
    intent.putExtra(RemoteConstants.EXTRA_AMOUNT, amount);
    intent.putExtra(RemoteConstants.EXTRA_TIP_AMOUNT, tipAmount);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doCloseoutResponse(ResultStatus status,
                                 String reason,
                                 Batch batch) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_CLOSEOUT_RESPONSE);
    intent.putExtra(RemoteConstants.EXTRA_RESULT_STATUS, (Parcelable) status);
    intent.putExtra(RemoteConstants.EXTRA_FAIL_REASON, reason);
    intent.putExtra(RemoteConstants.EXTRA_BATCH, batch);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doRetrievePendingPaymentsResponse(ResultStatus status,
                                                String reason,
                                                List<Payment> payments) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_RETRIEVE_PENDING_PAYMENTS_RESPONSE);
    intent.putExtra(RemoteConstants.EXTRA_RESULT_STATUS, (Parcelable) status);
    intent.putExtra(RemoteConstants.EXTRA_FAIL_REASON, reason);
    intent.putParcelableArrayListExtra(RemoteConstants.EXTRA_PENDING_PAYMENTS, (ArrayList<Payment>) payments);
    sendRemoteTerminalBroadcast(intent);
  }


  public void doRetrievePaymentResponse(ResultStatus status,
                                        String reason,
                                        String externalId,
                                        QueryStatus queryStatus,
                                        Payment payment) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_RETRIEVE_PAYMENT_RESPONSE);
    intent.putExtra(RemoteConstants.EXTRA_RESULT_STATUS, (Parcelable) status);
    intent.putExtra(RemoteConstants.EXTRA_FAIL_REASON, reason);
    intent.putExtra(RemoteConstants.EXTRA_EXTERNAL_PAYMENT_ID, externalId);
    intent.putExtra(RemoteConstants.EXTRA_QUERY_STATUS, (Parcelable) queryStatus);
    intent.putExtra(RemoteConstants.EXTRA_PAYMENT, payment);
    sendRemoteTerminalBroadcast(intent);
  }


  public void doDeviceStatusResponse(ResultStatus status,
                                     String reason, ExternalDeviceState state, ExternalDeviceSubState substate, ExternalDeviceStateData data,
                                     boolean internal, int count) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_RETRIEVE_DEVICE_STATUS_RESPONSE);
    intent.putExtra(RemoteConstants.EXTRA_RESULT_STATUS, (Parcelable) status);
    intent.putExtra(RemoteConstants.EXTRA_FAIL_REASON, reason);
    intent.putExtra(RemoteConstants.EXTRA_DEVICE_STATUS_STATE, (Parcelable) state);
    intent.putExtra(RemoteConstants.EXTRA_DEVICE_STATUS_SUBSTATE, (Parcelable) substate);
    intent.putExtra(RemoteConstants.EXTRA_DEVICE_STATUS_DATA, data);
    intent.putExtra(RemoteConstants.EXTRA_DEVICE_STATUS_INTERNAL, internal);
    intent.putExtra(RemoteConstants.EXTRA_DEVICE_STATUS_INTERNAL_COUNT, count);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doDeviceResetResponse(ResultStatus status,
                                    String reason, ExternalDeviceState state) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_RESET_DEVICE_RESPONSE);
    intent.putExtra(RemoteConstants.EXTRA_RESULT_STATUS, (Parcelable) status);
    intent.putExtra(RemoteConstants.EXTRA_FAIL_REASON, reason);
    intent.putExtra(RemoteConstants.EXTRA_DEVICE_STATUS_STATE, (Parcelable) state);

    sendRemoteTerminalBroadcast(intent);
  }

  public void doShowConfiguration() {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_SHOW_CONFIGURATION_COMMAND);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doPrintJobStatusResponse(String externalPrintjobId, String currentPrintJobStatus) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_PRINT_JOB_STATUS_RESPONSE);
    intent.putExtra(RemoteConstants.EXTRA_PRINTJOB_EXT_ID, externalPrintjobId);
    intent.putExtra(RemoteConstants.EXTRA_PRINTJOB_STATUS, currentPrintJobStatus);
    sendRemoteTerminalBroadcast(intent);
  }

  public void doGetPrintersResponse(ArrayList<Printer> printers) {
    Intent intent = new Intent(RemoteConstants.ACTION_V1_GET_PRINTERS_RESPONSE);
    intent.putParcelableArrayListExtra(RemoteConstants.EXTRA_PRINTERS, printers);
    sendRemoteTerminalBroadcast(intent);
  }


  private void sendRemoteTerminalBroadcast(Intent intent) {
    ALog.i(this, "Sending %s", intent);
    // SEMI-726 :  Temporary band-aid until we get the permission ownership moved to the service.
    //context.sendBroadcast(intent, Intents.PERMISSION_REMOTE_TERMINAL);
    context.sendBroadcast(intent);
  }


  //
  // callbacks
  //

  public void onMessage() {
  }

  protected abstract void onActivityRequest(String action, String payload, boolean nonBlocking, boolean forceLaunch);

  protected abstract void onMessageToActivity(String action, String payload);

  protected abstract void onTxStartRequest(PayIntent payIntent, Order order, boolean suppressOnScreenTips); //deprecated

  protected abstract void onTxStartRequest(PayIntent payIntent, Order order, String requestInfo);

  protected abstract void onKeyPress(KeyPress keyPress);

  protected abstract void onVoidPayment(Payment payment, VoidReason reason, String packageName);

  protected abstract void onShowWelcome();

  protected abstract void onGetPrinters(PrintCategory category);

  protected abstract void onGetPrintjobStatus(String externalPrintjobId);

  protected abstract void onShowOrder(DisplayOrder order, Object operation, boolean isOrderModificationSupported);

  protected abstract void onShowThankYou();

  protected abstract void onSignatureVerified(Payment payment, boolean verified);

  protected abstract void onTerminalMessage(String text, Integer durationInMillis, Integer messageId);

  protected abstract void onBreak();

  protected abstract void onPrintText(String externalPrintjobId, Printer printer, List<String> textLines);

  protected abstract void onShowPaymentReceiptOptions(String orderId, String paymentId);

  protected abstract void onShowPaymentReceiptOptionsV2(String orderId, String paymentId, boolean disableCloverPrinting);

  protected abstract void onPrintImage(String externalPrintjobId, Printer printer, Bitmap bitmap);

  protected abstract void onPrintImage(String externalPrintjobId, Printer printer, String urlString);

  protected abstract void onOrderModified(OrderActionResponse orderActionResponse);

  protected abstract void onRefundRequest(String orderId, String paymentId, long amount);

  protected abstract void onRefundRequestV2(String orderId, String paymentId, long amount, boolean fullRefund, boolean disableCloverPrinting, boolean disableReceiptSelection);

  protected abstract void onOpenCashdrawer(Printer printer, String reason);

  protected abstract void onTipAdjust(String orderId, String paymentId, long tipAmount);

  protected abstract void onVaultCard(int cardEntryMethods);

  protected abstract void onCardData(PayIntent payIntent);

  protected abstract void onCapturePreAuth(String paymentId, long amount, long tipAmount);

  protected abstract void onCloseout(boolean allowOpenTabs, String batchId);

  protected abstract void onPaymentConfirmed(Payment payment);

  protected abstract void onPaymentRejected(Payment payment, VoidReason voidReason);

  protected abstract void onRetrievePendingPayments();

  protected abstract void onRetrievePayment(String externalPaymentId);

  protected abstract void onRetrieveDeviceStatusRequest(boolean sendLastMessage, boolean internal, int count);



}
