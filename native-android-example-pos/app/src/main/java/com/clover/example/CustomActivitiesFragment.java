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

package com.clover.example;

import com.clover.sdk.v3.connector.IPaymentConnector;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.lang.ref.WeakReference;

public class CustomActivitiesFragment extends Fragment {

  private OnFragmentInteractionListener mListener;
  private WeakReference<IPaymentConnector> cloverConnectorWeakReference;
  private Spinner customActivityId;
  private View view;

  public static CustomActivitiesFragment newInstance(IPaymentConnector cloverConnector) {
    CustomActivitiesFragment fragment = new CustomActivitiesFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    fragment.setCloverConnector(cloverConnector);
    return fragment;
  }

  public CustomActivitiesFragment(){

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_custom_activities, container, false);


    customActivityId = ((Spinner) view.findViewById(R.id.activity_id));
    // Get a reference to the AutoCompleteTextView in the layout and assign the auto-complete choices.
    String[] samples = getResources().getStringArray(R.array.customIds);
    ArrayAdapter<String> customAdapter = new ArrayAdapter<>(this.getActivity().getBaseContext(), android.R.layout.simple_spinner_item, samples);
    customAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    customActivityId.setAdapter(customAdapter);
    return view;
  }


  public interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
    public void onFragmentInteraction(Uri uri);
  }

  public void setCloverConnector(IPaymentConnector cloverConnector) {
    cloverConnectorWeakReference = new WeakReference<IPaymentConnector>(cloverConnector);
  }
}
