package com.clover.example.paywithsecurepaymentexample;

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
