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

import com.clover.cfp.examples.R;
import com.clover.cfp.examples.objects.Rating;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RatingFragment.OnRatingChangedListener} interface
 * to handle interaction events.
 * Use the {@link RatingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RatingFragment extends Fragment implements View.OnClickListener {

  private static String RATING = "Rating";
  private OnRatingChangedListener mListener;
  private Rating rating;

  ImageView[] offs = new ImageView[5];
  ImageView[] ons = new ImageView[5];

  public static RatingFragment newInstance(Rating rating, OnRatingChangedListener listener) {
    RatingFragment fragment = new RatingFragment();
    Bundle args = new Bundle();
    fragment.rating = rating;
    fragment.mListener = listener;
    args.putSerializable(RATING, rating);
    fragment.setArguments(args);
    return fragment;
  }

  public RatingFragment() {
    // Required empty public constructor
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      rating = (Rating) getArguments().getSerializable(RATING);
    }

  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    int orientation = this.getResources().getConfiguration().orientation;
    View view;
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      view = inflater.inflate(R.layout.fragment_rating_question_portrait, container, false);
    } else {
      view = inflater.inflate(R.layout.fragment_rating_question_landscape, container, false);
    }
    TextView ratingLabel = (TextView) view.findViewById(R.id.ratingLabel);
//    TextView overlay = (TextView) view.findViewById(R.id.textView2);
    ratingLabel.setText(rating.question);
//    overlay.setText(rating.question);

    offs[0] = (ImageView)view.findViewById(R.id.imageView1off);
    offs[1] = (ImageView)view.findViewById(R.id.imageView2off);
    offs[2] = (ImageView)view.findViewById(R.id.imageView3off);
    offs[3] = (ImageView)view.findViewById(R.id.imageView4off);
    offs[4] = (ImageView)view.findViewById(R.id.imageView5off);

    ons[0] = (ImageView)view.findViewById(R.id.imageView1on);
    ons[1] = (ImageView)view.findViewById(R.id.imageView2on);
    ons[2] = (ImageView)view.findViewById(R.id.imageView3on);
    ons[3] = (ImageView)view.findViewById(R.id.imageView4on);
    ons[4] = (ImageView)view.findViewById(R.id.imageView5on);

    for (ImageView iv : offs) {
      iv.setOnClickListener(this);
    }
    for (ImageView iv : ons) {
      iv.setOnClickListener(this);
    }


    return view;
  }

  @Override public void onClick(View v) {
    for(int i=offs.length-1; i>=0; i--) {
      if(offs[i] == v || ons[i] == v) {
        rating.value = i+1;
        for(int j=0;j<offs.length; j++) {
          if(j < rating.value) {
            ons[j].setVisibility(View.VISIBLE);
            offs[j].setVisibility(View.GONE);
          } else {
            ons[j].setVisibility(View.GONE);
            offs[j].setVisibility(View.VISIBLE);
          }
        }
        break;
      }
    }
    mListener.onRatingChanged(rating);
  }

  @Override public void onResume() {
    super.onResume();

    /*try {
      mListener = (OnRatingChangedListener)getParentFragment();//(OnRatingChangedListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(getActivity().toString() + " must implement OnRatingsChangedListener");
    }*/
  }

  @Override public void onPause() {
    super.onPause();
    mListener = null;
  }

  @Override public void onStart() {
    super.onStart();
  }

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);

  }

  @Override public void onDetach() {
    super.onDetach();
    mListener = null;
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
  public interface OnRatingChangedListener {
    // TODO: Update argument type and name
    public void onRatingChanged(Rating rating);
  }

}
