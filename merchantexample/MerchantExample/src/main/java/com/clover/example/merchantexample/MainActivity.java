package com.clover.example.merchantexample;

import android.accounts.Account;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.merchant.Merchant;
import com.clover.sdk.v1.merchant.MerchantConnector;


public class MainActivity extends Activity {

  private MerchantConnector merchantConnector;

  private TextView merchantName;
  private TextView address1;
  private TextView address2;
  private TextView address3;
  private TextView city;
  private TextView state;
  private TextView zip;
  private TextView country;
  private TextView phone;
  private Account account;

  private ProgressBar progressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Find all the TextView objects
    merchantName = (TextView) findViewById(R.id.merchant_name);
    address1 = (TextView) findViewById(R.id.merchant_address1);
    address2 = (TextView) findViewById(R.id.merchant_address2);
    address3 = (TextView) findViewById(R.id.merchant_address3);
    city = (TextView) findViewById(R.id.merchant_city);
    state = (TextView) findViewById(R.id.merchant_state);
    zip = (TextView) findViewById(R.id.merchant_zip);
    country = (TextView) findViewById(R.id.merchant_country);
    phone = (TextView) findViewById(R.id.merchant_phone);

    progressBar = (ProgressBar) findViewById(R.id.progressBar);
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

    // Create and Connect to the MerchantConnector
    connect();

    // Get the merchant object
    getMerchant();
  }


  @Override
  protected void onPause() {
    disconnect();
    super.onPause();
  }

  private void connect() {
    disconnect();
    if (account != null) {
      merchantConnector = new MerchantConnector(this, account, null);
      merchantConnector.connect();
    }
  }

  private void disconnect() {
    if (merchantConnector != null) {
      merchantConnector.disconnect();
      merchantConnector = null;
    }
  }

  private void getMerchant() {

    new AsyncTask<Void, Void, Merchant>() {

      @Override
      protected void onPreExecute() {
        super.onPreExecute();

        // Show progressBar while waiting
        progressBar.setVisibility(View.VISIBLE);
      }

      @Override
      protected Merchant doInBackground(Void... params) {
        Merchant merchant = null;
        try {
          merchant = merchantConnector.getMerchant();
        } catch (RemoteException e) {
          e.printStackTrace();
        } catch (ClientException e) {
          e.printStackTrace();
        } catch (ServiceException e) {
          e.printStackTrace();
        } catch (BindingException e) {
          e.printStackTrace();
        }
        return merchant;
      }

      @Override
      protected void onPostExecute(Merchant merchant) {
        super.onPostExecute(merchant);

        if (!isFinishing()) {
          // Populate the merchant information
          if (merchant != null) {
            merchantName.setText(merchant.getName());
            address1.setText(merchant.getAddress().getAddress1());
            address2.setText(merchant.getAddress().getAddress2());
            address3.setText(merchant.getAddress().getAddress3());
            city.setText(merchant.getAddress().getCity());
            state.setText(merchant.getAddress().getState());
            zip.setText(merchant.getAddress().getZip());
            country.setText(merchant.getAddress().getCountry());
            phone.setText(merchant.getPhoneNumber());
          }

          // Hide the progressBar
          progressBar.setVisibility(View.GONE);
        }
      }
    }.execute();
  }
}