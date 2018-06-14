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

import com.clover.sdk.v3.base.Signature;
import com.clover.sdk.v3.connector.IPaymentConnector;
import com.clover.sdk.v3.remotepay.VerifySignatureRequest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class SignatureFragment extends Fragment {

  private Signature signature;
  Button rejectButton;
  Button acceptButton;
  SignatureView sigCanvas;

  private OnFragmentInteractionListener mListener;
  private VerifySignatureRequest verifySignatureRequest;
  private IPaymentConnector paymentConnector;

  public static SignatureFragment newInstance(VerifySignatureRequest verifySignatureRequest, IPaymentConnector paymentConnector) {
    SignatureFragment fragment = new SignatureFragment();
    fragment.setVerifySignatureRequest(verifySignatureRequest);
    fragment.setPaymentConnector(paymentConnector);
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public SignatureFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    final View view = inflater.inflate(R.layout.fragment_signature, container, false);

    if (view != null) {
      sigCanvas = (SignatureView) view.findViewById(R.id.SignatureView);
      rejectButton = (Button) view.findViewById(R.id.RejectButton);
      acceptButton = (Button) view.findViewById(R.id.AcceptButton);

      sigCanvas.setSignature(verifySignatureRequest.getSignature());

      acceptButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
          fragmentTransaction.hide(SignatureFragment.this);
          fragmentTransaction.commit();
          paymentConnector.acceptSignature(verifySignatureRequest);
        }
      });
      rejectButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
          fragmentTransaction.hide(SignatureFragment.this);
          fragmentTransaction.commit();
          paymentConnector.rejectSignature(verifySignatureRequest);
        }
      });
    }

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();

  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (SignatureFragment.OnFragmentInteractionListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString()
                                   + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  public void setVerifySignatureRequest(VerifySignatureRequest verifySignatureRequest) {
    this.verifySignatureRequest = verifySignatureRequest;
    if (sigCanvas != null) {
      sigCanvas.setSignature(verifySignatureRequest.getSignature());
      sigCanvas.postInvalidate();
    }
  }

  public VerifySignatureRequest getVerifySignatureRequest() {
    return verifySignatureRequest;
  }

  public void setPaymentConnector(IPaymentConnector paymentConnector) {
    this.paymentConnector = paymentConnector;
  }

  public interface OnFragmentInteractionListener {
    public void onFragmentInteraction(Uri uri);
  }


  public void setSignature(Signature signature) {
    this.signature = signature;

    int sigWidth = signature.getWidth();
    int sigHeight = signature.getHeight();

    sigCanvas.setMinimumHeight(sigHeight);
    sigCanvas.setMinimumWidth(sigWidth);

  }
}
