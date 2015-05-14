package com.clover.example.terminalplanpaymentexample;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.clover.sdk.v3.payments.Payment;

import java.util.List;


public class MainActivity extends Activity {

    private Account account;
    private OrderConnector orderConnector;
    private InventoryConnector inventoryConnector;
    private Order order;
    private Button payButton;
    private static final int SECURE_PAY_REQUEST_CODE = 1;
    //This bit value is used to store selected card entry methods, which can be combined with bitwise 'or' and passed to EXTRA_CARD_ENTRY_METHODS
    private int cardEntryMethodsAllowed = Intents.CARD_ENTRY_METHOD_MAG_STRIPE | Intents.CARD_ENTRY_METHOD_ICC_CONTACT | Intents.CARD_ENTRY_METHOD_NFC_CONTACTLESS | Intents.CARD_ENTRY_METHOD_MANUAL;


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

        CheckBox magStripeCheckBox = (CheckBox) findViewById(R.id.mag_stripe_check_box);
        CheckBox chipCardCheckBox = (CheckBox) findViewById(R.id.chip_card_check_box);
        CheckBox nfcCheckBox = (CheckBox) findViewById(R.id.nfc_check_box);
        CheckBox manualEntryCheckBox = (CheckBox) findViewById(R.id.manual_entry_check_box);

        //These methods toggle the bitvalue storing which card entry methods are allowed
        magStripeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cardEntryMethodsAllowed = cardEntryMethodsAllowed ^ Intents.CARD_ENTRY_METHOD_MAG_STRIPE;
            }
        });
        chipCardCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cardEntryMethodsAllowed = cardEntryMethodsAllowed ^ Intents.CARD_ENTRY_METHOD_ICC_CONTACT;
            }
        });
        nfcCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cardEntryMethodsAllowed = cardEntryMethodsAllowed ^ Intents.CARD_ENTRY_METHOD_NFC_CONTACTLESS;
            }
        });
        manualEntryCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cardEntryMethodsAllowed = cardEntryMethodsAllowed ^ Intents.CARD_ENTRY_METHOD_MANUAL;
            }
        });



        // create order
        new OrderAsyncTask().execute();

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSecurePaymentIntent();
            }
        });

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
                // Update local representation of the order
                mOrder = orderConnector.getOrder(mOrder.getId());

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
                }
            }
        }
    }

    // Start intent to launch Clover's secure payment activity
    //NOTE: ACTION_SECURE_PAY requires that your app has "clover.permission.ACTION_PAY" in it's AndroidManifest.xml file
    private void startSecurePaymentIntent() {
        Intent intent = new Intent(Intents.ACTION_SECURE_PAY);

        //EXTRA_AMOUNT is required for secure payment
        intent.putExtra(Intents.EXTRA_AMOUNT, order.getTotal());

        //Pass the generated order's id
        intent.putExtra(Intents.EXTRA_ORDER_ID, order.getId());
        //If no order id were passed to EXTRA_ORDER_ID a new empty order would be generated for the payment

        
        //Allow only selected card entry methods
        intent.putExtra(Intents.EXTRA_CARD_ENTRY_METHODS, cardEntryMethodsAllowed);

        startActivityForResult(intent, SECURE_PAY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SECURE_PAY_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                //Once the secure payment activity completes the result and its extras can be worked with
                Payment payment = data.getParcelableExtra(Intents.EXTRA_PAYMENT);
                Toast.makeText(getApplicationContext(), getString(R.string.payment_successful, payment.getOrder().getId()), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.payment_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
