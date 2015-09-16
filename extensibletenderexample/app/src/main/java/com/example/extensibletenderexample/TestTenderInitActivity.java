package com.example.extensibletenderexample;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.tender.Tender;
import com.clover.sdk.v1.tender.TenderConnector;

import java.util.List;

public class TestTenderInitActivity extends Activity {
    public static final String TAG = "TestTenderInitActivity";
    private TenderConnector tenderConnector;
    private Button initializeButton;
    private TextView customTenderText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_tender_init);
        customTenderText = (TextView) findViewById(R.id.customTenderText);

        initializeButton = (Button) findViewById(R.id.initializeButton);
        initializeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTenderType();
            }
        });

        tenderConnector = new TenderConnector(this, CloverAccount.getAccount(this), null);
        tenderConnector.connect();

        getTenderTypes();
    }

    public void getTenderTypesSuccess(List<Tender> tenders) {
        boolean tenderExists = false;
        for (Tender tender : tenders) {
            if (getString(R.string.tender_name).equals(tender.getLabel())) {
                tenderExists = true;
                break;
            }
        }

        int textId;
        if (tenderExists)
            textId = R.string.custom_tender_initialized;
        else {
            textId = R.string.custom_tender_not_found;
            initializeButton.setVisibility(View.VISIBLE);
        }
        customTenderText.setText(textId);
    }

    private void getTenderTypes() {
        new AsyncTask<Void, Void, List<Tender>>() {
            @Override
            protected List<Tender> doInBackground(Void... params) {
                try {
                    return tenderConnector.getTenders();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e.getCause());
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<Tender> tenders) {
                if (tenders != null)
                    getTenderTypesSuccess(tenders);
            }
        }.execute();
    }

    private void createTenderTypeFinished(Boolean success) {
        int textId;
        if (success) {
            textId = R.string.custom_tender_initialized;
            initializeButton.setVisibility(View.GONE);
        } else {
            textId = R.string.custom_tender_failure;
            initializeButton.setVisibility(View.VISIBLE);
        }
        customTenderText.setText(textId);
        Toast.makeText(TestTenderInitActivity.this, textId, Toast.LENGTH_SHORT).show();
    }

    private void createTenderType() {
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
                createTenderTypeFinished(success);
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
