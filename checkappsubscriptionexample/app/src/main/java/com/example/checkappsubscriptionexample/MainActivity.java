package com.example.checkappsubscriptionexample;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.clover.sdk.internal.util.Strings;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.apps.App;
import com.clover.sdk.v3.apps.AppMetered;
import com.clover.sdk.v3.apps.AppSubscription;
import com.clover.sdk.v3.apps.AppsConnector;

public class MainActivity extends Activity {

  private Account account;
  private App appObject;
  private AppsConnector connector;
  private TextView appName;
  private TextView currentSubscription;
  private LinearLayout subscriptions;
  private LinearLayout metereds;
  private Button launchAppstoreViaPackageName;
  private Button launchAppstoreViaAppId;
  private Button launchAppstoreViaAppObject;
  private Button upgradeSubscription;
  private static final int RESULT_CODE = 0;
  private static final String TARGET_SUBSCRIPTION = "THIS IS YOUR TARGET SUBSCRIPTION ID";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    appName = (TextView) findViewById(R.id.app_name);
    currentSubscription = (TextView) findViewById(R.id.current_subscription);
    subscriptions = (LinearLayout) findViewById(R.id.subscriptions);
    metereds = (LinearLayout) findViewById(R.id.metereds);

    launchAppstoreViaPackageName = (Button) findViewById(R.id.launch_app_package);

    launchAppstoreViaAppId = (Button) findViewById(R.id.launch_app_id);

    launchAppstoreViaAppObject = (Button) findViewById(R.id.launch_app_obj);

    upgradeSubscription = (Button) findViewById(R.id.change_subscription_btn);

    launchAppstoreViaPackageName.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent();
        intent.setAction(Intents.ACTION_START_APP_DETAIL);
        intent.putExtra(Intents.EXTRA_APP_PACKAGE_NAME, "com.example.zachsubscriptionapp.app");
        startActivityForResult(intent, RESULT_CODE);
      }
    });

    launchAppstoreViaAppId.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent();
        intent.setAction(Intents.ACTION_START_APP_DETAIL);
        // Pass in the App's ID
        intent.putExtra(Intents.EXTRA_APP_ID, "PGGD0KS56PJB8");
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
        intent.putExtra(Intents.EXTRA_APP_PACKAGE_NAME, "com.example.zachsubscriptionapp.app");
        // Pass in one of the App's Subscription Id
        intent.putExtra(Intents.EXTRA_TARGET_SUBSCRIPTION_ID, TARGET_SUBSCRIPTION);
        startActivityForResult(intent, RESULT_CODE);
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RESULT_CODE && resultCode == RESULT_OK && data != null && TARGET_SUBSCRIPTION.equals(data.getExtras().getString(Intents.EXTRA_RESULT_SUBSCRIPTION_ID))) {
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
        } catch (RemoteException e) {
          e.printStackTrace();
        } catch (ServiceException e) {
          e.printStackTrace();
        } catch (BindingException e) {
          e.printStackTrace();
        } catch (ClientException e) {
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
