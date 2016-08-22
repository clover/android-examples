/**
 * Copyright (C) 2016 Clover Network, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thirdparty.android.example;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v3.order.OrderConnector;
import com.clover.sdk.v3.payments.Payment;

import android.accounts.Account;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
  private TextView statusText;

  private Account account;
  private List<Payment> pendingPayments = null;
  private int numberOfPendingPayments = 0;
  private double pendingPaymentsTotalAmount = 0;
  private OrderConnector orderConnector;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_pending_payments_example);

    statusText = (TextView) findViewById(R.id.status);

    account = CloverAccount.getAccount(this);

    Button b = (Button) findViewById(R.id.button);
    b.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        statusText.setText("");
        doExample();
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (orderConnector == null) {
      orderConnector = new OrderConnector(this, account, null);
      orderConnector.connect();
    }
  }

  @Override
  protected void onPause() {
    if (orderConnector != null) {
      orderConnector.disconnect();
      orderConnector = null;
    }
    super.onPause();
  }

  private void doExample() {
    new AsyncTask<Void, Void, Boolean>() {
      @Override
      protected Boolean doInBackground(Void... params) {
        try {
          if (orderConnector == null) {
            orderConnector = new OrderConnector(MainActivity.this, account, null);
            orderConnector.connect();
          }
          pendingPayments = orderConnector.getPendingPayments();
          numberOfPendingPayments = pendingPayments.size();
          pendingPaymentsTotalAmount = 0;
          for (Payment payment:pendingPayments) {
            pendingPaymentsTotalAmount += payment.getAmount();
          }
        } catch (Exception e) {
          System.out.println(e.getMessage());
          return false;
        }
        return true;
      }

      @Override
      protected void onPostExecute(Boolean result) {
        if (result != null && result) {
          NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
          String output;
          output = "SUCCESS: " + numberOfPendingPayments + " payments found. \n";
          if (numberOfPendingPayments > 0) {
            output += "============================================\n";
            for (Payment payment:pendingPayments) {
              output += "Payment ID: " + payment.getId() + " Amount:" + currencyFormatter.format(new Double(payment.getAmount() * .01)) + "\n";
            }
            output += "============================================\n";
            output += "Total Amount for pending payments is " + currencyFormatter.format(pendingPaymentsTotalAmount * .01);
          }
          statusText.setText(output);
        } else {
          statusText.setText("Call to OrderConnector for getPendingPayments failed.");
        }
      }
    }.execute();
  }
}
