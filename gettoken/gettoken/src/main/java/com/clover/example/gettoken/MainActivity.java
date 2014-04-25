package com.clover.example.gettoken;

import android.accounts.Account;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.util.CloverAuth;


public class MainActivity extends Activity {
  private static String TAG = "MainActivity";

  private Account mAccount;
  private CloverAuth.AuthResult mCloverAuth;

  private TextView mToken;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Find all the TextView objects
    mToken = (TextView) findViewById(R.id.token);
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (mAccount == null) {
      mAccount = CloverAccount.getAccount(this);

      if (mAccount == null) {
        Toast.makeText(this, getString(R.string.no_account), Toast.LENGTH_SHORT).show();
        finish();
      }

      getCloverAuth();
    }
  }

  private void getCloverAuth() {
    new AsyncTask<Void, Void, CloverAuth.AuthResult>() {
      @Override
      protected CloverAuth.AuthResult doInBackground(Void... params) {
        try {
          return CloverAuth.authenticate(MainActivity.this, mAccount);
        } catch (OperationCanceledException e) {
          Log.e(TAG, "Authentication cancelled", e);
        } catch (Exception e) {
          Log.e(TAG, "Error retrieving authentication", e);
        }
        return null;
      }

      @Override
      protected void onPostExecute(CloverAuth.AuthResult result) {
        mCloverAuth = result;
        if (mCloverAuth != null && mCloverAuth.authToken !=null) {
          mToken.setText(getString(R.string.token) + mCloverAuth.authToken);
        } else {
          mToken.setText(getString(R.string.auth_error));
        }
      }
    }.execute();
  }
}