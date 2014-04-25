package com.clover.example.inventory;

import android.accounts.Account;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v3.inventory.InventoryContract;

public class InventoryExampleActivity extends Activity {

  private static final int LOADER_ID_INVENTORY = 0;

  private Account mCloverAccount;
  private CursorAdapter mInventoryCursorAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Debug.waitForDebugger();

    mCloverAccount = CloverAccount.getAccount(this);

    initUi();

    initInventoryCursorLoader();
  }

  private void initUi() {
    setContentView(R.layout.main);

    mInventoryCursorAdapter = new SimpleCursorAdapter(
        this,
        android.R.layout.simple_list_item_2,
        null,
        new String[] { InventoryContract.Item.NAME, InventoryContract.Item.CODE },
        new int[] {android.R.id.text1, android.R.id.text2 },
        0
    );

    ListView listView = (ListView)findViewById(R.id.listview);
    listView.setAdapter(mInventoryCursorAdapter);
  }

  private void initInventoryCursorLoader() {
    LoaderManager.LoaderCallbacks<Cursor> callbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
      @Override
      public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = InventoryContract.Item.contentUriWithAccount(mCloverAccount);
        return new CursorLoader(InventoryExampleActivity.this, uri, null, null, null, null);
      }

      @Override
      public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mInventoryCursorAdapter.swapCursor(data);
      }

      @Override
      public void onLoaderReset(Loader<Cursor> loader) {
        mInventoryCursorAdapter.swapCursor(null);
      }
    };

    getLoaderManager().initLoader(LOADER_ID_INVENTORY, null, callbacks);
  }

}
