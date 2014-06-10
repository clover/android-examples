package com.clover.example.inventory;

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.app.ListActivity;
import android.util.Log;
import android.os.AsyncTask;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ForbiddenException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.InventoryContract;
import com.clover.sdk.v1.merchant.Merchant;
import com.clover.sdk.v1.merchant.MerchantConnector;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v1.ResultStatus;
import com.clover.sdk.v3.inventory.ModifierGroup;
import com.clover.sdk.v3.inventory.PriceType;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.TaxRate;
import com.clover.sdk.v3.inventory.Tag;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.List;

/**
 * In this example app, the Clover InventoryContract is used with a standard Android Loader to
 * query the Clover inventory, from which an item's detailed information is dynamically fetched
 * from the Inventory Service. You should be familiar with the Android ListView class, as well as
 * the AsyncTask class.
 * http://developer.android.com/guide/topics/ui/layout/listview.html
 * http://developer.android.com/reference/android/os/AsyncTask.html
 */

public class InventoryExampleActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Account mCloverAccount;
    private SimpleCursorAdapter mInventoryCursorAdapter;
    private final Context context = this;
    private static final String TAG = InventoryExampleActivity.class.getSimpleName();
    // Used for currency formatting
    private MerchantConnector mMerchantConnector;
    private static final NumberFormat sCurrencyFormat = DecimalFormat.getCurrencyInstance(Locale.US);
    private static final int ITEM_LOADER_ID = 0;
    // Used for connection to Inventory Service
    private InventoryConnector inventoryConnector;
    private boolean serviceIsBound = false;
    // Used for dialog box
    private String UUID;
    private AlertDialog.Builder builder;
    private String message;
    private AlertDialog alertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // Get a Clover Account
        super.onCreate(savedInstanceState);
        mCloverAccount = CloverAccount.getAccount(context);

        // Connect to Clover Inventory Service
        connectToServiceConnector();

        // Create an empty adapter we will use to display the loaded data.
        mInventoryCursorAdapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                null,
                new String[]{InventoryContract.Item.NAME, InventoryContract.Item.PRICE},
                new int[]{android.R.id.text1, android.R.id.text2},
                0
        );
        setListAdapter(mInventoryCursorAdapter);

        getCurrency();
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "Pausing...");
        mMerchantConnector.disconnect();
        inventoryConnector.disconnect();
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "...Resumed");
        super.onResume();

        // Retrieve the Clover account
        if (mCloverAccount == null) {
            mCloverAccount = CloverAccount.getAccount(context);

            if (mCloverAccount == null) {
                Toast.makeText(this, getString(R.string.no_account), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }
        mMerchantConnector.connect();
        connectToServiceConnector();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMerchantConnector.disconnect();
        inventoryConnector.disconnect();
    }

    // Get the merchant's currency using MerchantConnector to get a Merchant.
    private void getCurrency() {
        if (mCloverAccount != null) {
            mMerchantConnector = new MerchantConnector(context, mCloverAccount, null);
            mMerchantConnector.getMerchant(new ServiceConnector.Callback<Merchant>() {
                @Override
                public void onServiceSuccess(Merchant result, ResultStatus status) {
                    // Set the Currency
                    sCurrencyFormat.setCurrency(result.getCurrency());

                    // Formatting the merchant's currency
                    mInventoryCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                        @Override
                        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                            if (view.getId() == android.R.id.text2) {
                                TextView priceTextView = (TextView) view;
                                String price = "";
                                // Retrieves the Item's PRICE_TYPE and PRICE
                                PriceType priceType = PriceType.values()[cursor.getInt(cursor.getColumnIndex(InventoryContract.Item.PRICE_TYPE))];
                                String priceString = cursor.getString(cursor.getColumnIndex(InventoryContract.Item.PRICE));
                                long value = Long.valueOf(priceString);
                                if (priceType == PriceType.FIXED) {
                                    price = sCurrencyFormat.format(value / 100.0);
                                } else if (priceType == PriceType.VARIABLE) {
                                    price = "Variable";
                                } else if (priceType == PriceType.PER_UNIT) {
                                    price = sCurrencyFormat.format(value / 100.0) + "/" + cursor.getString(cursor.getColumnIndex(InventoryContract.Item.UNIT_NAME));
                                }
                                priceTextView.setText(price);
                                return true;
                            }
                            return false;
                        }
                    });

                    // Now that we have the merchant's currency, a CursorLoader is used to query the ContentProvider.
                    getLoaderManager().initLoader(ITEM_LOADER_ID, null, InventoryExampleActivity.this);
                }

                @Override
                public void onServiceFailure(ResultStatus status) {
                }

                @Override
                public void onServiceConnectionFailure() {
                }
            });
        }
    }

    // Connects to the Clover Inventory Service
    private void connectToServiceConnector() {
        inventoryConnector = new InventoryConnector(context, mCloverAccount, new ServiceConnector.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(ServiceConnector connector) {
                serviceIsBound = true;
            }

            @Override
            public void onServiceDisconnected(ServiceConnector connector) {
                serviceIsBound = false;
            }
        });
        inventoryConnector.connect();
    }

    // This is called when a new Loader needs to be created
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ITEM_LOADER_ID:
                // The uri points to Item, which is the items in the inventory
                Uri uri = InventoryContract.Item.contentUriWithAccount(mCloverAccount);
                String sortOrder = InventoryContract.Item.NAME;
                return new CursorLoader(InventoryExampleActivity.this, uri, null, null, null, sortOrder);
            default:
                // An invalid id was passed in
        }
        throw new IllegalArgumentException("Unknown Loader ID");
    }

    // Defines the callback that CursorLoader calls when it's finished its query
    @Override
    public final void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swaps the new cursor in.
        mInventoryCursorAdapter.swapCursor(data);
    }

    // Invoked when the CursorLoader is being reset
    // i.e. if the data changes in the provider and the Cursor becomes stale.
    @Override
    public final void onLoaderReset(Loader<Cursor> loader) {
        // Clears out the adapter's reference to the Cursor to prevent memory leaks.
        mInventoryCursorAdapter.swapCursor(null);
    }

    // Opens a dialog box containing detailed info about the inventory item that was clicked on.
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        /* The cursor can give you information about an item, such as its price.
         * Using its UUID, you can further query the Inventory Service for more detailed information.
         * Info available from Cursor (in this order):
         * price_type, price, count, _id, taxable, default_tax_rates, name, code, alternate_name, uuid, sku, unit_name
         * This can be shown by dumping the cursor using the Android Database Utilities
         * builder.setMessage(DatabaseUtils.dumpCursorToString(myCursor));
         */

        Cursor myCursor = (Cursor) l.getItemAtPosition(position);
        UUID = myCursor.getString(9);

        // Pulling the previously formatted price from the ListView.
        TextView text = (TextView) v.findViewById(android.R.id.text2);
        message = "Price: " + text.getText().toString() + "\nUUID: " + UUID;

        // Fetching additional information from the Clover Inventory Service using an AsyncTask
        if (serviceIsBound && inventoryConnector != null) {
            new AccessInventoryService().execute();
        }

        // This builds the alert dialog box that is displayed when a user clicks on an item.
        // The remaining info is added at the completion of the AsyncTask completes
        builder = new AlertDialog.Builder(context);
        builder.setTitle(myCursor.getString(6) + " Details");
    }

    // AsyncTask to fetch inventory item information from the Clover Inventory Service
    private class AccessInventoryService extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            String information = "";
            try {
                // Fetches tax rates for items
                // getTaxRatesForItem will only return the tax rate if it is NOT the default rate.
                List<TaxRate> taxRatesForItem = inventoryConnector.getTaxRatesForItem(UUID);

                if (!taxRatesForItem.isEmpty()) {
                    information += "\nTax Rate: ";
                    for (TaxRate taxRate : taxRatesForItem) {
                        information += taxRate.getName();
                    }
                } else {
                    information += "\nTax Rate: Default";
                }

                // Fetches item groups
                information += "\nGroups: ";
                List<ModifierGroup> modifierGroups = inventoryConnector.getModifierGroupsForItem(UUID);
                if (!modifierGroups.isEmpty()) {
                    for (ModifierGroup group : modifierGroups) {
                        information += group.getName() + " ";
                    }
                } else {
                    information += "None";
                }

                // Fetches item tags
                information += "\nTags: ";
                List<Tag> tags = inventoryConnector.getTagsForItem(UUID);
                if (!tags.isEmpty()) {
                    for (Tag t : tags) {
                        information += t.getName() + " ";
                    }
                } else {
                    information += "None";
                }
            } catch (ForbiddenException e) {
                Log.e(TAG, "Auth Exception", e);
            } catch (ClientException e) {
                Log.e(TAG, "Client Exception", e);
            } catch (ServiceException e) {
                Log.e(TAG, "Service Exception", e);
            } catch (BindingException e) {
                Log.e(TAG, "Error calling inventory service", e);
            } catch (RemoteException e) {
                Log.e(TAG, "Error calling inventory service", e);
            }
            return information;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Finishes building the dialog box
            message += result;
            builder.setMessage(message);
            alertDialog = builder.create();
            alertDialog.show();
        }
    }
}