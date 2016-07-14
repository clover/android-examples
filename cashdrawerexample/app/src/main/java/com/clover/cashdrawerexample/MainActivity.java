package com.clover.cashdrawerexample;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.clover.sdk.cashdrawer.CashDrawer;
import com.clover.sdk.cashdrawer.CashDrawers;

import java.util.Set;

public class MainActivity extends Activity {
  private ListView cashDrawerList;
  private CashDrawerAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    cashDrawerList = (ListView) findViewById(R.id.list_cash_drawers);
    adapter = new CashDrawerAdapter(this);
    cashDrawerList.setAdapter(adapter);
    cashDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CashDrawer cd = adapter.getItem(position);
        cd.pop();
      }
    });
    cashDrawerList.setEmptyView(findViewById(R.id.text_empty));
  }

  @Override
  protected void onResume() {
    super.onResume();

    new AsyncTask<Void,Void,Set<CashDrawer>>() {
      @Override
      protected Set<CashDrawer> doInBackground(Void... params) {
        return new CashDrawers(MainActivity.this).list();
      }

      @Override
      protected void onPostExecute(Set<CashDrawer> cashDrawers) {
        adapter.addData(cashDrawers);
      }
    }.execute();
  }
}
