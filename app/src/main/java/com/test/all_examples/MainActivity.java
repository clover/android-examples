/**
 * Copyright (C) 2015 Clover Network, Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.test.all_examples;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final ListView activityList = (ListView) findViewById(R.id.activity_list);
    activityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ActivityInfo activityInfo = (ActivityInfo) adapterView.getAdapter().getItem(i);
        launch(activityInfo);
      }
    });

    try {
      final PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
      final ListAdapter listAdapter = new ActivityAdapter(this, filter(pi.activities));
      activityList.setAdapter(listAdapter);
    } catch (PackageManager.NameNotFoundException e) {
      Log.e(this.getClass().getSimpleName(), e.getMessage(), e.fillInStackTrace());
    }
  }

  private void launch(ActivityInfo activityInfo) {
    Intent intent;

    try {
      intent = new Intent(this, Class.forName(activityInfo.name));
    } catch (ClassNotFoundException e) {
      Log.e(this.getClass().getSimpleName(), e.getMessage(), e.fillInStackTrace());

      intent = new Intent(Intent.ACTION_MAIN);
      intent.addCategory(Intent.CATEGORY_LAUNCHER);
      intent.setComponent(new ComponentName(activityInfo.packageName, activityInfo.name));
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    startActivity(intent);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_javadoc:
        Intent docIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://clover.github.io/clover-android-sdk/"));
        startActivity(docIntent);
        return true;
      case R.id.action_source:
        Intent sourceIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/clover/clover-android-sdk"));
        startActivity(sourceIntent);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private ActivityInfo[] filter(ActivityInfo[] activityInfos) throws PackageManager.NameNotFoundException {
    final ActivityInfo thisActivity = getPackageManager().getActivityInfo(new ComponentName(getPackageName(), getClass().getName()), PackageManager.GET_ACTIVITIES);
    List<ActivityInfo> activityInfoList = new ArrayList<ActivityInfo>();
    for (ActivityInfo ai : activityInfos) {
      if (ai.name.equals(thisActivity.name) || ai.) {
        continue;
      }
      activityInfoList.add(ai);
    }
    Collections.sort(activityInfoList, new Comparator<ActivityInfo>() {
      private static final int IS_EQUAL = 0;

      @Override
      public int compare(ActivityInfo activityInfo1, ActivityInfo activityInfo2) {
        int compare = activityInfo1.packageName.compareTo(activityInfo2.packageName);
        if (compare == IS_EQUAL) {
          compare = activityInfo1.name.compareTo(activityInfo2.name);
        }
        return compare;
      }
    });

    return activityInfoList.toArray(new ActivityInfo[activityInfos.length - 1]);
  }
}
