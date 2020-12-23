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

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.clover.cfp.activity.CFPConstants;
import com.clover.cfp.examples.objects.Rating;
import com.clover.cfp.examples.objects.RatingsMessage;
import com.clover.cfp.activity.CloverCFPActivity;

import com.google.gson.Gson;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link RatingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RatingsFragment extends Fragment implements View.OnClickListener{

  public static final String RATINGS = "Ratings";
  private final Gson GSON = new Gson();
  private Rating[] ratings;

  public static RatingsFragment newInstance(Rating[] ratings) {
    RatingsFragment fragment = new RatingsFragment();
    fragment.ratings = ratings;
    Bundle args = new Bundle();
    args.putSerializable(RATINGS, ratings);
    fragment.setArguments(args);
    return fragment;
  }

  public RatingsFragment() {
    // Required empty public constructor
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ratings = (Rating[]) getArguments().getSerializable(RATINGS);
  }

  @Override public void onStart() {
    super.onStart();


  }

  @Override public void onResume() {
    super.onResume();
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    int orientation = this.getResources().getConfiguration().orientation;
    View view;
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      view = inflater.inflate(R.layout.fragment_ratings_final_portrait, container, false);
    } else {
      view = inflater.inflate(R.layout.fragment_ratings_final_landscape, container, false);
    }

    Button btn = (Button)view.findViewById(R.id.ratingsDoneButton);

    btn.setOnClickListener(this);

    return view;
  }

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);
  }

  @Override public void onDetach() {
    super.onDetach();
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p/>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  @Override public void onClick(View v) {
    Intent result = new Intent();
    RatingsMessage ratingsMessage = new RatingsMessage(ratings);
    String rawMsg = new Gson().toJson(ratingsMessage);
    result.putExtra(CFPConstants.EXTRA_PAYLOAD, rawMsg);
    getActivity().setResult(Activity.RESULT_OK, result);
    ((CloverCFPActivity)getActivity()).setResultAndFinish(Activity.RESULT_OK, rawMsg);
  }
}
