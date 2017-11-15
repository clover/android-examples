package com.clover.native_pidgin_test.remote_terminal_kiosk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;

/**
 * Created by connor on 11/13/17.
 */
public abstract class SecurePay extends BroadcastReceiver {
  public static final String ACTION_V1_TX_STATE = "com.clover.remote.terminal.securepay.action.V1_TX_STATE";
  public static final String ACTION_V1_UI_STATE = "com.clover.remote.terminal.securepay.action.V1_UI_STATE";
  public static final String ACTION_V1_FINISH_OK = "com.clover.remote.terminal.securepay.action.V1_FINISH_OK";
  public static final String ACTION_V1_FINISH_CANCEL = "com.clover.remote.terminal.securepay.action.V1_FINISH_CANCEL";
  public static final String ACTION_V1_VOID_PAYMENT = "com.clover.remote.terminal.securepay.action.V1_VOID_PAYMENT";
  public static final String ACTION_V0_VOID_PAYMENT = "com.clover.remote.terminal.securepay.action.VOID_PAYMENT";
  public static final String ACTION_V1_TX_START = "com.clover.remote.terminal.securepay.action.V1_TX_START";
  public static final String ACTION_V1_BREAK = "com.clover.remote.terminal.securepay.action.V1_BREAK";
  public static final String ACTION_V1_KEYPRESS = "com.clover.remote.terminal.securepay.action.V1_INPUT";
  public static final String ACTION_V1_PAYMENT_VOIDED = "com.clover.remote.terminal.securepay.action.V1_PAYMENT_VOIDED";
  public static final String ACTION_V2_PAYMENT_VOIDED = "com.clover.remote.terminal.securepay.action.V2_PAYMENT_VOIDED";
  public static final String ACTION_V1_PRINT_PAYMENT_MERCHANT_COPY = "com.clover.remote.terminal.securepay.action.V1_PRINT_MERCHANT_COPY";
  public static final String ACTION_V1_PRINT_PAYMENT_DECLINE = "com.clover.remote.terminal.securepay.action.V1_PRINT_PAYMENT_DECLINE";
  public static final String ACTION_V1_PRINT_CREDIT_DECLINE = "com.clover.remote.terminal.securepay.action.V1_PRINT_CREDIT_DECLINE";
  public static final String ACTION_V1_CASHBACK_SELECTED = "com.clover.remote.terminal.securepay.action.CASHBACK_SELECTED";
  public static final String ACTION_V1_PARTIAL_AUTH = "com.clover.remote.terminal.securepay.action.PARTIAL_AUTH";
  public static final String EXTRA_UI_STATE = "com.clover.remote.terminal.securepay.extra.UI_STATE";
  public static final String EXTRA_UI_TEXT = "com.clover.remote.terminal.securepay.extra.UI_TEXT";
  public static final String EXTRA_UI_DIRECTION = "com.clover.remote.terminal.securepay.extra.UI_DIRECTION";
  public static final String EXTRA_TX_STATE = "com.clover.remote.terminal.securepay.extra.TX_STATE";
  public static final String EXTRA_INPUT_OPTIONS = "com.clover.remote.terminal.securepay.extra.INPUT_OPTIONS";
  public static final String EXTRA_PAYMENT = "com.clover.remote.terminal.securepay.extra.PAYMENT";
  public static final String EXTRA_CREDIT = "com.clover.remote.terminal.securepay.extra.CREDIT";
  public static final String EXTRA_PAYMENT_ID = "com.clover.remote.terminal.securepay.extra.PAYMENT_ID";
  public static final String EXTRA_ORDER_ID = "com.clover.remote.terminal.securepay.extra.ORDER_ID";
  public static final String EXTRA_VOID_REASON = "com.clover.remote.terminal.securepay.extra.VOID_REASON";
  public static final String EXTRA_VOID_STATUS = "com.clover.remote.terminal.securepay.extra.VOID_STATUS";
  public static final String EXTRA_PACKAGE_NAME = "com.clover.remote.terminal.securepay.extra.PACKAGE_NAME";
  public static final String EXTRA_EMPLOYEE_ID = "com.clover.remote.terminal.securepay.extra.EMPLOYEE_ID";
  public static final String EXTRA_KEYPRESS = "com.clover.remote.terminal.securepay.extra.KEY_PRESS";
  public static final String EXTRA_DECLINE_REASON = "com.clover.remote.terminal.securepay.extra.DECLINE_REASON";
  public static final String EXTRA_CASHBACK_AMOUNT = "com.clover.remote.terminal.securepay.extra.CASHBACK_SELECTED";
  public static final String EXTRA_PARTIAL_AUTH_AMOUNT = "com.clover.remote.terminal.securepay.extra.PARTIAL_AUTH_AMOUNT";
  public static final String PERMISSION_REMOTE_TERMINAL = "com.clover.remote.terminal.permission.REMOTE_TERMINAL";
  protected final Context context;
  private boolean registered;

  public SecurePay(Context context) {
    this.context = context;
    this.registered = false;
  }

  public void register() {
    if(!this.registered) {
      this.context.registerReceiver(this, this.getIntentFilter(), "com.clover.remote.terminal.permission.REMOTE_TERMINAL", (Handler)null);
      this.registered = true;
    }

  }

  public void unregister() {
    if(this.registered) {
      this.context.unregisterReceiver(this);
      this.registered = false;
    }

  }

  protected abstract IntentFilter getIntentFilter();
}
