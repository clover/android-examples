package com.clover.cfp.examples;

import com.clover.cfp.examples.R;
import com.clover.cfp.activity.CloverCFPActivity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CustomerInfoFragment.CustomerInfoFragmentListener} interface
 * to handle interaction events.
 * Use the {@link CustomerInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CustomerInfoFragment extends Fragment{
  private CustomerInfoFragmentListener mListener;
  private String customerName;
  private TextView customerNameLabel;

  public static CustomerInfoFragment newInstance() {
    CustomerInfoFragment fragment = new CustomerInfoFragment();
    Bundle args = new Bundle();
    return fragment;
  }

  public CustomerInfoFragment() {
    // Required empty public constructor
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    customerName = getArguments().getString("CustomerName");
  }


  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    int orientation = this.getResources().getConfiguration().orientation;
    View view;
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      view = inflater.inflate(R.layout.fragment_customer_portrait, container, false);
    } else {
      view = inflater.inflate(R.layout.fragment_customer_landscape, container, false);
    }

    customerNameLabel = (TextView) view.findViewById(R.id.textView);
    String labelText = null;
    if (customerName == null || customerName.isEmpty()) {
      labelText = getResources().getText(R.string.welcome) + " loyal customer!";
    } else {
      labelText = getResources().getText(R.string.welcome) + " " + customerName;
    }
    customerNameLabel.setText(labelText);
    Button ok_btn = (Button)view.findViewById(R.id.okButton);

    ok_btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mListener != null) {
          mListener.onStartRatings();
        }
      }
    });

    Button cancel_btn = (Button)view.findViewById(R.id.cancelButton);

    cancel_btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent result = new Intent();
        getActivity().setResult(Activity.RESULT_OK, result);
        String data = "Customer opted out of the ratings application";
        ((CloverCFPActivity)getActivity()).setResultAndFinish(Activity.RESULT_OK, data);
      }
    });

    return view;
  }

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (CustomerInfoFragmentListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement CustomerInfoFragmentListener");
    }
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
  public interface CustomerInfoFragmentListener {
    public void onStartRatings();
  }

  public void yesClicked(View v) {
    if (mListener != null) {
      mListener.onStartRatings();
    }
  }
  public void noClicked(View v) {
    getActivity().finish();
  }

}
