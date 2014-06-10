package com.example.checkappsubscriptionexample;

import android.accounts.Account;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.clover.sdk.internal.util.Strings;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.apps.App;
import com.clover.sdk.v3.apps.AppMetered;
import com.clover.sdk.v3.apps.AppSubscription;
import com.clover.sdk.v3.apps.AppsConnector;


public class MainActivity extends Activity {

  private Account account;
  private AppsConnector connector;
  private TextView appName;
  private TextView currentSubscription;
  private LinearLayout subscriptions;
  private LinearLayout metereds;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    appName = (TextView) findViewById(R.id.app_name);
    currentSubscription = (TextView) findViewById(R.id.current_subscription);
    subscriptions = (LinearLayout) findViewById(R.id.subscriptions);
    metereds = (LinearLayout) findViewById(R.id.metereds);
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

    // get the app object from AppsConnector and populate the UI fields
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
