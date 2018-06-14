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

import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

// created this subclass so I could programmatically open the dialog
public class CustomEditTextPreference extends EditTextPreference {

  public CustomEditTextPreference(Context ctx) {
    super(ctx);
  }

  public CustomEditTextPreference(Context ctx, AttributeSet attributeSet) {
    super(ctx, attributeSet);
  }

  public CustomEditTextPreference(Context ctx, AttributeSet attributeSet, int defStyle) {
    super(ctx, attributeSet, defStyle);
  }

  public void show(Bundle state) {
    super.showDialog(state);
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    super.onDialogClosed(positiveResult);
  }
}
