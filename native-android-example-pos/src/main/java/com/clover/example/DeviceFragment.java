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

import com.clover.sdk.util.Platform;
import com.clover.sdk.v3.connector.IPaymentConnector;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.lang.ref.WeakReference;

public class DeviceFragment extends Fragment{

  private OnFragmentInteractionListener mListener;
  private WeakReference<IPaymentConnector> cloverConnectorWeakReference;
  private View view;
  private EditText showMessageText, printTextText, printImageUrlText;
  private Button showMessage, showWelcomeMessage, showThankYou, printText, printImageUrl, printImage, readCardData, closeout, openCashDrawer;

  public static DeviceFragment newInstance(IPaymentConnector cloverConnector) {
    DeviceFragment fragment = new DeviceFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    fragment.setCloverConnector(cloverConnector);
    return fragment;
  }

  public DeviceFragment(){

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_device, container, false);

    showMessageText = (EditText) view.findViewById(R.id.ShowMessageText);
    showMessage = (Button) view.findViewById(R.id.ShowMessageButton);
    printTextText = (EditText) view.findViewById(R.id.PrintTextText);
    printText = (Button) view.findViewById(R.id.PrintTextButton);
    printImageUrlText = (EditText) view.findViewById(R.id.PrintImageURLText);
    printImageUrl = (Button) view.findViewById(R.id.PrintImageURLButton);
    printImage = (Button) view.findViewById(R.id.PrintImageButton);
    readCardData = (Button) view.findViewById(R.id.ReadCardDataButton);
    showWelcomeMessage = (Button) view.findViewById(R.id.showWelcomeMessageButton);
    showThankYou = (Button) view.findViewById(R.id.showThankYouButton);
    closeout = (Button) view.findViewById(R.id.CloseoutButton);
    openCashDrawer = (Button) view.findViewById(R.id.CashDrawerButton);

    View[] disabled = {printTextText, printText, printImageUrlText, printImageUrl, printImage, openCashDrawer};

    for(View view : disabled){
      disableView(view);
    }

    if(Platform.isCloverGoldenOak()){
      enableView(showMessageText);
      enableView(showMessage);
      enableView(showWelcomeMessage);
      enableView(showThankYou);
    }
    else {
      disableView(showMessageText);
      disableView(showMessage);
      disableView(showWelcomeMessage);
      disableView(showThankYou);
    }
    if(!Platform.isCloverStation() && !Platform.isCloverGoldenOak()){
      enableView(readCardData);
      enableView(closeout);
    }
    else{
      disableView(readCardData);
      disableView(closeout);
    }

    return view;
  }

  public void enableView (View view){
    view.setEnabled(true);
    view.setAlpha(1);
  }

  public void disableView (View view){
    view.setEnabled(false);
    view.setAlpha((float)0.4);
  }

  public interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
    public void onFragmentInteraction(Uri uri);
  }

  public void setCloverConnector(IPaymentConnector cloverConnector) {
    cloverConnectorWeakReference = new WeakReference<IPaymentConnector>(cloverConnector);
  }
}
