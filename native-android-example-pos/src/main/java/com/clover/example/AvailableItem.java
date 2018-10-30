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

import com.clover.example.model.POSItem;
import com.clover.example.utils.CurrencyUtils;
import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class AvailableItem extends Fragment {
  POSItem item;
  List<AvailableItemListener> listeners = new ArrayList<AvailableItemListener>(5);

  private OnFragmentInteractionListener mListener;
  private TextView badgeView;

  public static AvailableItem newInstance() {
    AvailableItem fragment = new AvailableItem();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public AvailableItem() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View v = inflater.inflate(R.layout.fragment_available_item, container, false);
    if (v != null) {
      TextView tv = (TextView) v.findViewById(R.id.ItemNameLabel);
      if (tv != null) {
        tv.setText(item.getName());
      }
      tv.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          addItemToOrder();
        }
      });
      tv = (TextView) v.findViewById(R.id.ItemNamePrice);
      tv.setText(CurrencyUtils.format(item.getPrice(), Locale.getDefault()));
      tv.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          addItemToOrder();
        }
      });

      badgeView = (TextView) v.findViewById(R.id.ItemBadge);
    }
    return v;
  }

  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
    }
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (OnFragmentInteractionListener) activity;
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

  public void addListener(AvailableItemListener listener) {
    listeners.add(listener);
  }

  public void setQuantity(int quantity) {

    badgeView.setText("" + quantity);
    if (quantity <= 0) {
      badgeView.setVisibility(View.INVISIBLE);
    } else {
      badgeView.setVisibility(View.VISIBLE);
    }
  }

  public interface OnFragmentInteractionListener {
    public void onFragmentInteraction(Uri uri);
  }

  public void addItemToOrder() {
    for (AvailableItemListener listener : listeners) {
      listener.onItemSelected(item);
    }
  }


  public void setItem(POSItem item) {
    this.item = item;

  }
}
