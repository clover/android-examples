package com.clover.cfp.examples;


import com.clover.cfp.examples.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class CarouselFragment extends Fragment {

  private ImageView carousel;
  private int imagesToShow[] = { R.drawable.station_counter, R.drawable.mini_yoga, R.drawable.station_sublime_donut, R.drawable.mini_phone_pay };



  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_carousel, container, false);

    carousel = (ImageView) view.findViewById(R.id.image_carousel);
    carousel.setVisibility(View.GONE);

    return view;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override public void onResume() {
    animate(carousel, imagesToShow, 0,true);

    carousel.setVisibility(View.VISIBLE);

    super.onResume();
  }

  private void animate(final ImageView imageView, final int images[], final int imageIndex, final boolean forever) {

    //imageView <-- The View which displays the images
    //images[] <-- Holds R references to the images to display
    //imageIndex <-- index of the first image to show in images[]
    //forever <-- If equals true then after the last image it starts all over again with the first image resulting in an infinite loop. You have been warned.

    int fadeInDuration = 500; // Configure time values here
    int timeBetween = 3000;
    int fadeOutDuration = 1000;

    imageView.setVisibility(View.INVISIBLE);    //Visible or invisible by default - this will apply when the animation ends
    imageView.setImageResource(images[imageIndex]);

    Animation fadeIn = new AlphaAnimation(0, 1);
    fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
    fadeIn.setDuration(fadeInDuration);

    Animation fadeOut = new AlphaAnimation(1, 0);
    fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
    fadeOut.setStartOffset(fadeInDuration + timeBetween);
    fadeOut.setDuration(fadeOutDuration);

    AnimationSet animation = new AnimationSet(false); // change to false
    animation.addAnimation(fadeIn);
    animation.addAnimation(fadeOut);
    animation.setRepeatCount(1);
    imageView.setAnimation(animation);

    animation.setAnimationListener(new Animation.AnimationListener() {
      public void onAnimationEnd(Animation animation) {
        if (images.length - 1 > imageIndex) {
          animate(imageView, images, imageIndex + 1, forever); //Calls itself until it gets to the end of the array
        } else {
          if (forever == true) {
            animate(imageView, images, 0, forever);  //Calls itself to start the animation all over again in a loop if forever = true
          }
        }
      }

      public void onAnimationRepeat(Animation animation) {
        // TODO Auto-generated method stub
      }

      public void onAnimationStart(Animation animation) {
        // TODO Auto-generated method stub
      }
    });
  }
}
