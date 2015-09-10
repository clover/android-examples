package com.example.extensibletenderexample;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.tender.TenderConnector;

public class TestTenderInitActivity extends Activity {
    public static final String TAG = "TestTenderInitActivity";
    private TenderConnector tenderConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_tender_init);

        createTenderType();
    }

    private void createTenderType() {
        tenderConnector = new TenderConnector(this, CloverAccount.getAccount(this), null);
        tenderConnector.connect();

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    tenderConnector.checkAndCreateTender(getString(R.string.tender_name), getPackageName(), true, false);
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
}
