/**
 * Copyright (C) 2015 Clover Network, Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.test.all_examples.auths;

import android.accounts.Account;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.util.CloverAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.test.all_examples.CustomHttpClient;
import com.test.all_examples.R;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class APIServiceActivity extends Activity {

  private static final String TAG = APIServiceActivity.class.getSimpleName();

  private Account mAccount;
  private Button mButton;
  private TextView mLogText;

  private SimpleDateFormat dateFormat = new SimpleDateFormat("H:mm:ss");
  private View.OnClickListener mRequestOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      queryWebService();
      v.setEnabled(false);
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.web_service);

    mLogText = (TextView) findViewById(R.id.logText);
    mButton = (Button) findViewById(R.id.button);
    mButton.setOnClickListener(mRequestOnClickListener);

    mAccount = CloverAccount.getAccount(this);
    if (mAccount == null) {
      log("Account not found.");
      return;
    }
    log("Retrieved Clover Account: " + mAccount.name);
  }

  private void log(String text) {
    Log.i(TAG, text);

    StringBuilder sb = new StringBuilder(mLogText.getText().toString());
    if (sb.length() > 0) {
      sb.append('\n');
    }
    sb.append(dateFormat.format(new Date())).append(": ").append(text);
    mLogText.setText(sb.toString());
  }

  private void queryWebService() {
    new AsyncTask<Void, String, Void>() {

      @Override
      protected void onProgressUpdate(String... values) {
        String logString = values[0];
        log(logString);
      }

      @Override
      protected Void doInBackground(Void... params) {
        try {
          publishProgress("Requesting auth token");
          CloverAuth.AuthResult authResult = CloverAuth.authenticate(APIServiceActivity.this, mAccount);
          publishProgress("Successfully authenticated as " + mAccount + ".  authToken=" + authResult.authToken + ", authData=" + authResult.authData);

          if (authResult.authToken != null && authResult.baseUrl != null) {
            CustomHttpClient httpClient = CustomHttpClient.getHttpClient();
            String getNameUri = "/v2/merchant/name";
            String url = authResult.baseUrl + getNameUri + "?access_token=" + authResult.authToken;
            publishProgress("requesting merchant id using: " + url);
            String result = httpClient.get(url);
            JSONTokener jsonTokener = new JSONTokener(result);
            JSONObject root = (JSONObject) jsonTokener.nextValue();
            String merchantId = root.getString("merchantId");
            publishProgress("received merchant id: " + merchantId);

            // now do another get using the merchant id
            String inventoryUri = "/v2/merchant/" + merchantId + "/inventory/items";
            url = authResult.baseUrl + inventoryUri + "?access_token=" + authResult.authToken;

            publishProgress("requesting inventory items using: " + url);
            result = httpClient.get(url);
            publishProgress("received inventory items response: " + toPrettyFormat(result));
          }
        } catch (Exception e) {
          publishProgress("Error retrieving merchant info from server" + e);
        }
        return null;
      }

      public String toPrettyFormat(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
      }

      @Override
      protected void onPostExecute(Void aVoid) {
        mButton.setEnabled(true);
      }
    }.execute();
  }

}
