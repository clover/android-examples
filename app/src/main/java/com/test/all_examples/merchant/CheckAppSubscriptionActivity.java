package com.test.all_examples.merchant;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.clover.sdk.internal.util.Strings;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v3.apps.App;
import com.clover.sdk.v3.apps.AppMetered;
import com.clover.sdk.v3.apps.AppSubscription;
import com.clover.sdk.v3.apps.AppsConnector;
import com.test.all_examples.R;

public class CheckAppSubscriptionActivity extends Activity {

  private static final String SUBSCRIPTION_PACKAGE_NAME = "com.example.zachsubscriptionapp.app";
  private static final String TARGET_SUBSCRIPTION = "THIS IS YOUR TARGET SUBSCRIPTION ID";
  private static final String SUBSCRIPTION_APP_ID = "PGGD0KS56PJB8";
  private static final int RESULT_CODE = 0;
  private Account account;
  private App appObject;
  private AppsConnector connector;
  private TextView appName;
  private TextView currentSubscription;
  private LinearLayout subscriptions;
  private LinearLayout metereds;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_check_app_subscription);

    appName = (TextView) findViewById(R.id.app_name);
    currentSubscription = (TextView) findViewById(R.id.current_subscription);
    subscriptions = (LinearLayout) findViewById(R.id.subscriptions);
    metereds = (LinearLayout) findViewById(R.id.metereds);

    Button launchAppstoreViaPackageName = (Button) findViewById(R.id.launch_app_package);
    Button launchAppstoreViaAppId = (Button) findViewById(R.id.launch_app_id);
    Button launchAppstoreViaAppObject = (Button) findViewById(R.id.launch_app_obj);
    Button upgradeSubscription = (Button) findViewById(R.id.change_subscription_btn);

    launchAppstoreViaPackageName.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent();
        intent.setAction(Intents.ACTION_START_APP_DETAIL);
        intent.putExtra(Intents.EXTRA_APP_PACKAGE_NAME, SUBSCRIPTION_PACKAGE_NAME);
        startActivityForResult(intent, RESULT_CODE);
      }
    });

    launchAppstoreViaAppId.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent();
        intent.setAction(Intents.ACTION_START_APP_DETAIL);
        // Pass in the App's ID
        intent.putExtra(Intents.EXTRA_APP_ID, SUBSCRIPTION_APP_ID);
        startActivityForResult(intent, RESULT_CODE);
      }
    });

    launchAppstoreViaAppObject.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent();
        intent.setAction(Intents.ACTION_START_APP_DETAIL);
        // Pass in the whole app object
        intent.putExtra(Intents.EXTRA_APP, appObject);
        startActivityForResult(intent, RESULT_CODE);
      }
    });

    upgradeSubscription.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent();
        intent.setAction(Intents.ACTION_START_APP_DETAIL);
        intent.putExtra(Intents.EXTRA_APP_PACKAGE_NAME, SUBSCRIPTION_PACKAGE_NAME);
        // Pass in one of the App's Subscription Id
        intent.putExtra(Intents.EXTRA_TARGET_SUBSCRIPTION_ID, TARGET_SUBSCRIPTION);
        startActivityForResult(intent, RESULT_CODE);
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RESULT_CODE && resultCode == RESULT_OK && data != null && TARGET_SUBSCRIPTION.equals(data.getStringExtra(Intents.EXTRA_RESULT_SUBSCRIPTION_ID))) {
      Toast.makeText(this, "Upgraded", Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(this, "Not Upgraded", Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    // Retrieve the Clover account
    if (account == null) {
      account = CloverAccount.getAccount(this);

      if (account == null) {
        Toast.makeText(this, getString(R.string.no_account), Toast.LENGTH_SHORT).show();
        finish();
        return;
      }
    }

    connect();

    // Get the app object from AppsConnector and populate the UI fields
    getAppObject();
  }

  @Override
  protected void onPause() {
    disconnect();
    super.onPause();
  }

  private void connect() {
    disconnect();
    if (account != null) {
      connector = new AppsConnector(this, account);
      connector.connect();
    }
  }

  private void disconnect() {
    if (connector != null) {
      connector.disconnect();
      connector = null;
    }
  }

  private void getAppObject() {
    new AsyncTask<Void, Void, App>() {

      @Override
      protected App doInBackground(Void... params) {
        try {
          if (connector != null) {
            return connector.getApp();
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        return null;
      }

      @Override
      protected void onPostExecute(App app) {
        super.onPostExecute(app);

        if (!isFinishing() && app != null) {
          appObject = app;
          appName.setText(getString(R.string.merchant_app_name, app.getName()));
          if (app.getCurrentSubscription() != null) {
            AppSubscription subscription = app.getCurrentSubscription();
            if (Strings.isNullOrEmpty(subscription.getDescription())) {
              currentSubscription.setText(subscription.getName());
            } else {
              currentSubscription.setText(getString(R.string.colon_separated_strings, subscription.getName(), subscription.getDescription()));
            }
          }
          if (app.getAvailableSubscriptions() != null) {
            subscriptions.removeAllViews();
            for (AppSubscription subscription : app.getAvailableSubscriptions()) {
              // Only display active subscriptions
              if (subscription.getActive()) {
                TextView item = (TextView) getLayoutInflater().inflate(R.layout.item_text_view, null);
                if (Strings.isNullOrEmpty(subscription.getDescription())) {
                  item.setText(subscription.getName());
                } else {
                  item.setText(getString(R.string.colon_separated_strings, subscription.getName(), subscription.getDescription()));
                }
                subscriptions.addView(item);
              }
            }
          }

          if (app.getAvailableMetereds() != null) {
            metereds.removeAllViews();
            for (AppMetered metered : app.getAvailableMetereds()) {
              // Only display active subscriptions
              if (metered.getActive()) {
                TextView item = (TextView) getLayoutInflater().inflate(R.layout.item_text_view, null);
                item.setText(getString(R.string.metered_string, metered.getAmount(), metered.getAction()));
                metereds.addView(item);
              }
            }
          }
        }
      }
    }.execute();
  }
}
