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

package com.clover.cfp.examples.objects;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;

public class Rating implements Parcelable, Serializable {
  public String id;
  public String question;
  public int value;

  protected Rating(Parcel in) {
    id = (String)in.readValue(null);
    question = (String)in.readValue(null);
    value = Integer.valueOf((String)in.readValue(null));
  }

  public static final Creator<Rating> CREATOR = new Creator<Rating>() {
    @Override
    public Rating createFromParcel(Parcel in) {
      return new Rating(in);
    }

    @Override
    public Rating[] newArray(int size) {
      return new Rating[size];
    }
  };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(id);
    dest.writeString(question);
    dest.writeInt(value);
  }
}