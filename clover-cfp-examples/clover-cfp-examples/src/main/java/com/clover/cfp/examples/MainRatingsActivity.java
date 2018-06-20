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

package com.clover.cfp.examples;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.clover.cfp.examples.objects.CustomerInfo;
import com.clover.cfp.examples.objects.CustomerInfoMessage;
import com.clover.cfp.examples.objects.PayloadMessage;
import com.clover.cfp.examples.objects.PhoneNumberMessage;
import com.clover.cfp.examples.objects.Rating;
import com.clover.cfp.examples.objects.RatingsMessage;
import com.clover.cfp.examples.objects.RequestRatingsMessage;
import com.clover.cfp.activity.CloverCFPActivity;


import com.google.gson.Gson;

import static android.content.ContentValues.TAG;

public class MainRatingsActivity extends CloverCFPActivity implements CustomerLoginFragment.CustomerLoginFragmentListener,
    RatingFragment.OnRatingChangedListener, CustomerInfoFragment.CustomerInfoFragmentListener {

  private RatingFragment.OnRatingChangedListener mRatingChangedListener;
  private Rating[] ratings;
  private Rating currentRating;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_ratings);
    try {
      mRatingChangedListener = this;
    } catch (ClassCastException e) {
      throw new ClassCastException(this.toString() + " must implement OnRatingsChangedListener");
    }

    Fragment customerLoginFragment = CustomerLoginFragment.newInstance();

    FragmentTransaction transaction = getFragmentManager().beginTransaction();
    transaction.replace(R.id.fragment_container, customerLoginFragment);
    transaction.addToBackStack(null);
    transaction.commit();
  }

  @Override public void onNumberEntered(String phoneNumber) {
    waiting();
    PhoneNumberMessage msg = new PhoneNumberMessage(phoneNumber);
    String rawMsg = new Gson().toJson(msg);
    try {
      sendMessage(rawMsg);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onStartRatings() {
    waiting();
    RequestRatingsMessage msg = new RequestRatingsMessage();
    String rawMsg = new Gson().toJson(msg);
    try {
      sendMessage(rawMsg);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void waiting() {
    Fragment waiting = WaitingFragment.newInstance();
    FragmentTransaction transaction = getFragmentManager().beginTransaction();
    transaction.replace(R.id.fragment_container, waiting);
    transaction.addToBackStack(null);
    transaction.commit();
  }

  @Override public void onMessage(final String payload) {
    runOnUiThread(new Runnable(){
      @Override public void run() {
        //Toast.makeText(MainRatingsActivity.this, payload, Toast.LENGTH_SHORT).show();
        new CountDownTimer(3000,1000){

          @Override
          public void onTick(long miliseconds){}

          @Override
          public void onFinish(){
            //after 5 seconds continue
            PayloadMessage payloadMessage = new Gson().fromJson(payload,PayloadMessage.class);
            switch (payloadMessage.messageType) {
              case RATINGS:
                handleRatings(payload);
                break;
              case CUSTOMER_INFO:
                handleCustomerInfo(payload);
                break;
              default:
                Toast.makeText(getApplicationContext(), R.string.unknown_payload + payloadMessage.payloadClassName, Toast.LENGTH_LONG).show();
            }

          }
        }.start();

      }
    });
  }

  private void handleRatings(String payloadJson) {
    ratings = ((RatingsMessage)RatingsMessage.fromJsonString(payloadJson)).ratings;

    LinearLayout v = (LinearLayout)findViewById(R.id.fragment_container);
    v.removeAllViews();
    v.invalidate();

    // Create new fragment and transaction
    // for the first rating
    if (ratings != null && ratings.length > 0) {
      currentRating = ratings[0];
      addRatingFragment();
    }
  }

  private void addRatingFragment() {
    Fragment newFragment = RatingFragment.newInstance(currentRating, mRatingChangedListener);
    FragmentTransaction transaction = getFragmentManager().beginTransaction();

    // Replace whatever is in the fragment_container view with this fragment,
    // and add the transaction to the back stack if needed
    transaction.replace(R.id.fragment_container, newFragment);
    transaction.addToBackStack(null);

    // Commit the transactions
    transaction.commit();
  }

  private void handleCustomerInfo(String payloadJson) {
    CustomerInfo customerInfo = ((CustomerInfoMessage)CustomerInfoMessage.fromJsonString(payloadJson)).customerInfo;

    LinearLayout v = (LinearLayout)findViewById(R.id.fragment_container);
    v.removeAllViews();
    v.invalidate();

    // Create new fragment and transaction
    Fragment newFragment = new CustomerInfoFragment();
    FragmentTransaction transaction = getFragmentManager().beginTransaction();

    Bundle bundle = new Bundle();
    bundle.putSerializable("CustomerName", customerInfo.customerName);

    newFragment.setArguments(bundle);

    // Replace whatever is in the fragment_container view with this fragment,
    // and add the transaction to the back stack if needed
    transaction.replace(R.id.fragment_container, newFragment);
    transaction.addToBackStack(null);

    // Commit the transactions
    transaction.commit();
  }

  @Override public void onRatingChanged(Rating rating) {
    for (Rating oldRating : ratings) {
      if (oldRating.id.equals(rating.id)) {
        oldRating.value = rating.value;
        break;
      }
    }
    onRatingsChanged(ratings);
  }

  private void onRatingsChanged(Rating[] ratings) {
    RatingsMessage ratingsMessage = new RatingsMessage(ratings);
    String rawMsg = new Gson().toJson(ratingsMessage);
    try {
      sendMessage(rawMsg);
    } catch (Exception e) {
      e.printStackTrace();
    }
    Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      public void run() {
        gotoNextRating();
      }
    }, 500);
  }

  private void gotoNextRating() {
    int i = 0;
    Rating holdRating = currentRating;
    currentRating = null;

    while (i < ratings.length) {
      if (ratings[i].id.equals(holdRating.id)) {
        i++;
        if (i < ratings.length) {
          currentRating = ratings[i]; //Found next rating!
        }
        break;
      } else {
        i++;
      }
    }
    if (currentRating != null) {
      addRatingFragment();
    } else {
      addRatingsFinalFragment();
    }
  }

  private void addRatingsFinalFragment() {
    Fragment newFragment = RatingsFragment.newInstance(ratings);
    FragmentTransaction transaction = getFragmentManager().beginTransaction();

    // Replace whatever is in the fragment_container view with this fragment,
    // and add the transaction to the back stack if needed
    transaction.replace(R.id.fragment_container, newFragment);
    transaction.addToBackStack(null);

    // Commit the transactions
    transaction.commit();
  }
}
