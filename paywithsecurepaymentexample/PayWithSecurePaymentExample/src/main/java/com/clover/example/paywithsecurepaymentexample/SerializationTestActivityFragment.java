package com.clover.example.paywithsecurepaymentexample;

import com.clover.sdk.v3.remotepay.SaleResponse;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    Intent intent = getActivity().getIntent();
    SaleResponse saleResponse = intent.getParcelableExtra("saleResponse");

    try {
      Log.d(this.getClass().getSimpleName(), "Saleresponse is " + saleResponse.getJSONObject().toString(2));
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return inflater.inflate(R.layout.fragment_serialization_test, container, false);
  }
}
