package com.example.extensibletenderexample;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.tender.TenderConnector;

public class TestTenderInitActivity extends AppCompatActivity {
    public static final String TAG = "TestTenderInitActivity";
    private TenderConnector tenderConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createTenderType();
    }

    private void createTenderType() {
        tenderConnector = new TenderConnector(this, CloverAccount.getAccount(this), null);
        tenderConnector.connect();

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    tenderConnector.checkAndCreateTender(getString(R.string.tender1_text), getPackageName(), true, false);
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e.getCause());

                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(TestTenderInitActivity.this, "Test tender init SUCCESS!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(TestTenderInitActivity.this, "Test tender init FAILURE!", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    @Override
    protected void onDestroy() {
        if (tenderConnector != null) {
            tenderConnector.disconnect();
            tenderConnector = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
