package com.clover.pidgin_test_native.remote_terminal_kiosk;

import com.clover.common.analytics.ALog;
import com.clover.common.metrics.Counters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by connor on 11/6/17.
 */
public abstract class RemoteControl extends BroadcastReceiver {

  private static final Set<CharSequence> COUNTED_ACTIONS = new HashSet<CharSequence>() {{
    add(RemoteConstants.ACTION_V1_TX_STATE);
    add(RemoteConstants.ACTION_V1_VOID_PAYMENT);
    add(RemoteConstants.ACTION_V1_TX_START_REQUEST);
    add(RemoteConstants.ACTION_V2_TX_START_REQUEST);
    add(RemoteConstants.ACTION_V1_TX_START_RESPONSE);
    add(RemoteConstants.ACTION_V1_FINISH_OK);
    add(RemoteConstants.ACTION_V1_FINISH_CANCEL);
  }};

  protected final Context context;
  private boolean registered;

  public RemoteControl(Context context) {
    this.context = context;
    this.registered = false;
  }

  public void register() {
    register(null);
  }

  public void register(Handler handler) {
    if (!registered) {
      context.registerReceiver(this, getIntentFilter(), Intents.PERMISSION_REMOTE_TERMINAL, handler);
      registered = true;
      ALog.i(this, "Registered RemoteControl class %s", getClass());
    }
  }

  public void unregister() {
    if (registered) {
      context.unregisterReceiver(this);
      registered = false;
      ALog.i(this, "Unregistered RemoteControl class %s", getClass());
    }
  }

  protected abstract IntentFilter getIntentFilter();

  @Override
  public void onReceive(Context context, Intent intent) {
    //countAction(intent);
  }

  private static final Pattern ACTION_PATTERN = Pattern.compile("(?:([^.]+)\\.?)+");

  private static String getRelative(CharSequence action) {
    Matcher m = ACTION_PATTERN.matcher(action);
    if (!m.find()) {
      return null;
    }
    return m.group(1);
  }

  /*protected void countAction(Intent intent) {
    if (!COUNTED_ACTIONS.contains(intent.getAction())) {
      return;
    }

    String relativeAction = getRelative(intent.getAction());
    if (relativeAction == null) {
      return;
    }

    String key = String.format("action.%s", relativeAction.toLowerCase());

    // special handling for particular actions
    if (RemoteConstants.ACTION_V1_TX_STATE.equals(intent.getAction())) {
      TxState txState = intent.getParcelableExtra(RemoteConstants.EXTRA_TX_STATE);
      key += "." + txState.name().toLowerCase();
    }

    Counters.instance(context).increment(key);
  }*/
}
