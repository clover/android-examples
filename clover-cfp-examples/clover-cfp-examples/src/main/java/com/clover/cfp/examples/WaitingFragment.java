package com.clover.cfp.examples;

import com.clover.cfp.examples.R;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WaitingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WaitingFragment extends Fragment {

  private RotateAnimation anim;
  private ImageView imageView;

  public static WaitingFragment newInstance() {
    WaitingFragment fragment = new WaitingFragment();
    Bundle args = new Bundle();
    return fragment;
  }

  public WaitingFragment() {
    // Required empty public constructor
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_blank, container, false);

    imageView = (ImageView) view.findViewById(R.id.imageViewRotate);

    anim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    anim.setInterpolator(new LinearInterpolator());
    anim.setRepeatCount(Animation.INFINITE);
    anim.setDuration(700);

    // Start animating the image
    //final ImageView splash = (ImageView) findViewById(R.id.splash);
    //splash.startAnimation(anim);

    // Later.. stop the animation
    //splash.setAnimation(null);
    return view;
  }

  @Override public void onStart() {
    super.onStart();
    imageView.startAnimation(anim);
  }

  @Override public void onPause() {
    super.onPause();
    imageView.setAnimation(null);
  }
}
