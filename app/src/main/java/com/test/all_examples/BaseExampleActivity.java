package com.test.all_examples;

import android.accounts.Account;
import android.app.Activity;
import com.clover.sdk.util.CloverAccount;

/**
 * Created by mmaietta on 3/10/16.
 */
public class BaseExampleActivity extends Activity {

  protected Account account;

  @Override
  protected void onResume() {
    super.onResume();

    account = CloverAccount.getAccount(this);
  }
}
