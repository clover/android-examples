package com.example.extensibletendernfc;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;

import com.clover.sdk.v1.Intents;

/**
 * Created by mmaietta on 8/16/15.
 * Modified by ketpat on 12/20/16
 */
public class CustomerFacingTenderActivity extends TenderActivity {
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Necessary for Customer Facing user experiences
        setSystemUiVisibility();

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            showMessage(R.string.error, R.string.no_nfc);
            finish();
            return;
        }
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        /**
         * @see Intents.ACTION_CUSTOMER_TENDER
         */
    }

    public void setSystemUiVisibility() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {
            if (!mAdapter.isEnabled()) {
                showWirelessSettingsDialog();
            }
            mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }
}
