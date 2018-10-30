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

package com.clover.example.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class PagerAdapter extends FragmentPagerAdapter {
  List<Fragment> fragments = new ArrayList<>();

  public PagerAdapter(FragmentManager fragmentManager) {
    super(fragmentManager);
  }

  public void addFragment(Fragment fragment) {
    fragments.add(fragment);
  }

  @Override
  public Fragment getItem(int position) {
    if (position < 0 || position > 3) {
      throw new IllegalArgumentException("position must be 0-3");
    }
    return fragments.get(position);
  }

  @Override
  public int getCount() {
    return fragments.size();
  }
}
