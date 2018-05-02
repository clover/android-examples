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

import com.clover.example.model.POSTransaction;
import com.clover.example.utils.Utility;
import com.clover.sdk.v3.connector.IPaymentConnector;
import com.clover.example.adapter.ItemsListViewAdapter;
import com.clover.example.adapter.OrdersListViewAdapter;
import com.clover.example.adapter.PaymentsListViewAdapter;
import com.clover.example.model.OrderObserver;
import com.clover.example.model.POSCard;
import com.clover.example.model.POSDiscount;
import com.clover.example.model.POSLineItem;
import com.clover.example.model.POSOrder;
import com.clover.example.model.POSPayment;
import com.clover.example.model.POSRefund;
import com.clover.example.model.POSStore;
import com.clover.example.model.StoreObserver;
import com.clover.sdk.v3.base.PendingPaymentEntry;
import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OrdersFragment extends Fragment implements OrderObserver {
  private POSStore store;
  private OnFragmentInteractionListener mListener;
  private WeakReference<IPaymentConnector> paymentConnectorWeakReference;
  private ListView itemsListView;
  private View view;
  private ListView ordersListView;
  private Button openInRegister;
  private POSOrder selectedOrder = null;

  public static OrdersFragment newInstance(POSStore store, IPaymentConnector paymentConnector) {
    OrdersFragment fragment = new OrdersFragment();
    fragment.setStore(store);
    fragment.setPaymentConnector(paymentConnector);
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public OrdersFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    if(selectedOrder != null) {
      selectedOrder.removeObserver(this);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    view = inflater.inflate(R.layout.fragment_orders, container, false);

    openInRegister = (Button) view.findViewById(R.id.OrdersGoToRegister);

    itemsListView = (ListView) view.findViewById(R.id.ItemsGridView);
    final ItemsListViewAdapter itemsListViewAdapter = new ItemsListViewAdapter(view.getContext(), R.id.ItemsGridView, Collections.EMPTY_LIST);
    itemsListView.setAdapter(itemsListViewAdapter);

    final ListView paymentsListView = (ListView) view.findViewById(R.id.PaymentsGridView);
    final PaymentsListViewAdapter paymentsListViewAdapter = new PaymentsListViewAdapter(view.getContext(), R.id.PaymentsGridView, Collections.EMPTY_LIST);
    paymentsListView.setAdapter(paymentsListViewAdapter);

    ordersListView = (ListView) view.findViewById(R.id.OrdersListView);
    OrdersListViewAdapter ordersListViewAdapter = new OrdersListViewAdapter(view.getContext(), R.id.OrdersListView, store.getOrders());
    ordersListView.setAdapter(ordersListViewAdapter);
    ordersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(selectedOrder != null) {
          selectedOrder.removeObserver(OrdersFragment.this);
        }
        POSOrder posOrder = (POSOrder) ordersListView.getItemAtPosition(position);
        selectedOrder = posOrder;
        posOrder.addOrderObserver(OrdersFragment.this);
        updateDisplaysForOrder(posOrder);
      }
    });

    paymentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        POSTransaction posTransaction = (POSTransaction) paymentsListView.getItemAtPosition(position);
        if(posTransaction instanceof POSRefund){
          posTransaction = store.getPaymentByCloverId(((POSRefund)posTransaction).getPaymentId());
        }
        ((NativePOSActivity)getActivity()).showPaymentDetails(posTransaction);
      }
    });

    if(getActivity().getResources().getBoolean(R.bool.isFlex)){
      Utility.setListViewHeightBasedOnChildren(itemsListView);
      Utility.setListViewHeightBasedOnChildren(paymentsListView);
    }

    return view;
  }

  private void updateDisplaysForOrder(POSOrder posOrder) {
    updateItems(posOrder);
    updatePayments(posOrder);
  }

  private void updateItems(final POSOrder posOrder) {
    getView().post(new Runnable(){
      @Override public void run() {
        final ListView itemsListView = (ListView) view.findViewById(R.id.ItemsGridView);
        ItemsListViewAdapter itemsListViewAdapter = new ItemsListViewAdapter(view.getContext(), R.id.ItemsGridView, posOrder.getItems());
        itemsListView.setAdapter(itemsListViewAdapter);
        if(getActivity().getResources().getBoolean(R.bool.isFlex)){
          Utility.setListViewHeightBasedOnChildren(itemsListView);
        }
      }
    });
  }

  private void updatePayments(final POSOrder posOrder) {
    getView().post(new Runnable() {
      @Override public void run() {
        final ListView paymentsListView = (ListView) view.findViewById(R.id.PaymentsGridView);
        PaymentsListViewAdapter paymentsListViewAdapter = new PaymentsListViewAdapter(view.getContext(), R.id.PaymentsGridView, posOrder.getPayments());
        paymentsListView.setAdapter(paymentsListViewAdapter);
        if(getActivity().getResources().getBoolean(R.bool.isFlex)){
          Utility.setListViewHeightBasedOnChildren(paymentsListView);
        }
        if(posOrder.getPayments().size() < 1 || posOrder.getStatus().toString() == "OPEN"){
          openInRegister.setVisibility(View.VISIBLE);
          openInRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              openInRegister(posOrder);
            }
          });
        }
        else if(posOrder.getPayments().size() > 0) {
          openInRegister.setVisibility(View.INVISIBLE);
        }
      }
    });
  }

  public void openInRegister(POSOrder order){
    store.setCurrentOrder(order);
    ((NativePOSActivity)getActivity()).showRegister(null);
  }

  // TODO: Rename method, update argument and hook method into UI event
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

  public void setStore(final POSStore store) {
    this.store = store;

    store.addStoreObserver(new StoreObserver() {
      @Override
      public void onCurrentOrderChanged(POSOrder currentOrder) {

      }

      @Override
      public void newOrderCreated(POSOrder order, boolean userInitiated) {
        updateOrderList();
      }

      @Override public void cardAdded(POSCard card) {

      }

      @Override public void refundAdded(POSTransaction refund) {

      }

      @Override public void pendingPaymentsRetrieved(List<PendingPaymentEntry> pendingPayments) {

      }

      @Override
      public void transactionsChanged(List<POSTransaction> transactions) {

      }

    });

  }

  private void updateOrderList() {
    final List<POSOrder> orders = new ArrayList<POSOrder>(store.getOrders().size());
    List<POSOrder> storeOrders = store.getOrders();
    for(POSOrder currentOrder : storeOrders) {
      if(currentOrder.getStatus() != POSOrder.OrderStatus.INITIAL) {
          orders.add(currentOrder);
      }
    }

    Collections.sort(orders, new Comparator<POSOrder>() {
      @Override public int compare(POSOrder lhs, POSOrder rhs) {
        return Integer.parseInt(rhs.id) - Integer.parseInt(lhs.id);
      }
    });
    if(getActivity() != null) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if(view != null) {
            OrdersListViewAdapter listViewAdapter = new OrdersListViewAdapter(view.getContext(), R.id.ItemsGridView, orders);
            ordersListView.setAdapter(listViewAdapter);
          }
        }
      });
    }
  }

  @Override public void lineItemAdded(POSOrder posOrder, POSLineItem lineItem) {
    updateOrderList();
  }

  @Override public void lineItemRemoved(POSOrder posOrder, POSLineItem lineItem) {
    updateOrderList();
  }

  @Override public void lineItemChanged(POSOrder posOrder, POSLineItem lineItem) {
    updateOrderList();
  }

  @Override public void paymentAdded(POSOrder posOrder, POSPayment payment) {
    updateOrderList();
  }

  @Override public void refundAdded(POSOrder posOrder, POSRefund refund) {
    updateOrderList();
    updateDisplaysForOrder(posOrder);
  }

  @Override public void paymentChanged(POSOrder posOrder, POSTransaction pay) {
    updateOrderList();
    updatePayments(posOrder);
  }

  @Override public void discountAdded(POSOrder posOrder, POSDiscount discount) {
    updateOrderList();
  }

  @Override public void discountChanged(POSOrder posOrder, POSDiscount discount) {
    updateOrderList();
  }

  @Override
  public void paymentVoided(POSOrder posOrder, POSPayment payment) {

  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
    public void onFragmentInteraction(Uri uri);
  }

  public void setPaymentConnector(IPaymentConnector paymentConnector) {
    paymentConnectorWeakReference = new WeakReference<IPaymentConnector>(paymentConnector);
  }

}
