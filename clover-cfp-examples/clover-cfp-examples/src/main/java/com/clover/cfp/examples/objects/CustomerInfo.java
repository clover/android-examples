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

public class CustomerInfo implements Parcelable{
  public String phoneNumber;
  public String customerName;

  protected CustomerInfo(Parcel in) {
    phoneNumber = (String)in.readValue(null);
    customerName = (String)in.readValue(null);
  }

  public static final Creator<CustomerInfo> CREATOR = new Creator<CustomerInfo>() {
    @Override
    public CustomerInfo createFromParcel(Parcel in) {
      return new CustomerInfo(in);
    }

    @Override
    public CustomerInfo[] newArray(int size) {
      return new CustomerInfo[size];
    }
  };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(phoneNumber);
    dest.writeString(customerName);
  }
}
