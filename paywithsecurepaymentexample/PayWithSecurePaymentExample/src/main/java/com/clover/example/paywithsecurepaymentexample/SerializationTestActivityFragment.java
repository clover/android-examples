package com.clover.example.paywithsecurepaymentexample;

import android.os.Handler;
import android.widget.Button;
import com.clover.sdk.JSONifiable;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.json.JSONException;

/**
 * A placeholder fragment containing a simple view.
 */
public class SerializationTestActivityFragment extends Fragment {

  private Button goBackButton;

  public SerializationTestActivityFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_serialization_test, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    Intent intent = getActivity().getIntent();
    goBackButton = (Button) view.findViewById(R.id.button_go_back);
    goBackButton.setEnabled(true);
    goBackButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final Intent intent = new Intent(getActivity(), com.clover.example.paywithsecurepaymentexample.MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            startActivity(intent);
          }
        }, 1);
      }
    });

    JSONifiable response = intent.getParcelableExtra("response");

    try {
      String rawString = response.getJSONObject().toString(2);
      TextView textView = (TextView) view.findViewById(R.id.raw_output);
      textView.setText(rawString);

      Log.d(this.getClass().getSimpleName(), "Response is " + response.getJSONObject().toString(2));
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

}
