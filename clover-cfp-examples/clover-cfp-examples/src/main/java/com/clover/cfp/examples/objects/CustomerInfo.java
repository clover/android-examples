package com.clover.cfp.examples.objects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by glennbedwell on 5/4/17.
 */
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
