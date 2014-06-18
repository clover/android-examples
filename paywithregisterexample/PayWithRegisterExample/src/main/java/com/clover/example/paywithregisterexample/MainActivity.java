package com.clover.example.paywithregisterexample;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.PriceType;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;

import java.util.List;


public class MainActivity extends Activity {

    private Account account;
    private OrderConnector orderConnector;
    private InventoryConnector inventoryConnector;
    private Order order;
    private Button payButton;
    private Button payWithAutoLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Retrieve the Clover account
        if (account == null) {
            account = CloverAccount.getAccount(this);

            // If an account can't be acquired, exit the app
            if (account == null) {
                Toast.makeText(this, getString(R.string.no_account), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        // Create and Connect
        connect();

        payButton = (Button) findViewById(R.id.pay_button);
        payButton.setEnabled(false);

        payWithAutoLogout = (Button) findViewById(R.id.pay_auto_logout);
        payWithAutoLogout.setEnabled(false);

        // create order
        new OrderAsyncTask().execute();

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegisterIntent(false);
            }
        });

        payWithAutoLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegisterIntent(true);
            }
        });
    }

    // Start intent to launch Clover's register pay activity.
    // If true is passed in, the app will auto logout after the transaction is complete.
    private void startRegisterIntent(boolean auto_logout) {
        Intent intent = new Intent(Intents.ACTION_CLOVER_PAY);
        intent.putExtra(Intents.EXTRA_CLOVER_ORDER_ID, order.getId());
        intent.putExtra(Intents.EXTRA_OBEY_AUTO_LOGOUT, auto_logout);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        disconnect();
        super.onPause();
    }

    // Establishes a connection with the connectors
    private void connect() {
        disconnect();
        if (account != null) {
            orderConnector = new OrderConnector(this, account, null);
            orderConnector.connect();
            inventoryConnector = new InventoryConnector(this, account, null);
            inventoryConnector.connect();
        }
    }

    // Disconnects from the connectors
    private void disconnect() {
        if (orderConnector != null) {
            orderConnector.disconnect();
            orderConnector = null;
        }
        if (inventoryConnector != null) {
            inventoryConnector.disconnect();
            inventoryConnector = null;
        }
    }

    // Creates a new order w/ the first inventory item
    private class OrderAsyncTask extends AsyncTask<Void, Void, Order> {

        @Override
        protected final Order doInBackground(Void... params) {
            Order mOrder;
            List<Item> merchantItems;
            Item mItem;
            try {
                // Create a new order
                mOrder = orderConnector.createOrder(new Order());
                // Grab the items from the merchant's inventory
                merchantItems = inventoryConnector.getItems();
                // If there are no item's in the merchant's inventory, then call a toast and return null
                if (merchantItems.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.empty_inventory), Toast.LENGTH_SHORT).show();
                    finish();
                    return null;
                }
                // Taking the first item from the inventory
                mItem = merchantItems.get(0);
                // Add this item to the order, must add using its PriceType
                if (mItem.getPriceType() == PriceType.FIXED) {
                    orderConnector.addFixedPriceLineItem(mOrder.getId(), mItem.getId(), null, null);
                } else if (mItem.getPriceType() == PriceType.PER_UNIT) {
                    orderConnector.addPerUnitLineItem(mOrder.getId(), mItem.getId(), 1, null, null);
                } else { // The item must be of a VARIABLE PriceType
                    orderConnector.addVariablePriceLineItem(mOrder.getId(), mItem.getId(), 5, null, null);
                }
                return mOrder;
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            } catch (BindingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected final void onPostExecute(Order order) {
            // Enables the pay buttons if the order is valid
            if (!isFinishing()) {
                MainActivity.this.order = order;
                if (order != null) {
                    payButton.setEnabled(true);
                    payWithAutoLogout.setEnabled(true);
                }
            }
        }
    }

}
