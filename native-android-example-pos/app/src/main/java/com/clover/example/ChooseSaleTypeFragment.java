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

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class ChooseSaleTypeFragment extends DialogFragment {

  private View view;
  private Button sale, auth;
  private TextView close;
  private List<ChooseSaleTypeListener> listeners = new ArrayList<>(5);

  public static ChooseSaleTypeFragment newInstance(){
    ChooseSaleTypeFragment fragment = new ChooseSaleTypeFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(STYLE_NO_TITLE, R.style.CustomDialog);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_choose_sale_type, container, false);

    close = (TextView) view.findViewById(R.id.PopupClose);
    close.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
      }
    });

    sale = (Button) view.findViewById(R.id.ChooseSaleButton);
    sale.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onSaleTypeChoice("Sale");
      }
    });
    auth = (Button) view.findViewById(R.id.ChooseAuthButton);
    auth.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onSaleTypeChoice("Auth");
      }
    });

    return view;
  }

  public void onSaleTypeChoice(String choice){
    dismiss();
    for (ChooseSaleTypeListener listener : listeners){
      listener.onSaleTypeChoice(choice);
    }
  }

  public interface ChooseSaleTypeListener {
    public abstract void onSaleTypeChoice(String choice);
  }

  public void addListener(ChooseSaleTypeListener listener){
    listeners.add(listener);
  }

  private ChooseSaleTypeListener mListener;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      this.mListener = (ChooseSaleTypeListener) activity;
    }
    catch (final ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
    }
  }

}
