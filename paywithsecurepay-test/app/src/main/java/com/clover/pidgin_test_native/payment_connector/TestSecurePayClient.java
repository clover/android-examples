package com.clover.pidgin_test_native.payment_connector;

import com.clover.common.analytics.ALog;

import com.clover.remote.InputOption;
import com.clover.remote.Intents;
import com.clover.remote.KeyPress;
import com.clover.remote.TxState;
import com.clover.remote.UiState;
import com.clover.sdk.v3.order.VoidReason;
import com.clover.sdk.v3.payments.Credit;
import com.clover.sdk.v3.payments.Payment;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by connor on 11/2/17.
 */
public abstract class TestSecurePayClient extends TestSecurePay {
  public TestSecurePayClient(Context context) {
    super(context);
  }

  @Override
  protected IntentFilter getIntentFilter() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(ACTION_V1_UI_STATE);
    return filter;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (ACTION_V1_UI_STATE.equals(intent.getAction())) {
      UiState uiState = (UiState) intent.getSerializableExtra(EXTRA_UI_STATE);
      String uiText = intent.getStringExtra(EXTRA_UI_TEXT);
      UiState.UiDirection uiDirection = (UiState.UiDirection) intent.getSerializableExtra(EXTRA_UI_DIRECTION);
      ArrayList<InputOption> inputOptions = intent.getParcelableArrayListExtra(EXTRA_INPUT_OPTIONS);
      onUiState(uiState, uiText, uiDirection, inputOptions == null ? null : inputOptions.toArray(new InputOption[inputOptions.size()]));
    }
  }

  protected abstract void onTxState(TxState txState);

  protected abstract void onUiState(UiState uiState, String uiText, UiState.UiDirection uiDirection, InputOption... inputOptions);

  protected abstract void onPaymentVoided(Payment payment, VoidReason voidReason);

  public abstract void onPrintMerchantCopy(Payment payment);

  public abstract void onPrintDecline(Payment payment, String reason);

  public abstract void onPrintDecline(Credit credit, String reason);

  public abstract void onCashbackSelected(long cashbackAmount);

  public abstract void onPartialAuth(long partialAuthAmount);

  //
  // actions
  //


  public void doKeyPress(KeyPress keyPress) {

     Intent intent = new Intent(ACTION_V1_KEYPRESS);
    intent.putExtra(EXTRA_KEYPRESS, (Parcelable) keyPress);
    sendBroadcast(intent);


  }

  public void doBreak() {
    Intent intent = new Intent(ACTION_V1_BREAK);
    if (context.getPackageManager().queryBroadcastReceivers(intent, 0).size() > 0) {
      sendBroadcast(intent);
    } else {
      //legacy behavior
      doKeyPress(KeyPress.ESC);
    }
  }

  public void doVoidPayment(Payment payment, VoidReason reason, String packageName) {
    Intent intent = new Intent(ACTION_V1_VOID_PAYMENT);
    intent.putExtra(EXTRA_PAYMENT_ID, payment.getId());
    intent.putExtra(EXTRA_EMPLOYEE_ID, payment.getEmployee().getId());
    intent.putExtra(EXTRA_ORDER_ID, payment.getOrder().getId());
    intent.putExtra(EXTRA_VOID_REASON, (Parcelable) reason);
    intent.putExtra(EXTRA_PACKAGE_NAME, packageName);
    sendBroadcast(intent);

    // legacy, can be removed after next ROM update
    intent.setAction(ACTION_V0_VOID_PAYMENT);
    sendBroadcast(intent);
  }

  private void sendBroadcast(Intent intent) {
    ALog.i(this, "Sending: %s", intent);
    context.sendBroadcast(intent, Intents.PERMISSION_REMOTE_TERMINAL);
  }
}
