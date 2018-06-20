/*
 * Copyright (C) 2018 Clover Network, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clover.cfp.examples;

import com.clover.cfp.activity.CFPConstants;
import com.clover.cfp.examples.R;
import com.clover.cfp.examples.objects.ConversationQuestionMessage;
import com.clover.cfp.examples.objects.ConversationResponseMessage;
import com.clover.cfp.examples.objects.PayloadMessage;
import com.clover.cfp.activity.CloverCFPActivity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;

public class BasicExampleConversationalActivity extends CloverCFPActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    int orientation = this.getResources().getConfiguration().orientation;
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      setContentView(R.layout.activity_conversational_example_portrait);
      findViewById(R.id.basic_conversation_port_example).setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
          return true;
        }
      });
    }
    else{
      setContentView(R.layout.activity_conversational_example);
      findViewById(R.id.basic_conversation_land_example).setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
          return true;
        }
      });
    }
    findViewById(R.id.messagePayload).setVisibility(View.INVISIBLE);
    findViewById(R.id.button1).setVisibility(View.INVISIBLE);
    findViewById(R.id.outMessagePayload).setVisibility(View.INVISIBLE);
    findViewById(R.id.register_image).setVisibility(View.INVISIBLE);
    findViewById(R.id.device_image).setVisibility(View.INVISIBLE);
  }

  @Override protected void onResume() {
    super.onResume();
    String payload = getIntent().getStringExtra(CFPConstants.EXTRA_PAYLOAD);
    if (getIntent().getAction().equals(receiver)) {
      onMessage(payload);
    } else {
      findViewById(R.id.send_window).setVisibility(View.INVISIBLE);
      findViewById(R.id.messagePayload).setVisibility(View.INVISIBLE);
      findViewById(R.id.message_payload).setVisibility(View.INVISIBLE);
      findViewById(R.id.button1).setVisibility(View.INVISIBLE);
      findViewById(R.id.outMessagePayload).setVisibility(View.INVISIBLE);
      ((TextView) findViewById(R.id.payload)).setText(payload);
      findViewById(R.id.register_image).setVisibility(View.INVISIBLE);
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
  }

  @Override protected void onMessage(String payload) {
    findViewById(R.id.send_window).setVisibility(View.VISIBLE);
    findViewById(R.id.messagePayload).setVisibility(View.VISIBLE);
    findViewById(R.id.button1).setVisibility(View.VISIBLE);
    findViewById(R.id.outMessagePayload).setVisibility(View.VISIBLE);
    findViewById(R.id.register_image).setVisibility(View.VISIBLE);

    PayloadMessage payloadMessage = new Gson().fromJson(payload,PayloadMessage.class);
    switch (payloadMessage.messageType) {
      case CONVERSATION_QUESTION:
        ConversationQuestionMessage conversationQuestionMessage = new Gson().fromJson(payload, ConversationQuestionMessage.class);
        ((TextView) findViewById(R.id.messagePayload)).setText(conversationQuestionMessage.message);
        break;
      default:
        Toast.makeText(getApplicationContext(), R.string.unknown_payload + payloadMessage.payloadClassName, Toast.LENGTH_LONG);
    }
  }

  public void messageClicked(View view) {
    String answer = ((TextView) findViewById(R.id.outMessagePayload)).getText().toString();
    ConversationResponseMessage conversationResponseMessage = new ConversationResponseMessage(answer);
    try {
      sendMessage(conversationResponseMessage.toJsonString());
    } catch (final Exception e) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(getApplicationContext(), "There was a problem trying to send the message. Error = " + e.getMessage(),
              Toast.LENGTH_LONG).show();
        }
      });
      e.printStackTrace();
    }
    TextView outMessagePayload =  (TextView) findViewById(R.id.outMessagePayload);
    String message = outMessagePayload.getText().toString();
    outMessagePayload.setText("");
    TextView message_payload = (TextView) findViewById(R.id.message_payload);
    message_payload.setText(message);
    message_payload.setVisibility(View.VISIBLE);
    Button button1  = (Button) findViewById(R.id.button1);
    button1.setEnabled(false);
    button1.setAlpha(.5f);
    findViewById(R.id.device_image).setVisibility(View.VISIBLE);
  }

  public void finishClicked(View view) {
    String payload = ((EditText)findViewById(R.id.resultPayload)).getText().toString();
    setResultAndFinish(RESULT_OK, payload);
  }
}