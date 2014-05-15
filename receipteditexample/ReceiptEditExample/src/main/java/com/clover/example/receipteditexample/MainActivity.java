
package com.clover.example.receipteditexample;

import android.accounts.Account;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IInterface;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.printer.ReceiptRegistrationConnector;
import com.clover.sdk.v1.printer.job.BillPrintJob;
import com.clover.sdk.v1.printer.job.PrintJob;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;
import com.clover.sdk.v3.order.OrderContract;

public class MainActivity extends Activity {
  private Account account;
  private ReceiptRegistrationConnector connector;
  private OrderConnector orderConnector;

  private Button buttonPrint;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    buttonPrint = (Button) findViewById(R.id.button_print);
    buttonPrint.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Set the add on text and print the last order's bill receipt
        ReceiptRegistrationProvider.updateReceiptAddOnText(((EditText) findViewById(R.id.additional_text)).getText().toString());
        new MyAsyncTask().execute();
      }
    });

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

    // Create and Connect
    connect();
  }

  private void registerReceiptRegistration() {
    new AsyncTask<Void, Void, Void>() {

      @Override
      protected Void doInBackground(Void... params) {
        connector.register(Uri.parse(ReceiptRegistrationProvider.CONTENT_URI_TEXT.toString()), new ReceiptRegistrationConnector.ReceiptRegistrationCallback<Void>());
        return null;
      }
    }.execute();
  }

  private void unregisterReceiptRegistration() {
    new AsyncTask<Void, Void, Void>() {

      @Override
      protected Void doInBackground(Void... params) {
        connector.unregister(Uri.parse(ReceiptRegistrationProvider.CONTENT_URI_TEXT.toString()), new ReceiptRegistrationConnector.ReceiptRegistrationCallback<Void>());
        return null;
      }

      @Override
      protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        connector.disconnect();
        connector = null;
      }
    }.execute();
  }

  @Override
  protected void onPause() {
    disconnect();
    super.onPause();
  }

  private void connect() {
    disconnect();
    if (account != null) {
      connector = new ReceiptRegistrationConnector(this, account, new ServiceConnector.OnServiceConnectedListener() {
        @Override
        public void onServiceConnected(ServiceConnector<? extends IInterface> serviceConnector) {
          registerReceiptRegistration();
        }

        @Override
        public void onServiceDisconnected(ServiceConnector<? extends IInterface> serviceConnector) {
        }
      });
      connector.connect();
      orderConnector = new OrderConnector(this, account, null);
      orderConnector.connect();
    }
  }

  private void disconnect() {
    if (connector != null) {
      unregisterReceiptRegistration();
    }

    if (orderConnector != null) {
      orderConnector.disconnect();
      orderConnector = null;
    }
  }

  private class MyAsyncTask extends AsyncTask<Void, Void, Order> {

    @Override
    protected final Order doInBackground(Void... params) {
      String orderId = null;
      Cursor cursor = null;
      try {
        // Query the last order
        cursor = MainActivity.this.getContentResolver().query(OrderContract.Summaries.contentUriWithAccount(account), new String[]{OrderContract.Summaries.ID}, null, null, OrderContract.Summaries.LAST_MODIFIED + " DESC LIMIT 1");
        if (cursor != null && cursor.moveToFirst()) {
          orderId = cursor.getString(cursor.getColumnIndex(OrderContract.Summaries.ID));
        }

        if (orderId == null) {
          return orderConnector.createOrder(new Order());
        } else {
          return orderConnector.getOrder(orderId);
        }
      } catch (RemoteException e) {
        e.printStackTrace();
      } catch (ClientException e) {
        e.printStackTrace();
      } catch (ServiceException e) {
        e.printStackTrace();
      } catch (BindingException e) {
        e.printStackTrace();
      } finally {
        if (cursor != null) {
          cursor.close();
        }
      }
      return null;
    }

    @Override
    protected final void onPostExecute(Order order) {
      PrintJob pj = new BillPrintJob.Builder().orderId(order.getId()).build();
      pj.print(MainActivity.this, account);
    }
  }
}