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

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PopupMessageFragment extends DialogFragment {

  private View view;
  private TextView title, close;
  private LinearLayout content;
  private String dialogTitle;
  private String[] dialogContent;
  private boolean monospace = false;

  public static PopupMessageFragment newInstance(String title, String[] messageContent){
    PopupMessageFragment fragment = new PopupMessageFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    fragment.setTitle(title);
    fragment.setContent(messageContent);
    return fragment;
  }

  public static PopupMessageFragment newInstance(String title, String[] messageContent, boolean monospace){
    PopupMessageFragment fragment = new PopupMessageFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    fragment.setTitle(title);
    fragment.setContent(messageContent);
    fragment.setMonospace(monospace);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(STYLE_NO_TITLE, R.style.CustomDialog);
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_popup_message, container, false);
    title = (TextView) view.findViewById(R.id.PopupTitle);
    content = (LinearLayout) view.findViewById(R.id.PopupMessageContent);
    close = (TextView) view.findViewById(R.id.PopupClose);
    close.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismissAllowingStateLoss();
      }
    });

    if(title != null) {
      title.setText(dialogTitle);
    }
    for(String line : dialogContent){
      TextView textLine = new TextView(getActivity());
      if(monospace) {
        textLine.setTypeface(Typeface.MONOSPACE);
      }
      textLine.setText(line);
      content.addView(textLine);
    }
    return view;
  }

  @Override
  public void show(FragmentManager manager, String tag) {
    try {
      FragmentTransaction ft = manager.beginTransaction();
      ft.add(this, tag);
      ft.commitAllowingStateLoss();
    } catch (IllegalStateException e) {
      Log.d("ABSDIALOGFRAG", "Exception", e);
    }
  }

  public void setTitle(String title){
    dialogTitle = title;
  }

  public void setContent(String[] content){
    dialogContent = content;
  }

  public void setMonospace(boolean useMonospace){
    monospace = useMonospace;
  }

}

