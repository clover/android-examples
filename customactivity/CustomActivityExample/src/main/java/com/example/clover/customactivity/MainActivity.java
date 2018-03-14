package com.example.clover.customactivity;

import com.clover.cfp.activity.CFPConstants;
import com.clover.cfp.activity.CloverCFPActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends CloverCFPActivity {
  private TextView payloadReceived;
  private Button payloadSend;
  private EditText payloadContent;
  private ListView messageList;

  private List<PayloadMessage> messages;


  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);
    messages = new ArrayList<PayloadMessage>();
    payloadReceived = (TextView) findViewById(R.id.PayloadReceived);
    payloadContent = (EditText) findViewById(R.id.SendPayloadContent);
    messageList = (ListView) findViewById(R.id.MessagesListView);
    PayloadMessageAdapter payloadMessageAdapter = new PayloadMessageAdapter(this, R.id.MessagesListView, messages);
    messageList.setAdapter(payloadMessageAdapter);
    payloadSend = (Button) findViewById(R.id.SendPayloadButton);
    payloadSend.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        try {
          String payload =payloadContent.getText().toString();
          payloadContent.setText("");
          sendMessage(payload);
          messages.add(new PayloadMessage(payload, false));
          updateMessages();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    String payload = getIntent().getStringExtra(CFPConstants.EXTRA_PAYLOAD);
    payloadReceived.setText(payload);
  }

  @Override protected void onResume() {
    super.onResume();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
  }

  @Override protected void onMessage(String payload) {
    messages.add(new PayloadMessage(payload, true));
    updateMessages();
  }


  public void updateMessages(){
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        PayloadMessageAdapter payloadMessageAdapter = new PayloadMessageAdapter(MainActivity.this, R.id.MessagesListView, messages);
        messageList.setAdapter(payloadMessageAdapter);
      }
    });
  }

  public void finishClicked(View view) {
    String payload = ((EditText)findViewById(R.id.resultPayload)).getText().toString();
    setResultAndFinish(RESULT_OK, payload);
  }

}
